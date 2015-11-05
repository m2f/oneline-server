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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.bizosys.oneline.util.StringUtils;

public class UserProfile 
{
	public static final String ANY = "all";
	public static final String GUEST = "guest";
	private static final String USER_NAME = "user_display_name";
	private static final String TENANT_ID = "tenant";
	private static final String ROLE = "role";
	private static final String LOCALE = "locale";
	
	private static final String COMAPNY_NAME = "company";
	
	public String loginid = StringUtils.Empty;
	public String hexdigest = StringUtils.Empty;
	
	private Map<String, String> profile = new HashMap<String, String>();
	private boolean isGuest = false;
	
	
	
	public static UserProfile GUEST_PROFILE;
	
	static
	{
		GUEST_PROFILE = new UserProfile(GUEST, "role|"+ GUEST);
		GUEST_PROFILE.isGuest = true;
	}
	
	public UserProfile() 
	{
	}

	public UserProfile(String loginid, String profile) 
	{
		this.loginid = loginid;
		setProfile(profile);
	}

	public boolean isGuest()
	{
		return this.isGuest;
	}
	
	public static UserProfile getAnonymous()
	{
		return GUEST_PROFILE;
	}
	
	public static String getProfile(int tenantId, String companyName) {
		return TENANT_ID + "|" + tenantId + "," + COMAPNY_NAME + "|" + companyName;		
	}
	
	
	public String getProfile() {
		boolean isFirstTime = true;
		StringBuilder aclSB = new StringBuilder();
		for (String key : profile.keySet()) {
			if ( isFirstTime ) {
				isFirstTime = false;
			} else {
				aclSB.append(',');
			}
			aclSB.append(key).append('|').append(profile.get(key));
		}
		return aclSB.toString();
	}
	
	public String getProfile(String key) {
		return this.profile.get(key);
	}	

	public void setProfile(String strProfile) {
		
		if ( null == strProfile) return;
		if ( strProfile.length() == 0 ) return;
		
		int phraseIndexStart = 0;
		int phraseIndexEnd = 0;
		while ( true ) {
			phraseIndexEnd = strProfile.indexOf(',', phraseIndexStart);
			boolean isLast = ( phraseIndexEnd == -1 );
			
			String phase = ( phraseIndexEnd == -1 )  ? 
				strProfile.substring(phraseIndexStart) :
				strProfile.substring(phraseIndexStart, phraseIndexEnd);
			phraseIndexStart = phraseIndexStart + phase.length() + 1;
			
			int keyCut = phase.indexOf('|');
			if( -1 == keyCut) {
				System.err.println("WARN: Bad user profile phrase: [" + phase + "]");
				break;
			}
			String key = phase.substring(0, keyCut);
			String val = phase.substring(keyCut+1);			
			this.profile.put(key, val);
			
			if ( isLast ) break;
			
		}
	}
	
	public String getUserName() {
		String userName = getProfile(USER_NAME);
		if ( null == userName) userName = "" ;
		return userName;
	}
	
	public Integer getTenantId() {
		String tenantId = getProfile(TENANT_ID);
		if ( null == tenantId) return -1 ;
		return Integer.parseInt(tenantId);
	}
	
	public String getRole() {
		return this.profile.get(ROLE);
	}	

	public void setRole(String value)
	{
		this.profile.put(ROLE, value);
	}
	
	public String getLocale() {
		String locale = getProfile(LOCALE);
		if ( null == locale) return Locale.ENGLISH.getLanguage();
		return locale;
	}
	
	
	public String toString()
	{
		return getProfile();
	}

	public static void main(String[] args) {
		UserProfile profile = new UserProfile("nainika.thakur@bizosys.com",
			"role|admin,user_display_name|Nainika,locale|fr");
		System.out.println(profile.getProfile("user_display_name"));
		System.out.println(profile.getProfile("role"));
		System.out.println(profile.getProfile("locale"));
		
		System.out.println(profile.toString() );
	}
}
