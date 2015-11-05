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

package com.bizosys.oneline.servlet;

import org.apache.log4j.Logger;

import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.web.sensor.Request;
import com.bizosys.oneline.web.sensor.Response;
import com.bizosys.oneline.web.sensor.Sensor;

public class UserMessagesSetup implements Sensor {

	private final static Logger LOG = Logger.getLogger(UserMessagesSetup.class);
	
	@Override
	public void processRequest(Request request, Response response) {
		
		if ( "refresh".equals( request.action ) ) {
			refresh(request, response); 
		} else if ( "list".equals( request.action ) ) {
			response.writeTextWithNoHeaderAndFooter(
					ErrorMessages.getInstance().getEnglishMessages());
			
		} else {
			response.setErrorMessage("INFORMATION_MISSING_ERROR", ErrorCodes.PROCESSING_ENDPOINT_FAILURE, 
										ErrorCodes.QUERY_KEY, "Unknow user action : " + request.action, true);
		}
	}

	private void refresh(Request request, Response response) {
		
		LOG.debug("Processing Refresh messages");
		refreshMesssages();
	}
	
	private synchronized void refreshMesssages()
	{
		try 
		{
			ErrorMessages.getInstance().refreshLocales();
			LOG.debug("Refreshing messages done.");
		} catch (Exception e) {
			LOG.fatal("Error in loading the language packages " + e.getMessage());
		}
	}
	
	@Override
	public void init() 
	{
		ErrorMessages.getInstance().refreshLocales();
	}

	@Override
	public String getName() {
		return "lang";
	}



}
