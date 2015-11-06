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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class JdbcTestMysql {

	Connection conn = null;
	public static void main(final String[] args) throws Exception {
		JdbcTestMysql t = new JdbcTestMysql();
		t.testSimpleStatement();
	}

	public final void testSimpleStatement() throws Exception {

		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://216.241.82.117/testdb","root","root");

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select * from testdb";
			stmt = conn.prepareStatement(sql);
			boolean isExecuted = stmt.execute();
			System.out.println(isExecuted);
			if( isExecuted ) {
				do {
					rs = stmt.getResultSet();
					ResultSetMetaData md = rs.getMetaData();
					int totalCol = md.getColumnCount();
					while(rs.next()){
						for ( int i=0; i<totalCol; i++ ) {
							Object obj = rs.getObject(i+1);
							if ( null == obj) continue;
							System.out.print(obj.toString() + "   |   ");
						}
						System.out.println("");
					}
					System.out.println("------------------------------------------------");
				} while (stmt.getMoreResults());
			}
		}catch(SQLException se){
			System.out.println(se.getMessage());
			se.printStackTrace();
		}finally{
			if(null != rs)try{rs.close();}catch (Exception e) {};
			if(null != stmt)try{stmt.close();}catch (Exception e) {};
		}
	}

}
