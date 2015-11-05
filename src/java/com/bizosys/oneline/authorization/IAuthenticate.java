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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bizosys.oneline.user.UserProfile;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.StringUtils;


public interface IAuthenticate {
	void setUser(UserProfile user) throws ErrorCodeExp;
	void setUser(UserProfile user, boolean rememberme) throws ErrorCodeExp;
	void store(HttpServletRequest request, HttpServletResponse response) throws ErrorCodeExp;
	UserProfile getUser(HttpServletRequest request, HttpServletResponse response)throws ErrorCodeExp;
	void removeUser();
	void  clear();
}
