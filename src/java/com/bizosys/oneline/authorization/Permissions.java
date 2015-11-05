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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bizosys.oneline.sql.SqlSensor;
import com.bizosys.oneline.user.UserProfile;

public class Permissions implements IAuthroize {
	
	private final static Logger LOG = Logger.getLogger(SqlSensor.class);
	
	private Map<String, String> perms = null;
	
	public String getPerms(String role, String objectType, String objectName) throws IOException {
		if ( null == perms) build();
		return perms.get(getCompisiteKey(role,objectType,objectName));
	}

	@Override
	public synchronized void build() throws IOException {
		Map<String, String> newperms = new HashMap<String, String>();
		List<Authorization> allrows = null;
		try {
			allrows = AuthorizationTable.selectAll();
		} catch (SQLException ex) {
			LOG.error("Error while loading permissions", ex);
			allrows = new ArrayList<Authorization>();
		}
		StringBuilder sb  = new StringBuilder(92);
		for (Authorization aAuth : allrows) {
			String key = getCompisiteKey(aAuth.rolename,aAuth.objecttype,aAuth.objectname);
			newperms.put(key, aAuth.permission);
			sb.setLength(0);
		}
		perms = newperms;
	}
	
	private String getCompisiteKey(String rolename, String objecttype, String objectname) {
		StringBuilder sb = new StringBuilder(64);
		return getCompisiteKey(sb, rolename, objecttype, objectname);
	}

	private String getCompisiteKey(StringBuilder sb, String rolename, String objecttype, String objectname) {
		return sb.append(rolename).append("\t").append(objecttype).append("\t").append(objectname).toString();
	}
	
	@Override
	public boolean isAuthorized(UserProfile user, String objectType, 
		String objectName) throws IOException {

		String allRoles = getPerms(UserProfile.ANY,objectType,objectName);
		allRoles = ( null == allRoles) ? ":" : allRoles.trim();
		int allRolesLen = allRoles.length();
		if ( allRolesLen > 0 ) {
			char execSet = allRoles.charAt(allRolesLen - 1);
			if ( execSet == 'x') return true;
		}
		
		String role = ( null == user) ? UserProfile.GUEST : user.getRole();
		String rolePerm = getPerms(role,objectType,objectName);
		rolePerm = ( null == rolePerm) ? "" : rolePerm;
		int rolePermLen = rolePerm.length();
		if ( rolePermLen > 0 ) {
			char execSet = rolePerm.charAt(rolePermLen - 1);
			if ( execSet == 'x') return true;
		}
		if ( LOG.isDebugEnabled()) LOG.debug("role\t" + role + "\n" + rolePerm + "\t" + rolePerm);
		return false;
	}
	
}
