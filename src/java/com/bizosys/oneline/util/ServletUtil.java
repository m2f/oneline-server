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

package com.bizosys.oneline.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class ServletUtil {

	protected final static Logger LOG = Logger.getLogger(ServletUtil.class);
	public static Map<String, String> extractQueryElements(HttpServletRequest req) throws IOException {

		Map<String, String> data = new HashMap<String, String>();

		@SuppressWarnings("rawtypes")
		Enumeration reqKeys = req.getParameterNames();
		while (reqKeys.hasMoreElements()) {
			String key = (String) reqKeys.nextElement();
			String value = req.getParameter(key);
			data.put(key, value);
		}
		
		reqKeys = req.getAttributeNames();
		while (reqKeys.hasMoreElements()) {
			String key = reqKeys.nextElement().toString();
			String value = req.getAttribute(key).toString();
			data.put(key, value);
		}
		
		BufferedReader bodyReader = req.getReader();
		try {
			int count = 0;
			if(null != bodyReader)
			{
				char[] buffer = new char[1024];
				StringBuilder sb = new StringBuilder(1024);
				while (-1 != ( count = bodyReader.read(buffer) )) {
					sb.append(buffer,0,count);
				}
				if(sb.length() == 0) return data;
				
				//String decodedBody = URLDecoder.decode(sb.toString(), "UTF-8");
				String decodedBody = sb.toString(); // assign raw text as is
				int index = decodedBody.indexOf("query=");
				if(index > -1){
					decodedBody = decodedBody.substring(index+6);
					req.setAttribute("query", decodedBody);
					data.put("query", decodedBody);
				}
			}
		} catch (Exception e) {
			String msg = "Error parsing query details from request " + e.getMessage();
			LOG.fatal(msg);
			throw new IOException(msg);
		}
		
		return data;
	}	
}
