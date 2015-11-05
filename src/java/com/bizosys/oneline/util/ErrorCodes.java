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

public class ErrorCodes {
	public static String AUTH_FAILURE = "0001";
	public static String INVALID_PARAMETERS = "0002";
	public static String UNKNOWN_USER = "0003";
	public static String UNKNOWN_ERRORS = "0004";
	public static String SMTP_FAILURE = "0005";
	public static String PROCESSING_ENDPOINT_FAILURE = "0006";
	public static String INVALID_INPUT_PARAMETER = "0007";
	public static String MISSING_TEMPLATE = "0008";
	public static String CAPTCHA_MISMATCH = "0009";
	public static String UNAUTHORIZED_FILE_UPLOAD = "0011";
	public static String SUCCESSFULL_FILE_UPLOAD = "0012";
	public static String ERROR_FILE_UPLOAD = "0013";
	public static String REGISTRATION_NOT_ALLOWED = "0014";
	public static String USER_ALREADY_EXIST = "0015";
	public static String UNABLE_TO_REGISTER = "0016";
	public static String INVALID_COMBINATION = "0017";
	public static String UNAUTHORIZED_ACCESS_ATTEMPT = "0018";
	public static String COMPANY_ACCOUNT_DISABLED = "0019";
	public static String ACCOUNT_DISABLED = "0020";
	public static String ACCOUNT_LOCKED = "0021";
	public static String MAXIMUM_INCORRECT_ATTEMPT = "0023";
	public static String SYSTEM_LOCKED = "0024";
	public static String ERROR_RETREIVING_USER_PROFILE = "0025";
	public static String OLD_PASSWORD_MISMATCH = "0026";
	public static String PASSWORD_UPDATE_FAILED = "0027";
	public static String INVALID_TOKEN = "0028";
	public static String INFORMATION_NOT_FOUND = "0030";
	public static String DUPLICATE_ENTRY = "0031";
	public static String UNEXPECTED_DATA_HANDLING = "0032";
	public static String QUERY_NOT_FOUND = "0033";
	public static String INVALID_DATA_FORMAT = "0034";
	public static String UNAUTHORIZED_ACCESS = "0035";
	public static String UNAUTHORIZED_USER_ACCESS = "0036";
	public static String QUERY_EXECUTION_SUCCESS = "0037";
	public static String STORED_PROC_NOT_FOUND = "0033";

	public static String USER_LIMIT_REACHED = "0052";
	public static String INVALID_USER_ID = "0055";
	public static String INVALID_DATA_API = "0056";
	public static String DATA_API_REFRESH_FAILURE = "0057";

	public static String SEQUENCE_ID_NOTSETUP = "0058";
	public static String SEQUENCE_AMOUNT_ATLEAST_ONE = "0059";
	public static String NOTSUPPORTED_EMBEDDED_FUNCTION = "0060";
	public static String FUNCTION_NOT_FOUND = "0061";

	public static String TXN_SESSION_EXPIRED = "0062";
	public static String SQL_ERROR = "0063";
	public static String ERROR_IN_SP_EXECUTION = "0064";
	public static String SP_NOT_FOUND = "0065";
	
	public static String SYSTEM_CONFIGURATION_FAILURE = "9999";
	public static String INPUT_JSON_ERROR = "8888";
	public static String DB_CONNECTION_ERROR = "7777";
	
	public static String QUERY_KEY = "query";
	public static String FUNCTION_KEY = "function";
	public static String SP_KEY = "sp";
	public static String SEQUENCE_GENERATION_KEY = "SequenceGeneration";
	
	public static String DB_KEY = "db";
}

