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

import org.apache.log4j.Logger;

public final class Log4JLogger implements ILogger {

	Logger l = null;
	
	public Log4JLogger(Class<?> classz){
		l = Logger.getLogger(classz);
	}
	
	@Override
	public final boolean isTraceEnabled() {
		return l.isTraceEnabled();
	}

	@Override
	public final  void trace(final Object message, final Throwable t) {
		l.trace(message, t);
	}

	@Override
	public final void trace(final Object message) {
		l.trace(message);
	}

	@Override
	public final void debug(final Object message, final Throwable t) {
		l.debug(message, t);
	}

	@Override
	public final void debug(final Object message) {
		l.debug(message);
	}

	@Override
	public final void error(final Object message, final Throwable t) {
		l.error(message,t);
	}

	@Override
	public final void error(final Object message) {
		l.error(message);
	}

	@Override
	public final void fatal(final Object message, final Throwable t) {
		l.fatal(message,t);
	}

	@Override
	public final void fatal(final Object message) {
		l.fatal(message);
		
	}

	@Override
	public final void info(final Object message, final Throwable t) {
		l.info(message,t);
	}

	@Override
	public final void info(final Object message) {
		l.info(message);
	}

	@Override
	public boolean isDebugEnabled() {
		return l.isDebugEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return l.isInfoEnabled();
	}

	@Override
	public final void warn(final Object message, final Throwable t) {
		l.warn(message,t);
	}

	@Override
	public final void warn(final Object message) {
		l.warn(message);
	}
}
