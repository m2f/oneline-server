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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ErrorMessages 
{

	private final static Logger LOG = LogManager.getLogger(ErrorMessages.class);

	private Map<String, Map<String, String>> allLanguagePackMap = new HashMap<String, Map<String,String>>();

	private static ErrorMessages instance;

	private ErrorMessages() {
	}

	public static ErrorMessages getInstance()
	{
		if ( null != instance) return instance;
		synchronized (ErrorMessages.class.getName())
		{
			if ( null != instance) return instance;
			instance = new ErrorMessages();
		}
		return instance;
	}
	
	public String getEnglishMessages() {
		if ( null != this.allLanguagePackMap) {
			if ( allLanguagePackMap.containsKey("en")) {
				return allLanguagePackMap.get("en").toString();
			}
		}
		return "";
	}

	public void refreshLocales()
	{
		if( null == this.allLanguagePackMap) this.allLanguagePackMap = new HashMap<String, Map<String,String>>();
		this.allLanguagePackMap.clear();

		LOG.debug("Refreshing Locales...");
		Configuration conf = OnelineServerConfiguration.getInstance().getConfiguration();
		String supportedLangs = conf.get("messages.lang","en");
		for (String language : supportedLangs.split(",")) {
			String localeFileName = "messages." + language;
			File localeMsgFile = FileReaderUtil.getFile(localeFileName);
			if ( localeMsgFile.exists()) {
				allLanguagePackMap.put(language, FileReaderUtil.toNameValues(localeFileName));
				LOG.info("Language pack is loaded - " + localeFileName  );
			} else {
				LOG.warn(language + " locale is not supported. '" + localeFileName + "'" + " is missing." );
			}
		}
	}

	public String getMessage(String lang, String code)
	{
		String message = null;
		if( null != this.allLanguagePackMap) {
			Map<String , String> x = this.allLanguagePackMap.get(lang);
			if ( null != x) message = x.get(code);
		}
		return ( null == message ) ? code : message ;
	}

}