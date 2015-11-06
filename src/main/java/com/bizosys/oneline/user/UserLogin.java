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


public class UserLogin {

	public Integer id;
	public Integer tenantid;
	public String active;
	public String loginid;
	public String password;
	public String profile;
	public java.sql.Timestamp touchtime;

	/** Default constructor */
	public UserLogin() {
	}


	/** Constructor with primary keys (Insert with primary key)*/
	public UserLogin(Integer id,Integer tenant, String active,
			String loginid,String password, String profile) {

		this.id = id;
		this.tenantid = tenant;
		this.active = active;
		this.loginid = loginid;
		this.password = password;
		this.profile = profile;

	}


	/** Constructor with Non Primary keys (Insert with autoincrement)*/
	public UserLogin(Integer tenant, String active,String loginid,String password,String profile) {

		this.tenantid = tenant;
		this.active = active;
		this.loginid = loginid;
		this.password = password;
		this.profile = profile;

	}


	/** Params for (Insert with autoincrement)*/
	public Object[] getNewPrint() {
		return new Object[] {
			tenantid, active, loginid, password, profile
		};
	}


	/** Params for (Insert with primary key)*/
	public Object[] getNewPrintWithPK() {
		return new Object[] {
			id, tenantid, active, loginid, password, profile
		};
	}


	/** Params for (Update)*/ 
	public Object[] getExistingPrint() {
		return new Object[] {
			tenantid, active, loginid, password, profile, id
		};
	}
	
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<UserLogin>");
		sb.append("<tenantid>").append(tenantid).append("</tenantid>");
		sb.append("<id>").append(id).append("</id>");
		sb.append("<active>").append(active).append("</active>");
		sb.append("<loginid>").append(loginid).append("</loginid>");
		sb.append("<profile>").append(profile).append("</profile>");
		sb.append("</UserLogin>");
		return sb.toString();
	}
	
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"UserLogin\":{\"id\":").append(id).
		append(",\"tenantid\":").append(tenantid).append(",\"active\":\"");
		sb.append(active).append("\",\"loginid\":\"");
		sb.append(loginid).append("\",\"profile\":\"");
		sb.append(profile).append("\"}}");
		return sb.toString();
	}
}

