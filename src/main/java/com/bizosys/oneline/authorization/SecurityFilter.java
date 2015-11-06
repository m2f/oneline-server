/**
* Copyright 2010 Bizosys Technologies Limited
*
* Licensed to the Bizosys Technologies Limited (Bizosys) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The Bizosys licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.bizosys.oneline.authorization;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.user.UserProfile;
import com.bizosys.oneline.util.CreateException;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.ServletUtil;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.Response;

/**
 * SecurityFilter is the gate keeper for security.
 * It makes sure users are authenticated before they are given access 
 * to the site. Guest access is provided for not yet logged in users 
 */
public class SecurityFilter implements Filter
{
	private static final Logger LOG = LogManager.getLogger(SecurityFilter.class);
	private static final boolean INFO_ENABLED = LOG.isInfoEnabled();

	/**
	 * Check1 - 
	 * Find out the found user. Check the last login time
	 * If we are caching the last login time, good.
	 * If the login time > XX days Send the LOGIN_EXPIRED error message.
	 * 
	 * Check2 - 
	 * Extract the service and action. If the service and action are present, 
	 * find out the authority. '*' means allow all.
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) 
		throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		IAuthenticate authenticator = null;
		IAuthroize authorize = null;
		UserProfile user = null;
		try {
			authenticator = AuthenticatorFactory.getInstance().getAuthenticatorImpl();
			authorize = AuthorizationFactory.getInstance().getAuthorizationcatorImpl();
			
			user = authenticator.getUser(request, response);
			
			request.setAttribute("__user", user);
			Map<String, String> inputData = ServletUtil.extractQueryElements(request);
			
			String service = inputData.get("service");
			String action = inputData.get("action");
			
			boolean isPermitted = false;
			if ( service.equals("sql")) {
				
				if( ! StringUtils.isEmpty(action) ) {
					
					isPermitted = authorize.isAuthorized(user, service, action);
					if ( isPermitted ) {
						if ( "refresh".equals(action)) authorize.build();
					}
				
				} 
			} else {
				isPermitted = authorize.isAuthorized(user, service, action);
			}
			
			if ( isPermitted ) {
				
				chain.doFilter(request, response);
				
			} else {
				Map<String, String> errors = new HashMap<String, String>();
				errors.put("__SERVICE", service);
				errors.put("__ACTION", action);
				sendError(request, response, authenticator, user, ErrorCodes.AUTH_FAILURE, errors); 
				return;
			}
			
			
			/**
			 * Creates a audit log
			 */
			String userId = ( null == user) ? "null" : user.loginid;
			Integer tenantId = ( null == user) ? -1 : user.getTenantId();
			String clientHost = ( null == request) ? "a.b.c.d" : request.getRemoteAddr();
			if ( inputData.containsKey("password"))  inputData.put("password", "XXX");
			if ( inputData.containsKey("newpassword"))  inputData.put("newpassword", "XXX");
			if ( inputData.containsKey("__user"))  inputData.remove("__user");
			
			String inputDataStr = inputData.toString();
			String isPermittedStr = isPermitted ? "authorized" : "rejected";
			
			//Skip audit log if presto driver..HardCoded for now..change it
			/*
			if(!DbConfigModel.DEFAULT_PREPARE_STMT_SUPPORT)
				AuditSink.getInstance().addTask(
					new Systemlog( tenantId,action,service,clientHost,inputDataStr,isPermittedStr,userId));				
			*/
			
			if ( INFO_ENABLED ) {
				StringBuilder sb = new StringBuilder();
				sb.append("Teant:").append(tenantId).append(" User:").append(userId).append(" action:").
				append(action).append("client machine:").append(clientHost).append(" Input : ").
				append(inputDataStr).append(" Allowed ").append(isPermittedStr);
				
				LOG.info(sb.toString());
			}
		
		} catch (CreateException e) {
			LOG.fatal(e);
			sendError(request, response, authenticator, null, ErrorCodes.SYSTEM_CONFIGURATION_FAILURE, null);
			return;

		} catch (NullPointerException e) {
			e.printStackTrace();
			if ( null == user ) LOG.fatal(e);
			else LOG.fatal(user.getProfile(), e);
			
			sendError(request, response, authenticator, null, ErrorCodes.UNKNOWN_ERRORS, null);
			return;
		

		} catch (ErrorCodeExp e) {
			sendError(request, response, authenticator, null, e.errorCode, null);
			return;
		} finally {
			
			/**
			 * Clean up any session data.
			 */
			if ( null != authenticator) authenticator.clear();
		}
		

	}

	public void init(FilterConfig config) throws ServletException 
	{
		LOG.info("SecurityFilter is on and working.");
	}

	public void destroy() 
	{
		LOG.info("SecurityFilter is going down.");
	}
	
	private void sendError(HttpServletRequest request, HttpServletResponse response, IAuthenticate authenticator, 
			UserProfile user, String code, Map<String, String> variables) throws IOException {
		
		String locale = ( null == user ) ? "en" : user.getLocale();
		if ( null == locale ) locale = "en";
		
		String msg = ErrorMessages.getInstance().getMessage(locale, code);
		if ( null != variables) {
			for (String var : variables.keySet()) {
				msg = msg.replace(var, variables.get(var));
			}
		}
		
		String format = request.getParameter("format");
		if ( null == format ) {
			Object formatO = request.getParameter("format");
			if ( null != formatO) format = request.getParameter("format");
			else format = "jsonp";
		}

		PrintWriter pw = response.getWriter(); 
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		pw.write(Response.getError("xml".equals(format), true, code, msg, "SECURITY_ERROR", ErrorCodes.QUERY_KEY, "SECURITY_ERROR"));
		pw.flush();
		pw.close();

	}

	
}
