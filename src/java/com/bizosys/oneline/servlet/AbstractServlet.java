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
package com.bizosys.oneline.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.bizosys.oneline.authorization.AuthenticatorFactory;
import com.bizosys.oneline.authorization.IAuthenticate;
import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.user.UserProfile;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.Hash;
import com.bizosys.oneline.util.ServletUtil;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.InvalidRequestException;
import com.bizosys.oneline.web.sensor.Request;
import com.bizosys.oneline.web.sensor.Response;
import com.bizosys.oneline.web.sensor.Sensor;

public abstract class AbstractServlet extends HttpServlet {

	private static final int SC_EXPECTATION_FAILED = HttpServletResponse.SC_EXPECTATION_FAILED;
	protected static final long serialVersionUID = 4L;
	protected final static Logger LOG = Logger.getLogger(AbstractServlet.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	private static final boolean INFO_ENABLED = LOG.isInfoEnabled();

	private String key = Hash.KEY_VALUE_DEFAULT;
	private Set<String> captchaUrls = null;
	private IAuthenticate authenticator = null;

	@Override
	public void init(ServletConfig config) throws ServletException 
	{
		LOG.info("Initializing AbstractServlet...");
		super.init(config);
    	Configuration conf = ServiceFactory.getInstance().getAppConfig();
		this.key = conf.get(Hash.KEY_NAME,Hash.KEY_VALUE_DEFAULT);
		
		try {
			authenticator = AuthenticatorFactory.getInstance().getAuthenticatorImpl();
		} catch (Exception ex) {
			LOG.fatal("Unable to instantiate authenticator", ex);
		}
		
		/**
		 * Parse all the Urls which require the captcha verification.
		 */
		String captchUrlLine = conf.get("captchaurls");
		if ( ! StringUtils.isEmpty(captchUrlLine)) {
			List<String> sensorActionPairs = StringUtils.fastSplit(captchUrlLine, ',');
			if ( null != sensorActionPairs) {
				captchaUrls = new HashSet<String>();
				for (String sensorAction : sensorActionPairs) {
					LOG.info("Captcha Enable Url :" + sensorAction);
					captchaUrls.add(sensorAction);
				}
			}
		}
		LOG.info(">>>AbstractServlet Initialized....");
	}

	protected void setupSensor(Sensor sensor, String sensorId)
	{
		sensor.init();
		SensorFactory.sensorM.put(sensorId, sensor);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {
		this.doProcess(req, res);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {
		this.doProcess(req, res);
	}
	
	private void doProcess(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

		Object userO  = req.getAttribute("__user");
		UserProfile user = ( null != userO ) ? (UserProfile) userO : UserProfile.getAnonymous() ;
		
		res.setContentType("text/html");
		res.setCharacterEncoding (req.getCharacterEncoding() );
		res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
		res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		res.setDateHeader("Expires", 0); // Proxies.
		
		if ( INFO_ENABLED ) LOG.info("\n\n A web request has entered server.\n");

		/**
		 * Store all the parameters in the Sensor request object
		 */
		Map<String, String> data = ServletUtil.extractQueryElements(req);
		String sensorId = data.get("service");
		String action = data.get("action");
		sensorId = (null == sensorId) ? StringUtils.Empty : sensorId.trim();
		action = (null == action) ? StringUtils.Empty : action.trim();
		
		if ( INFO_ENABLED ) LOG.info("Service [ " + sensorId + " ] , Action [ " + action + " ]");
		
		if ( (sensorId.length() == 0) || ( action.length() == 0 )) {
			String errorMsg =  StringUtils.FatalPrefix + "Sensor [" + sensorId + "] or action [" + action + "] are missing." ; 
			LOG.warn(errorMsg);
			sendErrorHttp(HttpServletResponse.SC_BAD_REQUEST, req, res, errorMsg);
			return;
		}

		Request sensorReq = new Request(sensorId, action, data);
		sensorReq.clientIp = req.getRemoteAddr();
		sensorReq.serverName = req.getServerName();
		
		setUser(req, sensorReq);

		String format = data.get("format");
		String contentType = null;
		String downloadFile = null;
		boolean isBinaryData = false;

		if ( StringUtils.isEmpty(format)) {
			
			contentType = ("text/html");
			format = StringUtils.Empty;
			
		} else if ( "jsonp".equals(format) ){
		
			contentType = ("application/json");
            format = "jsonp";

		} else if ( "xls".equals(format )  ){
			
			contentType = ("application/vnd.ms-excel");
			downloadFile = data.get("filename");
			if ( StringUtils.isEmpty(downloadFile))  downloadFile = new Date().getTime() + ".xls";
			isBinaryData = true;
	    	
		} else if ( "xlsx".equals(format) ){

			contentType = ("application/vnd.ms-excel");
			downloadFile = data.get("filename");
			if ( StringUtils.isEmpty(downloadFile))  downloadFile = new Date().getTime() + ".xlsx";
			isBinaryData = true;
	    	
		} else if ( "csv".equals(format) ){
			contentType = ("application/CSV");
			format = "csv";
        }
		else {
			contentType = ("text/xml");
			format = "xml";
		}

		
		/**
		 * Initiate the sensor response, putting the stamp on it and xsl. 
		 */
		PrintWriter pw = (isBinaryData ) ? null : res.getWriter();
		OutputStream os = (isBinaryData ) ? res.getOutputStream() : null ;
		
		Response sensorRes = new Response(pw,os);
		sensorRes.format = format;
		if ( null != contentType ) res.setContentType(contentType);
    	if ( null != downloadFile ) res.setHeader("Content-Disposition", "attachment; filename=" + downloadFile);

		String callback = data.get("callback");
		sensorRes.callback = (null == callback) ? StringUtils.Empty : callback;
		
		try 
		{
			/**
			 * Check if this request need a captcha checkup. 
			 */
			if ( null != pw ) {
				boolean captchaVerified = verifyCaptcha(req, sensorReq, sensorRes, pw);
				if ( !captchaVerified) return;
			}
			
			Sensor sensor = SensorFactory.getSensor(sensorId);

			if (sensor == null) {
			
				/**
				 * Sensor is not set
				 */
				if ( null == pw ) {
					res.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					String locale = ( null == user ) ? "en" : ( user.getLocale() == null ) ? "en" : user.getLocale(); 
					String sensorNotFound = 
						ErrorMessages.getInstance().getMessage(locale, ErrorCodes.PROCESSING_ENDPOINT_FAILURE) + " " + sensorId;
					LOG.warn(sensorNotFound);
					sendErrorHttp(HttpServletResponse.SC_NOT_FOUND, req, res, sensorNotFound);
				}
				return;

			} 

			if ( DEBUG_ENABLED ) LOG.debug("Sensor processing START");
				
			sensor.processRequest(sensorReq, sensorRes);

			if ( DEBUG_ENABLED ) LOG.debug("Sensor processing END");

			if ( sensorRes.isError) {
				if ( null == pw ) {
					res.setStatus(SC_EXPECTATION_FAILED);
				} else {
					
					sendErrorHttp(HttpServletResponse.SC_SEE_OTHER, format, 
						req, res, sensorRes);

				}
			}
			else
			{
				pw.write(sensorRes.getOutData());
			}
			
		} catch (InvalidRequestException ex) {
			
			if ( null != pw ) {
				String locale = ( null == user ) ? "en" : ( user.getLocale() == null ) ? "en" : user.getLocale(); 
				String invalidParameters = ErrorMessages.getInstance().getMessage( locale, ErrorCodes.INVALID_PARAMETERS + " " + ex.errorField);
				if ( DEBUG_ENABLED ) LOG.debug("Improper request input : " + invalidParameters, ex);
				sendErrorHttp(HttpServletResponse.SC_SEE_OTHER, format, req, res, invalidParameters);
				
			} else res.setStatus(SC_EXPECTATION_FAILED);
			return;
			
			
		} catch (Exception ex) {

			boolean hasError = ( null == sensorRes) ? false : sensorRes.isError; 
				
			if ( hasError) 
			{
				if ( null != pw ) {
					res.setStatus(SC_EXPECTATION_FAILED);
					sendErrorHttp(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, format, req, res, sensorRes.getError("xml".equals(format)));
				} else res.setStatus(SC_EXPECTATION_FAILED);
				return;
				
			} else {
				if ( null != pw ) {
					String errorMsg = ErrorMessages.getInstance().getMessage(
							user.getLocale(), ErrorCodes.UNKNOWN_ERRORS) + "\n" + ex.getMessage();
							
					String unknownError = Response.getError("xml".equals(format),ErrorCodes.UNKNOWN_ERRORS, errorMsg, "UNKNOWN_ERROR", ErrorCodes.QUERY_KEY, null);
					sendErrorHttp(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, format, req, res, unknownError);
				} else res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
				LOG.error("Error in processing request", ex);
				return;
			}
		
		} finally {
			if ( null != pw) {
				pw.flush();
				pw.close();
			} else if ( null != os) {
				os.flush();
				os.close();
			}
		}
	}

	/**
	 * Set the user object in the sensor request.
	 * @param req
	 * @param sensorReq
	 */
	private void setUser(HttpServletRequest req, Request sensorReq) {
		Object userObject = req.getAttribute("__user");
		if ( null == userObject) 
		{
			sensorReq.setUser(UserProfile.getAnonymous());
		} 
		else 
		{
			sensorReq.setUser((UserProfile) userObject);
		}
	}

	/**
	 * Verify the captch for the enabled urls
	 * @param req
	 * @param res
	 * @param out
	 * @return
	 */
	public boolean verifyCaptcha(HttpServletRequest servletReq, Request req, Response res, PrintWriter out)
	{
		if ( null == captchaUrls) return true;
		
		UserProfile user = req.getUser();
		
		StringBuilder sb = new StringBuilder(24);
		sb.append(req.sensorId).append('.').append(req.action);
		if ( ! captchaUrls.contains(sb.toString())) return true;
		
		Map<String, String> params = req.mapData;

		String readcaptcha = null;
		String encodedCaptcha = null;
		if ( params.containsKey("readcaptcha") ) 
			readcaptcha = req.getString("readcaptcha", true, true, false);

		if ( params.containsKey("encodedcaptcha") ) 
			encodedCaptcha = req.getString("encodedcaptcha", true, true, false);
		else {
			if ( StringUtils.isEmpty(encodedCaptcha)) {
				Cookie[] cookies = servletReq.getCookies();
				if (cookies != null && cookies.length > 0) {
					for (Cookie cookie : cookies) {
						if ( cookie.getName().equals("encodedcaptcha")) {
							encodedCaptcha = cookie.getValue(); 
							break;
						}
					}
				}
			}
		}
		
		String captchaTextEncoded = null;
		if ( null != readcaptcha && null != encodedCaptcha) {
			String secureCaptchaText = servletReq.getRemoteAddr() + readcaptcha;
			captchaTextEncoded = Hash.createHex(this.key, secureCaptchaText);
			if (captchaTextEncoded.equals(encodedCaptcha)) return true;
		}

		res.setErrorCode("CAPTCHA_ERROR", ErrorCodes.CAPTCHA_MISMATCH, user.getLocale(), true);
		return false;
	}
	
	public static void sendErrorHttp(HttpServletRequest req, HttpServletResponse res, String message) throws IOException {
		sendErrorHttp(HttpServletResponse.SC_EXPECTATION_FAILED, req, res, message); 
	}

	public static void sendErrorHttp(int httpStatusCode, HttpServletRequest req, HttpServletResponse res, String message) throws IOException {
		String format = req.getParameter("format");
		if ( null == format) {
			Object formatO = req.getAttribute("format");
			if ( null == formatO) {
				format = "jsonp";
			} else {
				format = formatO.toString();
			}
		}
		sendErrorHttp(httpStatusCode, format, req, res, message);
	}

	public static void sendErrorHttp(int httpStatusCode, String format, HttpServletRequest req, HttpServletResponse res, String message) throws IOException {
		PrintWriter pw = res.getWriter(); 
		res.setStatus(httpStatusCode);
		pw.write(message);
		pw.flush();
		pw.close();
	}

	public static void sendErrorHttp(int httpStatusCode, String format, 
			HttpServletRequest req, HttpServletResponse res, Response sensorRes) throws IOException {
		
		PrintWriter pw = res.getWriter();
		
		if ( sensorRes.errorCode.equals(ErrorCodes.DB_CONNECTION_ERROR) ) res.setStatus(SC_EXPECTATION_FAILED);
		
		sensorRes.writeHeader();
		String message = sensorRes.getError("xml".equals(format), sensorRes.skipErrorRewrite );

		if ( sensorRes.skipErrorRewrite )
			pw.write( message );
		else 
			pw.write( "[" + message + "]" );
		
		sensorRes.writeFooter();
		pw.flush();
		pw.close();
	}

}