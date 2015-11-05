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

package com.bizosys.oneline.user;

import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.bizosys.oneline.authorization.AuthenticatorFactory;
import com.bizosys.oneline.authorization.IAuthenticate;
import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.Hash;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.InvalidRequestException;
import com.bizosys.oneline.web.sensor.Request;
import com.bizosys.oneline.web.sensor.Response;
import com.bizosys.oneline.web.sensor.Sensor;

public class UserProfileSensor implements Sensor 
{
	private static final int LOCK_DURATION_MINS = 30 ;
	private static final int LOCK_DURATION = LOCK_DURATION_MINS * 60 * 1000;
	private static final char STATUS_INACTIVE = 'N';
	private static final char STATUS_LOCK3 = 'L';
	private static final char STATUS_LOCK2 = 'K';
	private static final char STATUS_LOCK1 = 'J';
	private static final char STATUS_ACTIVE = 'Y';

	private final static Logger LOG = Logger.getLogger(UserProfileSensor.class);

	private String key = null;
	private boolean isLoginVerification = false;
	
	private IAuthenticate authenticator = null;
	private static final String PROFILE_ERROR_TITLE = "PROFILE_ERROR";

	@Override
	public void processRequest(Request request, Response response)
	{
		String action = request.action;

		UserProfile user = request.getUser();
		try
		{
			if ("login".equals(action))
			{
				Integer tenantId = request.getInteger("contextid", false);
				if(null == tenantId) this.login(request, response);
				else this.tenantlogin(request, response);
			}
			else if ("tenantlogin".equals(action))
			{
				this.tenantlogin(request, response);
			}
			else if ("logout".equals(action))
			{
				this.logout(request, response);
			}
			else if ("getloggedinuser".equals(action))
			{
				this.getLoggedInUser(request, response);
			}
			else if ("register".equals(action))
			{
				this.register(request, response);
			}
			else if ("changepassword".equals(action))
			{
				this.changePassword(request, response);
			}
			else if ("resetpassword".equals(action))
			{
				this.resetPassword(request, response);
			}
			else if ("activateuser".equals(action))
			{
				this.activateUser(request, response);
			}
			else if ("activate".equals(action))
			{
				this.activate(request, response);
			}
			else
			{
				LOG.warn("Invalid Request - " + request.toString());
				throw new InvalidRequestException("INVALID_OPERATION");
			}
		} 
		catch (InvalidRequestException ex) {
			response.setErrorMessage("INFORMATION_MISSING_ERROR", ErrorCodes.INVALID_INPUT_PARAMETER,
				ErrorMessages.getInstance().getMessage(user.getLocale(),
				ErrorCodes.INVALID_INPUT_PARAMETER)+ "'" + ex.getMessage() + "'", true);
			LOG.fatal(request.toString(), ex);
		} catch (Exception ex) {
			String locale = ( null == user ) ? "en" : user.getLocale();
			if ( null == locale ) locale = "en";
			
			response.setErrorMessage("UNKNOWN_ERROR", ErrorCodes.UNKNOWN_ERRORS, 
				ErrorMessages.getInstance().getMessage(locale, ErrorCodes.UNKNOWN_ERRORS), ex);
			LOG.fatal(request.toString(), ex);
		} 
	}
	
	private void register(Request request, Response response)
	{
		UserProfile user = request.getUser();
		WriteBase writeBase = null;
		try
		{
			Integer tenantId = user.getTenantId();
			if ( tenantId < 1) {
//				response.error("0014", LangPackLoader.getInstance().getMessage(user.getLocale(), "0014"));
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.REGISTRATION_NOT_ALLOWED, 
					ErrorMessages.getInstance().getMessage(user.getLocale(),
						ErrorCodes.REGISTRATION_NOT_ALLOWED), true);
				return;
			}

			String profile = request.getString("profile", true, false, true);
			String passwd = request.getString("password", true, true, false);
			String loginid = request.getString("loginid", true, true, false);
			
			// * Restrict no of users for company
			if ( tenantId > 1)
			{
				int noOfUsers = UserLoginTableExt.getUsersCount(tenantId);
				if( noOfUsers > 0 )
				{
					int limit = TenantLimit.getLimit(tenantId);
					if ( noOfUsers >= limit )
					{
						response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.USER_LIMIT_REACHED, 
							ErrorMessages.getInstance().getMessage(user.getLocale(), 
								ErrorCodes.USER_LIMIT_REACHED), true);
						return;
					}
				}
			}
			
			UserLogin existingUser = UserLoginTableExt.selectByLoginid(loginid);
	
			if (existingUser != null) {
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.USER_ALREADY_EXIST, 
					ErrorMessages.getInstance().getMessage(user.getLocale(),
						ErrorCodes.USER_ALREADY_EXIST), true);
				return;
			}
	
			char active = (this.isLoginVerification) ? STATUS_INACTIVE : STATUS_ACTIVE;
			
			String encodedPasswd = Hash.createHex(this.key, passwd);

			writeBase = new WriteBase();
			writeBase.beginTransaction();
			
			UserLogin newUser = new UserLogin(
				tenantId, new String(new char[]{active}), loginid, encodedPasswd, profile);
			UserLoginTableExt.insert(newUser, null);
			newUser.password = "";
			response.writeTextWithHeaderAndFooter(newUser.toJson()); // JSNOP fill
			
			writeBase.commitTransaction();
			writeBase = null;
			
		} 
		catch (Exception e)
		{
			if ( null != writeBase) {
				writeBase.rollbackTransaction();
				writeBase = null;
			}
			String locale = ( null == user ) ? "en" : user.getLocale();
			if ( null == locale ) locale = "en";
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.UNABLE_TO_REGISTER, 
				ErrorMessages.getInstance().getMessage(locale,ErrorCodes.UNABLE_TO_REGISTER) + e.getMessage(), e);
		}
	}

	private void tenantlogin(Request request, Response response) throws Exception {
		Integer tenantId = request.getInteger("contextid", true);
		login(request, response, tenantId); 
	}
	
	private void login(Request request, Response response) throws Exception {
		login(request, response, -1); 
	}
	
	private void login(Request request, Response response, int contextTenantId) throws Exception
	{
		String loginId = request.getString("loginid", true, true, false);
		String passwd = request.getString("password", true, true, false);
		Boolean rememberMe = request.getBoolean("rememberme", false);
		if( null == rememberMe) rememberMe = false;

		UserLogin userlogin = UserLoginTableExt.selectByLoginid(loginId);
		if (userlogin == null) {
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.INVALID_COMBINATION, 
					ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), 
						ErrorCodes.INVALID_COMBINATION), true);
			return;
		}

		if ( contextTenantId > 0) {
			if ( userlogin.tenantid == 1) {
				userlogin.tenantid =  contextTenantId; //Overriding the context login
				userlogin.profile = userlogin.profile.replaceFirst("root", "companyadmin"); // Overiding the role to companyadmin
			} else {
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.UNAUTHORIZED_ACCESS_ATTEMPT, 
						ErrorMessages.getInstance().getMessage(request.getUser().getLocale(),
							ErrorCodes.UNAUTHORIZED_ACCESS_ATTEMPT) + userlogin.tenantid, true);
				return;
			}
		}
		
		String companyName = "unknown"; 
		// Check Company Status
		if( userlogin.tenantid > 1)
		{
			char companyStatus = TenantLimit.getStatus(userlogin.tenantid);
			if ( companyStatus == STATUS_INACTIVE) {
				response.setErrorCode(PROFILE_ERROR_TITLE, ErrorCodes.COMPANY_ACCOUNT_DISABLED, 
					ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), 
						ErrorCodes.COMPANY_ACCOUNT_DISABLED), true);
				return;
			}
		}
		
		char status = STATUS_ACTIVE;
		if ( userlogin.active.length() > 0 ) status = userlogin.active.charAt(0);
		if ( status == STATUS_INACTIVE) {
			response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.ACCOUNT_DISABLED, 
				ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), 
					ErrorCodes.ACCOUNT_DISABLED), true);
			return;
		}

		long referenceTime = userlogin.touchtime.getTime();
		long timeLastTried = new Date().getTime() - referenceTime;
		if ( status == STATUS_LOCK3 && timeLastTried < LOCK_DURATION ) {
			response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.ACCOUNT_LOCKED,
				ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), 
					ErrorCodes.ACCOUNT_LOCKED).replace("__X", new Integer(LOCK_DURATION_MINS).toString()), true);
			 return;
		}
		
		//Good for password compare
		String encodedPasswd = Hash.createHex(this.key, passwd);

		if ( encodedPasswd.equals(userlogin.password)) {
			if ( status != STATUS_ACTIVE ) {
				UserLoginTableExt.activate(loginId, "Y", null);
			}

			userlogin.profile = "company|" + companyName + ",tenant|" + userlogin.tenantid + "," + userlogin.profile.toString();
			request.setUser(new UserProfile(loginId, userlogin.profile.toString()));
			authenticator.setUser(request.getUser(), rememberMe);
			userlogin.password = "";
			response.writeTextWithHeaderAndFooter(userlogin.toJson());
			
		} else {
			authenticator.removeUser();
			switch ( status ) {
				case STATUS_ACTIVE:
					response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.INVALID_COMBINATION, 
						ErrorMessages.getInstance().getMessage(request.getUser().getLocale(),
							ErrorCodes.INVALID_COMBINATION), true);
					status=STATUS_LOCK1;
					break;
				case STATUS_LOCK1:
					response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.MAXIMUM_INCORRECT_ATTEMPT,
						ErrorMessages.getInstance().getMessage(request.getUser().getLocale(),
							ErrorCodes.MAXIMUM_INCORRECT_ATTEMPT), true);
					status=STATUS_LOCK2;
					break;
				case STATUS_LOCK2:
					response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.SYSTEM_LOCKED, ErrorMessages.getInstance().getMessage(
						request.getUser().getLocale(), ErrorCodes.SYSTEM_LOCKED), true);
					status=STATUS_LOCK3; break;
				case STATUS_LOCK3:
					response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.INVALID_COMBINATION, ErrorMessages.getInstance().getMessage(
						request.getUser().getLocale(), ErrorCodes.INVALID_COMBINATION), true);
					status=STATUS_ACTIVE; break;
				default:
					response.setErrorCode(PROFILE_ERROR_TITLE,ErrorCodes.INVALID_COMBINATION, ErrorMessages.getInstance().getMessage(
						request.getUser().getLocale(), ErrorCodes.INVALID_COMBINATION), true);
					status=STATUS_LOCK1;
					System.err.println("Jombi User status:" + status);
			}
			UserLoginTableExt.activate(loginId, new String(new char[]{status}), null);
		}

	}

	private void logout(Request req, Response res)
	{
		req.setUser(UserProfile.getAnonymous());
		authenticator.removeUser();
	}
	
	private void getLoggedInUser(Request request, Response response)
	{
		UserProfile user = request.getUser();
		UserLogin userlogin = null;
		
		if (!user.isGuest())
		{
			try
			{
				userlogin = UserLoginTableExt.selectByLoginid(user.loginid);
			} 
			catch (SQLException ex)
			{
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.ERROR_RETREIVING_USER_PROFILE, ErrorMessages.getInstance().getMessage(user.getLocale(), ErrorCodes.ERROR_RETREIVING_USER_PROFILE), ex);
				return;
			}
		}
		if (userlogin != null)
		{
			userlogin.password = "";
			response.writeTextWithHeaderAndFooter(userlogin.toJson());
		}
		else
		{
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.UNKNOWN_USER, ErrorMessages.getInstance().getMessage(
				user.getLocale(), ErrorCodes.UNKNOWN_USER), true);
		}
	}

	private void changePassword(Request req, Response res)
	{
		UserProfile user = req.getUser();
		if ( user.isGuest()) {
			res.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.UNKNOWN_USER, ErrorMessages.getInstance().
				getMessage(user.getLocale(), ErrorCodes.UNKNOWN_USER), true);
			return;
		}
		
		req.mapData.put("loginid", user.loginid);
		String newPassword = req.getString("newpassword", true, true, false);
		String password = req.getString("password", true, true, false);
		UserLogin userlogin = this.setNewPassword(req, res, password, newPassword, true);
		if( null == userlogin) return;
		authenticator.removeUser();
	}

	private void resetPassword(Request req, Response res)
	{
		System.out.println("Reset Password not possile");
	}

	private UserLogin setNewPassword(Request request, Response response, String password, String newPassword, boolean matchOldPassword)
	{
		String loginId = request.getString("loginid", true, true, false);

		UserLogin userlogin;
		try
		{
			userlogin = UserLoginTableExt.selectByLoginid(loginId);
		} 
		catch (SQLException ex)
		{
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.ERROR_RETREIVING_USER_PROFILE, ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), ErrorCodes.ERROR_RETREIVING_USER_PROFILE), ex);
			return null;
		}

		if (userlogin == null) 
		{
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.INVALID_USER_ID, 
				ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), 
					ErrorCodes.INVALID_USER_ID), true);
			return null;
		}

		if (matchOldPassword)
		{
			String encodedPasswd = Hash.createHex(this.key, password);
			if (!encodedPasswd.equals(userlogin.password))
			{
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.OLD_PASSWORD_MISMATCH, 
					ErrorMessages.getInstance().getMessage(request.getUser().getLocale(),
							ErrorCodes.OLD_PASSWORD_MISMATCH), true);
				return null;
			}
		}
		
		String newpasswordHash = Hash.createHex(this.key, newPassword);
		userlogin.password = newpasswordHash;
		try
		{
			UserLoginTableExt.update(userlogin, null);
		} 
		catch (SQLException ex)
		{
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.PASSWORD_UPDATE_FAILED, ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), ErrorCodes.PASSWORD_UPDATE_FAILED), ex);
			return null;
		}
		userlogin.password = "";
		response.writeTextWithHeaderAndFooter(userlogin.toJson());
		return userlogin;
	}
	
	
	private void activateUser(Request request, Response response)
	{
		UserProfile user = request.getUser();
		Integer tenantId = user.getTenantId();
		try 
		{
			String id = request.getString("id", true, true, false);
			
			UserLogin userlogin = UserLoginTableExt.selectById(id);
			if (userlogin == null) {
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.INVALID_COMBINATION, 
					ErrorMessages.getInstance().getMessage(user.getLocale(), 
						ErrorCodes.INVALID_COMBINATION), true);
				return;
			}
			
			// * Restrict no of users for company
			if ( tenantId > 1)
			{
				int noOfUsers = UserLoginTableExt.getUsersCount(tenantId);
				if ( noOfUsers >= TenantLimit.getLimit(tenantId) )
				{
					response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.USER_LIMIT_REACHED,
						ErrorMessages.getInstance().getMessage(user.getLocale(), 
								ErrorCodes.USER_LIMIT_REACHED), true);
					return;
				}
			}
			UserLoginTableExt.activateUser(id, tenantId, null);
			response.writeTextWithHeaderAndFooter(Response.getError(false, ErrorCodes.QUERY_EXECUTION_SUCCESS, 
					ErrorMessages.getInstance().getMessage(user.getLocale(), ErrorCodes.QUERY_EXECUTION_SUCCESS), 
															"QUERY_SUCCESS", ErrorCodes.QUERY_KEY, "QUERY_SUCCESS"));

						
		}  catch (SQLException ex) {
			response.setErrorMessage("UNKNOWN_ERROR", ErrorCodes.UNKNOWN_ERRORS, ErrorMessages.getInstance().getMessage(user.getLocale(), ErrorCodes.UNKNOWN_ERRORS), ex);
		}
	}
	
	private void activate(Request req, Response response) {
		String loginId = req.getString("loginid", true, true, false);
		UserLogin userlogin = null;
		
		String incomingToken = req.getString("token", true, true, false);
		String expectingToken = Hash.createHex(this.key, loginId);
		if ( !expectingToken.equals(incomingToken) ) {
			response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.INVALID_TOKEN, 
				ErrorMessages.getInstance().getMessage(req.getUser().getLocale(), 
					ErrorCodes.INVALID_TOKEN), true);
			return;
		}
		
		try {
			userlogin = UserLoginTableExt.selectByLoginid(loginId);
			if (userlogin == null) {
				response.setErrorMessage(PROFILE_ERROR_TITLE, ErrorCodes.INVALID_COMBINATION, 
					ErrorMessages.getInstance().getMessage(req.getUser().getLocale(), 
						ErrorCodes.INVALID_COMBINATION), true);
				return;
			}
			UserLoginTableExt.activate(loginId, new String(new char[]{STATUS_ACTIVE}),  null);
			response.writeHeader();
			response.writeTextWithHeaderAndFooter(" { \"loginId\" : \" " + loginId + " \" } ");
			response.writeFooter();
		}  catch (SQLException ex) {
			response.setErrorMessage("UNKNOWN_ERROR",ErrorCodes.UNKNOWN_ERRORS, ErrorMessages.getInstance().getMessage(req.getUser().getLocale(), ErrorCodes.UNKNOWN_ERRORS), ex);
		}
	}	
	
	
	
	@Override
	public void init() 
	{
    	Configuration conf = ServiceFactory.getInstance().getAppConfig();
		this.key = conf.get("passwordkey","Jac!@3n dancias@##@ng the fEER%r haha!!#");
		this.isLoginVerification = conf.getBoolean("login.email.verification", false);
		try {
			authenticator = AuthenticatorFactory.getInstance().getAuthenticatorImpl();
		} catch (Exception ex) {
			LOG.fatal("Unable to instantiate authenticator", ex);
		}

	}
	
	@Override
	public String getName() {
		return "user";
	}	
}
