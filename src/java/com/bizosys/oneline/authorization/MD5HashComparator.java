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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.util.Configuration;
import com.bizosys.oneline.util.Hash;
import com.bizosys.oneline.util.StringUtils;

public class MD5HashComparator {
	
	public static MD5HashComparator instance;
	
	public static MD5HashComparator getInstance() {
		if ( null != instance) return instance;
		synchronized (MD5HashComparator.class.getName()) {
			if ( null != instance) return instance;
			instance = new MD5HashComparator();
		}
		return instance;
	}

	private static final Logger LOG = Logger.getLogger(MD5HashComparator.class);
	
	private Map<String, String> cachedEncodings = new HashMap<String, String>();    
	private String key = Hash.KEY_VALUE_DEFAULT;

	private MD5HashComparator() {
    	Configuration conf = ServiceFactory.getInstance().getAppConfig();
		this.key = conf.get(Hash.KEY_NAME,Hash.KEY_VALUE_DEFAULT);
	}
	
	public String create(String identifier) {
		return Hash.createHex(this.key, identifier);		
	}
	
	public boolean isTokenValid(String clientName, String clientTokenId) {
		return isDigestValid(clientName, clientTokenId);
	}
	
	/**
	 * HexDigest is checked against the local cache as follows.
	 * 1. If browser did not send hexdigest then this request is not authenticated.
	 * 2. If browser has sent and matches the digest against local cache it is good.
	 * 3. If browser has sent it but local cache does not have it, regenerate and match. 
	 * @param browserDigest
	 * @return
	 */
	public boolean isDigestValid(String browserKey, String browserDigest) 
	{
		if (StringUtils.isEmpty(browserDigest))	return false; //Browser did not send the digest
		
		if (this.cachedEncodings.containsKey(browserKey) && browserDigest.equals(this.cachedEncodings.get(browserKey))) return true; //Digests match

		String localDigest = Hash.createHex(this.key, browserKey);	
		if (browserDigest.equals(localDigest)) {
			this.cachedEncodings.put(browserKey, localDigest);
			return true;
		}
        LOG.warn("CookieSession > Authentication digest seem to be corrupted or compromised. Browser sent a digest and did not match with local digest for key:" + browserKey);
		return false;
	}	
	
	
}
