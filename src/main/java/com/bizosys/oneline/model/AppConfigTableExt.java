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

package com.bizosys.oneline.model;

import java.sql.SQLException;
import java.util.List;

import com.bizosys.oneline.dao.ReadObject;

public class AppConfigTableExt extends AppConfigTable
{
	private static String sqlSelectByTitle = sqlSelect + " where title = ?";
	private static String sqlSelectByTypeAndTitle = sqlSelect + " where doctype = ? and title = ?";
	private static String sqlSelectByParentIdAndType = sqlSelect + " where parentid = ? and doctype = ?";
	private static String sqlSelectByid = sqlSelect + " where id = ?";
	
	private AppConfigTableExt() {
	}

	public static AppConfig selectByTitle(Object title) throws SQLException 
	{
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		List<AppConfig> listL = ro.execute(sqlSelectByTitle, new Object[]{title});
		if (listL == null || listL.isEmpty()) return null;
		return listL.get(0);
	}

	public static AppConfig selectByTypeAndTitle(String type, String title) throws SQLException 
	{
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		List<AppConfig> listL = ro.execute(sqlSelectByTypeAndTitle, new Object[]{type, title});
		if (listL == null || listL.isEmpty()) return null;
		return listL.get(0);
	}

	public static AppConfig selectByParentIdAndType(Integer id, String type) throws SQLException 
	{
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		List<AppConfig> listL = ro.execute(sqlSelectByParentIdAndType, new Object[]{id, type});
		if (listL == null || listL.isEmpty()) return null;
		return listL.get(0);
	}

	public static List<AppConfig> selectListByParentIdAndType(Integer id, String type) throws SQLException 
	{
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		return ro.execute(sqlSelectByParentIdAndType, new Object[]{id, type});
	}

	public static AppConfig selectById( Object id, @SuppressWarnings("rawtypes") Class classToFill) throws SQLException {
		ReadObject<AppConfig> ro = new ReadObject<AppConfig>(clazz);
		AppConfig record = ro.selectByPrimaryKey(sqlSelectByid, id);
		if ( null == record) return null;
		return record;
	}

}
