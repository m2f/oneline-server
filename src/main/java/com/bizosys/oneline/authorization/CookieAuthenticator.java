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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.user.UserProfile;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.StringUtils;

public class CookieAuthenticator implements IAuthenticate
{
	protected static final String COOKIE_LOGINID = "lid";
	protected static final String COOKIE_USERID = "uid";
	protected static final String COOKIE_HEXDIGEST = "hex";

	private static final Logger LOG = LogManager.getLogger(CookieAuthenticator.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

	private String subDomain = StringUtils.Empty;
	private int credentialsExpireInSeconds = 0;
	
	public ThreadLocal<HttpServletRequest> request;
	public ThreadLocal<HttpServletResponse> response;

	public CookieAuthenticator()
	{
		this.init();
    	this.request = new ThreadLocal<HttpServletRequest>();
    	this.response = new ThreadLocal<HttpServletResponse>();
	}
	
    private void init()
	{
    	Configuration conf = ServiceFactory.getInstance().getAppConfig();
		this.subDomain = conf.get("subdomain", "");
		this.credentialsExpireInSeconds = conf.getInt("credential_expire_seconds", (24 * 60 * 60)); // 1 day
		
		if ( DEBUG_ENABLED) 
		{
			StringBuilder sb = new StringBuilder(100);
			sb.append("this.subDomain:").append(this.subDomain);
			sb.append(". this.credentialsExpireInSeconds:").append(this.credentialsExpireInSeconds);
			LOG.debug(sb.toString());
			sb.delete(0, sb.capacity());
		}
	}

    @Override
    public void store(HttpServletRequest request, HttpServletResponse response)
    {
    	this.request.set(request);
    	this.response.set(response);
    }
    
    @Override
    public void clear()
    {
    	this.request.remove();
    	this.response.remove();
    }
    
    @Override
	public UserProfile getUser( HttpServletRequest request, HttpServletResponse response)
	{
		UserProfile user = this.getUser(request);
		if (user == null) return UserProfile.getAnonymous();

		String browserKey = this.buildBrowserKey(request, user);
		return (MD5HashComparator.getInstance().isDigestValid(browserKey, user.hexdigest)) ? user: UserProfile.getAnonymous();
	}
	
	public void setUser(UserProfile user, HttpServletRequest request,	HttpServletResponse response, boolean isRemember) 
	{
		String browserKey = this.buildBrowserKey(request, user);

		if ( DEBUG_ENABLED ) {
			LOG.debug("browserKey:" + browserKey);
		}
		
		user.hexdigest = MD5HashComparator.getInstance().create(browserKey);

		if ( DEBUG_ENABLED ) {
			LOG.debug("Setting user:" + user.loginid+ " with browserKey: + " + browserKey + " and digest:" + user.hexdigest);
		}
		
		if( isRemember ) this.storeInCookie(user, response, this.credentialsExpireInSeconds);
		else this.storeInCookie(user, response, 0);
	}
	
	public void setUser(UserProfile user, boolean isRemember) 
	{
		if (this.request == null || this.response == null) return;
		HttpServletRequest request = this.request.get();
		HttpServletResponse response = this.response.get();
		if (request == null || response == null) return;
		this.setUser(user, request, response, isRemember);
	}

	public void setUser(UserProfile user) 
	{
		if (this.request == null || this.response == null) return;
		HttpServletRequest request = this.request.get();
		HttpServletResponse response = this.response.get();
		if (request == null || response == null) return;
		this.setUser(user, request, response, false);
	}

	public void removeUser()
	{
		UserProfile user = UserProfile.GUEST_PROFILE;
		String browserKey = this.buildBrowserKey(this.request.get(), user);
		user.hexdigest = MD5HashComparator.getInstance().create(browserKey);
		this.removeCookies(user, this.response.get());
	}
	
    /**
     * Private Methods
     */
    
	private String buildBrowserKey(HttpServletRequest request, UserProfile user) {
		StringBuilder sb = new StringBuilder(100);
		String profile = ( null == user) ? "guest" : user.getProfile();
		String loginId = ( null == user) ? "anonymous" : user.loginid;
		sb.append(profile).append(':').append(loginId).append(':').append(request.getRemoteAddr());
		return sb.toString();
	}


	
	/**
	 * This is constructed from bizosys stamped cookies
	 * This is valid for 8 hours
	 * 
	 * If we don't find in our cookie, check it with SSO service
	 * 
	 * @param request
	 * @return
	 */
	private UserProfile getUser(HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0)
		{
			if ( DEBUG_ENABLED ) LOG.debug("Got cookies count :" + cookies.length);
			UserProfile user = new UserProfile();
			for (Cookie cookie : cookies)
			{
				if ( DEBUG_ENABLED ) LOG.debug("Cookie - " + cookie.getName() + " : " + cookie.getValue());
				if (COOKIE_LOGINID.equals(cookie.getName()))
				{
					user.loginid = this.decodeCookieValue(cookie);
				}
				else if (COOKIE_USERID.equals(cookie.getName()))
				{
					user.setProfile(this.decodeCookieValue(cookie));
				}
				else if (COOKIE_HEXDIGEST.equals(cookie.getName()))
				{
					user.hexdigest = this.decodeCookieValue(cookie);
				}
			}
			return user;
		}
		if ( DEBUG_ENABLED ) LOG.debug("Did not get cookies.");
		return null;
	}

	private String decodeCookieValue(Cookie cookie) 
	{
		try 
		{
			return URLDecoder.decode(cookie.getValue(), "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			return cookie.getValue();
		}
	}

	private void storeInCookie(UserProfile user, HttpServletResponse response, int expiryTime)
	{
		this.addCookie(COOKIE_LOGINID, user.loginid, response, expiryTime);
		this.addCookie(COOKIE_USERID, user.getProfile(), response, expiryTime);
		this.addCookie(COOKIE_HEXDIGEST, user.hexdigest, response, expiryTime);
	}

	private void removeCookies(UserProfile user, HttpServletResponse response)
	{
		this.addCookie(COOKIE_LOGINID, user.loginid, response, 1);
		this.addCookie(COOKIE_USERID, user.getProfile(), response, 1);
		this.addCookie(COOKIE_HEXDIGEST, user.hexdigest, response, 1);
	}

	private void addCookie(String key, String value, HttpServletResponse response, int expiryInSeconds)
	{
		if (DEBUG_ENABLED) LOG.debug("Storing cookie: "+ key + ":" + value + " for " + expiryInSeconds  + " seconds");
		String encodedValue = value;
		try 
		{
			encodedValue = URLEncoder.encode(value, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			encodedValue = value;
		}
		Cookie cookie = new Cookie(key, encodedValue);
		cookie.setPath("/");
		if ( ! StringUtils.isEmpty(this.subDomain)) cookie.setDomain(this.subDomain); 
		if ( 0 != expiryInSeconds) cookie.setMaxAge(expiryInSeconds);
		response.addCookie(cookie);
	}

	/**
	private void resetCookie(HttpServletResponse res, Request sensorReq) {
		Cookie hexCookie = new Cookie("HEXDIGEST", "");
		res.addCookie(hexCookie);
	}
	*/

	

}