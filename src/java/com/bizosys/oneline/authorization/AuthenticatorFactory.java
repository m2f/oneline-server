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

import org.apache.log4j.Logger;

import com.bizosys.oneline.sql.SqlSensor;
import com.bizosys.oneline.util.CreateException;
import com.bizosys.oneline.util.OnelineServerConstants;


public class AuthenticatorFactory {

	private static AuthenticatorFactory instance = null;

	public static AuthenticatorFactory getInstance() {
		if ( null != instance ) return instance;
		synchronized (AuthenticatorFactory.class.getName()) {
			if ( null != instance ) return instance;
			instance = new AuthenticatorFactory();
		}
		return instance;
	}
	
	IAuthenticate authenticatorImplInstance = null;
	Logger LOG = Logger.getLogger(SqlSensor.class);
	
	public IAuthenticate getAuthenticatorImpl() throws CreateException {

		if ( null != authenticatorImplInstance) return authenticatorImplInstance;

		synchronized (IAuthenticate.class.getName()) {
			if ( null != authenticatorImplInstance ) return authenticatorImplInstance;
			try {
				LOG.info("Initializing the authenticator module :" + OnelineServerConstants.AUTHENTICATOR_CLASS);
				Object authenticatorImplInstanceO = Class.forName(OnelineServerConstants.AUTHENTICATOR_CLASS).newInstance();
				if( ! (authenticatorImplInstanceO instanceof IAuthenticate) )
					throw new CreateException("Couldnot find the Queue Implementation: [" + authenticatorImplInstance + "]");
				this.authenticatorImplInstance = (IAuthenticate) authenticatorImplInstanceO;
			} catch (Exception e) {
				LOG.fatal(e);
				throw new CreateException("Unable to crate the authenticator : " + OnelineServerConstants.AUTHENTICATOR_CLASS);
			} 
			
		}
		return authenticatorImplInstance;
	}	
}