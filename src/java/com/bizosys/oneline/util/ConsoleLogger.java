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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The logging framework ( Logs into console )
 * @author pramod
 *
 */
public final class ConsoleLogger implements ILogger 
{
	private String className = ConsoleLogger.class.getName();
	
	int logLevel = 0;
	
	public ConsoleLogger(){
	}
	
	public ConsoleLogger(String clazz){
		this.className = clazz;
	}
	
	public ConsoleLogger(String clazz, int logLevel){
		this.className = clazz;
		this.logLevel = logLevel;
	}

	@Override
	public final boolean isTraceEnabled() {
		return false;
	}

	@Override
	public final void trace(final Object message, final Throwable t) {
		if ( ! isTraceEnabled()) return;
		System.err.println(getPrefix(TRACE) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void trace(final Object message) {
		if ( ! isTraceEnabled()) return;
		System.err.println(getPrefix(TRACE) + message.toString());
	}

	@Override
	public final void debug(final Object message, final Throwable t) {
		if ( ! isDebugEnabled()) return;
		System.err.println(getPrefix(DEBUG) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void debug(final Object message) {
		if ( ! isDebugEnabled()) return;
		System.err.println(getPrefix(DEBUG) + message.toString());
	}

	@Override
	public final void error(final Object message, final Throwable t) {
		System.err.println(getPrefix(ERROR) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void error(final Object message) {
		System.err.println(getPrefix(ERROR) + message.toString());
	}

	@Override
	public final void fatal(final Object message, final Throwable t) {
		System.err.println(getPrefix(FATAL) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void fatal(final Object message) {
		System.err.println(getPrefix(FATAL) + message.toString());
	}

	@Override
	public final void info(final Object message, final Throwable t) {
		if ( ! isInfoEnabled()) return;
		System.err.println(getPrefix(INFO) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void info(final Object message) {
		if ( ! isInfoEnabled()) return;
		System.err.println(getPrefix(INFO) + message.toString());
	}

	@Override
	public final boolean isDebugEnabled() {
		return (logLevel <= DEBUG);
	}

	@Override
	public final boolean isInfoEnabled() {
		return (logLevel <= INFO);
	}

	@Override
	public final void warn(final Object message, final Throwable t) {
		System.err.println(getPrefix(WARN) + message.toString());
		t.printStackTrace();
	}

	@Override
	public final void warn(final Object message) {
		System.err.println(getPrefix(WARN) + message.toString());
	}
	
	StringBuilder sb = new StringBuilder();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	public final String getPrefix(final int level) {
		sb.setLength(0);
		sb.append(sdf.format(new Date()));
		switch ( level) {
			case  TRACE:
				sb.append(" [TRACE] ");
				break;
			case  DEBUG:
				sb.append(" [DEBUG] ");
				break;
			case  INFO:
				sb.append(" [INFO ]");
				break;
			case  WARN:
				sb.append(" [WARN ] ");
				break;
			case  ERROR:
				sb.append(" [ERROR] ");
				break;
			case  FATAL:
				sb.append(" [FATAL] ");
				break;
		}
		sb.append(this.className).append(" - ");
		return  sb.toString(); 
	}
}
