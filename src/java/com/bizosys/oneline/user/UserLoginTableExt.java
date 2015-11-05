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
import java.util.List;

import com.bizosys.oneline.dao.ReadScalar;
import com.bizosys.oneline.dao.WriteBase;

public class UserLoginTableExt extends UserLoginTable
{
	private static String sqlUpdate =
		"update user_login SET active = ? where loginid = ?";
	
	public UserLoginTableExt() {
	}
	
	public static void activate( String loginId, String status, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) sqlWriter = new WriteBase();
		sqlWriter.execute(sqlUpdate, new Object[] {status, loginId});
	}
	
	
	protected static String sqlUserCount = " select count(id) from user_login where active = 'Y' and tenantid = ?";
	public static int getUsersCount (Object tenantId) throws SQLException
	{
		List<Object> found = new ReadScalar().execute(sqlUserCount, new Object[] {tenantId});
		if( null == found ) return 0;
		return  ( ( null == found.get(0) ) ? 0 :  Integer.parseInt(found.get(0).toString()) );
	}
	
	
	private static String sqlActivateUser =
			"UPDATE user_login SET active='Y' WHERE id=? and tenantid = ?";
	
	public static void activateUser( Object id, Object tenantId, WriteBase sqlWriter) throws SQLException {
		if ( sqlWriter == null ) sqlWriter = new WriteBase();
		sqlWriter.execute(sqlActivateUser, new Object[] {id, tenantId});
	}
	
}
