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

public interface ILogger 
{
	static final int TRACE = 0;
	static final int DEBUG = 1;
	static final int INFO = 2;
	static final int WARN = 3;
	static final int ERROR = 4;
	static final int FATAL = 5;
	
	boolean isTraceEnabled();

	
	void trace(Object message, Throwable t);

	
	void trace(Object message);

	
	void debug(Object message, Throwable t);

	
	void debug(Object message);

	
	void error(Object message, Throwable t);

	
	void error(Object message);

	
	void fatal(Object message, Throwable t);

	
	void fatal(Object message);

	
	void info(Object message, Throwable t);

	
	void info(Object message);

	
	boolean isDebugEnabled();

	
	boolean isInfoEnabled();

	
	void warn(Object message, Throwable t);

	
	void warn(Object message);

}
