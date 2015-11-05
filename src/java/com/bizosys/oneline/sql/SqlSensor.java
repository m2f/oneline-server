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

package com.bizosys.oneline.sql;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

import com.bizosys.oneline.authorization.AuthorizationFactory;
import com.bizosys.oneline.authorization.IAuthroize;
import com.bizosys.oneline.dao.ReadArray;
import com.bizosys.oneline.dao.ReadJsonArray;
import com.bizosys.oneline.dao.ReadStoredProc;
import com.bizosys.oneline.dao.ReadXLS;
import com.bizosys.oneline.dao.ReadXml;
import com.bizosys.oneline.dao.WriteBase;
import com.bizosys.oneline.jkeylockmanager.manager.KeyLockManager;
import com.bizosys.oneline.jkeylockmanager.manager.KeyLockManagers;
import com.bizosys.oneline.jkeylockmanager.manager.LockCallback;
import com.bizosys.oneline.management.MetricAvgRollup;
import com.bizosys.oneline.management.MetricAvgRollupGate;
import com.bizosys.oneline.model.StoredProcOutParam;
import com.bizosys.oneline.service.ServiceFactory;
import com.bizosys.oneline.user.UserProfile;
import com.bizosys.oneline.util.DbConfigUtil;
import com.bizosys.oneline.util.ErrorCodeExp;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.ExceptionHolder;
import com.bizosys.oneline.util.LineReaderUtil;
import com.bizosys.oneline.util.OnelineServerConstants;
import com.bizosys.oneline.util.SqlValidator;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.InvalidRequestException;
import com.bizosys.oneline.web.sensor.Request;
import com.bizosys.oneline.web.sensor.Response;
import com.bizosys.oneline.web.sensor.Sensor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class SqlSensor implements Sensor 
{
	static enum SqlSensorActions {
		EXECUTE,EXECUTE_URL,EXECUTE_TEXT,
		CREATE_SP,DELETE_SP,EXECUTE_SP,
		CRAETE_TXN,RELEASE_TXN,
		GENERATE_ID,GENERATE_IDS,
		REFRESH,REFRESH_FUNCTION,REFRESH_SP,
		VALIDATE_SQL,VERSION,INITIALIZE_POOL
	}

	static final Map<String, SqlSensorActions> ACTIONS = new HashMap<String, SqlSensor.SqlSensorActions>();
	static {
		ACTIONS.put("execute", SqlSensorActions.EXECUTE);
		ACTIONS.put("executeUrl", SqlSensorActions.EXECUTE_URL);
		ACTIONS.put("executeText", SqlSensorActions.EXECUTE_TEXT);
		ACTIONS.put("createsp", SqlSensorActions.CREATE_SP);
		ACTIONS.put("deletesp", SqlSensorActions.DELETE_SP);
		ACTIONS.put("executesp", SqlSensorActions.EXECUTE_SP);
		ACTIONS.put("createTxn", SqlSensorActions.CRAETE_TXN);
		ACTIONS.put("releaseTxn", SqlSensorActions.RELEASE_TXN);
		ACTIONS.put("generateid", SqlSensorActions.GENERATE_ID);
		ACTIONS.put("generateids", SqlSensorActions.GENERATE_IDS);
		ACTIONS.put("refresh", SqlSensorActions.REFRESH);
		ACTIONS.put("refreshFunc", SqlSensorActions.REFRESH_FUNCTION);
		ACTIONS.put("refreshsp", SqlSensorActions.REFRESH_SP);
		ACTIONS.put("validatesql", SqlSensorActions.VALIDATE_SQL);
		ACTIONS.put("version", SqlSensorActions.VERSION);
		ACTIONS.put("initializePool", SqlSensorActions.INITIALIZE_POOL);
	}

	public static final class QueryCounters {
		
		public short selects = 0;
		public short creates = 0;
		public short iuds = 0;
		
		public short totalIUDVisited = 0;
		public short totalIUDExecuted = 0;
		
		@Override
		public String toString() {
			return "Selects/Assigns = " + this.selects + ", creates = " + creates + ", iuds = " + iuds +
					", totalIUDVisited = " + totalIUDVisited + ", totalIUDExecuted = " + totalIUDExecuted;
		}
	}

	public static final class IsFirst {
		public boolean isFirst = true;
	}
	
	private static final Class<Object> OBJECT_CLAZZ = Object.class;
	private final static Logger LOG = Logger.getLogger(SqlSensor.class);

	private static final boolean INFO_ENABLED = LOG.isInfoEnabled();
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

	private static String DOWNLOAD_TEMPLATE_DIR = "/tmp";
	private String sucessMsg = "OK";

	MetricAvgRollup mr = new MetricAvgRollup("SQL"); 
	IAuthroize authorizor = null;
	IIdGenerator idgenerator = new IdGenerator();

	private final KeyLockManager lockManager = KeyLockManagers.newLock();

	@Override
	public void init() 
	{
		try
		{
			DOWNLOAD_TEMPLATE_DIR = ServiceFactory.getInstance().
					getAppConfig().get("DOWNLOAD_TEMPLATE_DIR","/tmp");
			if ( ! DOWNLOAD_TEMPLATE_DIR.endsWith("/")) DOWNLOAD_TEMPLATE_DIR = DOWNLOAD_TEMPLATE_DIR + "/";

			MetricAvgRollupGate.getInstance().register(mr);

			authorizor = AuthorizationFactory.getInstance().getAuthorizationcatorImpl();

		}
		catch(Exception e)
		{
			LOG.fatal("Error in initializing download template folder " + e.getMessage());
			e.printStackTrace();
		}

		try {
			
			DbConfigUtil.setupPoolByUser(OnelineServerConstants.CURRENT_MACHINE_IP, 
										 OnelineServerConstants.PROJECT_USER);
			QueryDefinationFactory.refreshQueries(); 
			FunctionDefinationFactory.refreshFunctions();
			StoredProcDefinationFactory.refreshQueries();

		} catch ( Exception ex ) {
			LOG.fatal("Error in initializing functions or stored proc Definations - " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public final void processRequest(final Request request, final Response response)
	{
		long start = System.currentTimeMillis();
		
		String action = request.action;
		MetricAvgRollupGate monitorReq = MetricAvgRollupGate.getInstance();
		boolean isSucess = true;
		try
		{
			monitorReq.onEnter(mr);
			SqlSensorActions currentAction = ACTIONS.get(action);
			switch (currentAction) {
			case EXECUTE:
				if( ! request.mapData.containsKey("lock")) {
					
					executeQuery(request, response);

				} else {
					String lock = request.mapData.get("lock");
					System.out.println("LOCK: " + lock);
					final ExceptionHolder exceptionHolder = new ExceptionHolder();
					lockManager.executeLocked(lock, new LockCallback() {

						@Override
						public void doInLock() {
							try {
								executeQuery(request, response);
							} catch (ErrorCodeExp | IOException | SQLException e) {
								e.printStackTrace();
								exceptionHolder.ex = e;
							}
						}
					});

					if ( null != exceptionHolder.ex ) throw exceptionHolder.ex;
				}

				break;
			case EXECUTE_URL:
				executeUrl(request, response);
				break;
			case EXECUTE_TEXT:
				executeText(request, response);
				break;
			case CREATE_SP:
				createSP(request, response);
				break;
			case DELETE_SP:
				deleteSP(request, response);
				break;
			case EXECUTE_SP:
				executeStoredProc(request, response);
				break;
			case CRAETE_TXN:{
				String idleTimeInMillis = request.getString("idleTimeInMillis", true, true, false);
				String pool = request.getString("pool", true, true, true);
				Long txnId = TxnScopeFactory.getInstance().createClientTxn(
						pool, new Integer(idleTimeInMillis));
				response.writeTextWithHeaderAndFooter(txnId.toString());
				break;
			}
			case RELEASE_TXN:{
				String txnId = request.getString("txnid", true, false, false);
				String isCommit = request.getString("commit", true, false, true);
				boolean status = TxnScopeFactory.getInstance().closeClientTxn(new Long(txnId), new Boolean(isCommit));
				sendSuccessMessages(request, response, status);
				break;
			}
			case GENERATE_ID:
				idgenerator.generateId(request, response);
				break;
			case GENERATE_IDS:
				idgenerator.generateIds(request, response);
				break;
			case REFRESH:{
				boolean isOK = QueryDefinationFactory.refreshQueries();
				isOK = FunctionDefinationFactory.refreshFunctions();
				isOK = StoredProcDefinationFactory.refreshQueries();
				sendSuccessMessages(request, response, isOK);
				break;
			}
			case REFRESH_FUNCTION:{
				boolean isOK = FunctionDefinationFactory.refreshFunctions();
				sendSuccessMessages(request, response, isOK);
				break;
			}
			case REFRESH_SP:{
				boolean isOK = StoredProcDefinationFactory.refreshQueries();
				sendSuccessMessages(request, response, isOK);
				break;
			}
			case VALIDATE_SQL:
				validateSql(request , response);
				break;
			case VERSION:
				getBuildVersion(request, response);
				break;
			case INITIALIZE_POOL:
				initializePool(request, response);
				break;
			default:
				LOG.warn("Invalid action in request - " + request.toString());
				throw new InvalidRequestException("INVALID_OPERATION");
			}
		}

		catch (InvalidRequestException ire) {
			isSucess = false;
			response.setErrorMessage("INFORMATION_MISSING_ERROR", ErrorCodes.INFORMATION_NOT_FOUND, ErrorCodes.QUERY_KEY, ire.getMessage(), true);
		}
		catch ( SQLException sqlEx)
		{
			isSucess = false;
			String errorMessage = sqlEx.getMessage();
			if ( errorMessage.contains("link failure") || errorMessage.contains("Could not make connection"))
			{
				errorMessage = new String(errorMessage);
				response.setErrorMessage("DB_CONNECTION_ERROR", ErrorCodes.DB_CONNECTION_ERROR, ErrorCodes.DB_KEY, errorMessage,sqlEx);
			}
		} 
		catch (ErrorCodeExp ex)
		{
			isSucess = false;
			LOG.warn(ex.errorTitle + ex.errorCode + ex.errorMsg + "\n" + request.toString());
			response.setErrorMessage(ex.errorTitle, ex.errorCode, ex.errorType, ex.errorMsg, true);
		} catch (NullPointerException ex) 
		{
			ex.printStackTrace();
			isSucess = false;
			LOG.fatal("Null pointer",ex);
			response.setErrorMessage("UNKNOWN_ERROR", ErrorCodes.UNKNOWN_ERRORS, ErrorCodes.QUERY_KEY, ex.getMessage(),ex);
		}
		catch(JsonSyntaxException jsonEx)
		{
			jsonEx.printStackTrace();
			isSucess = false;
			LOG.fatal(request.toString(), jsonEx);
			response.setErrorMessage("INPUT_JSON_ERROR", "", ErrorCodes.INPUT_JSON_ERROR, jsonEx.getMessage(),jsonEx); 
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			isSucess = false;
			LOG.fatal(request.toString(), ex);
			response.setErrorMessage("UNKNOWN_ERROR", ErrorCodes.UNKNOWN_ERRORS, ErrorCodes.QUERY_KEY, ex.getMessage(),ex); 
		} finally 
		{
			monitorReq.onExit(mr, isSucess);
			long end = System.currentTimeMillis();
			if( INFO_ENABLED ) LOG.info("Time taken to process request is [ " + (end-start) + " ms ]");
		}
	}

	private final void createSP(final Request request, final Response response) throws SQLException 
	{
		if ( DEBUG_ENABLED ) LOG.debug("Creating SP...");

		String spTitle = request.getString("sptitle", true, false, false);
		String spBody = request.getString("spbody", true, false, false);
		String poolName = request.getString("pool", true, false, false);

		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

		response.writeTextWithNoHeaderAndFooter("[");
		WriteBase writer = new WriteBase(poolName);

		try
		{
			writer.execute(spBody);
			response.writeTextWithNoHeaderAndFooter(Response.getMsg("xml".equals(formatType), false, ErrorCodes.QUERY_EXECUTION_SUCCESS, sucessMsg, 
													spTitle,ErrorCodes.SP_KEY, true, false, null));
		}
		catch(SQLException sqlEx)
		{
			response.writeTextWithNoHeaderAndFooter(Response.getMsg("xml".equals(formatType), false, ErrorCodes.UNEXPECTED_DATA_HANDLING, 
													sqlEx.getMessage(), spTitle, ErrorCodes.SP_KEY, true, true, null));
			response.skipErrorRewrite = true;
			throw new SQLException();
		}
		finally
		{
			if ( !"xml".equals(formatType) ) response.writeTextWithNoHeaderAndFooter("]");
		}
	}


	private final void deleteSP(final Request request, final Response response) throws SQLException 
	{
		if ( DEBUG_ENABLED ) LOG.debug("deleting SP...");

		String spTitle = request.getString("sptitle", true, false, false);
		String poolName = request.getString("pool", true, false, false);

		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

		response.writeTextWithNoHeaderAndFooter("[");
		WriteBase writer = new WriteBase(poolName);

		try
		{
			writer.execute("drop procedure if exists "  +spTitle);
			response.writeTextWithNoHeaderAndFooter(Response.getMsg("xml".equals(formatType), false, ErrorCodes.QUERY_EXECUTION_SUCCESS, 
													sucessMsg, spTitle, ErrorCodes.SP_KEY, true, false, null));
		}
		catch(SQLException sqlEx)
		{
			response.writeTextWithNoHeaderAndFooter(Response.getMsg("xml".equals(formatType), false, ErrorCodes.UNEXPECTED_DATA_HANDLING, 
													sqlEx.getMessage(), spTitle, ErrorCodes.SP_KEY, true, true, null));
			response.skipErrorRewrite = true;
			throw new SQLException();
		}
		finally
		{
			if ( !"xml".equals(formatType) ) response.writeTextWithNoHeaderAndFooter("]");
		}
	}

	private final void validateSql(final Request request, final Response response) throws ErrorCodeExp 
	{
		String query = request.getString("query", false, true, false);
		String isValid = SqlValidator.validateSql(query);
		response.writeTextWithNoHeaderAndFooter(isValid);
	}	

	private final void executeUrl(final Request request, final Response response) throws ErrorCodeExp
	{
		String sqlUrl = request.getString("sqlUrl", true, false, false);
		String pool = request.getString("pool", true, false, true);

		String txnid = request.getString("txnid", false, false, true);
		Long txnId = ( null == txnid)? null : new Long(txnid).longValue();

		TxnScope scope = TxnScopeFactory.getInstance().get(txnId,false, pool);
		ExecuteSqlFile sqlFileProcessor = new ExecuteSqlFile(scope);
		String tempFile = sqlFileProcessor.createTempFile();
		sqlFileProcessor.downloadUrl(sqlUrl, tempFile);
		boolean status = sqlFileProcessor.executeSqlFile(tempFile);
		sendSuccessMessages(request, response, status);
		sqlFileProcessor.deleteTempFile(tempFile);
	}

	private final void executeText(final Request request, final Response response) throws ErrorCodeExp 
	{
		String sqlBody = request.getString("sqlBody", true, false, false);
		String pool = request.getString("pool", true, false, true);

		String txnid = request.getString("txnid", false, false, true);
		Long txnId = ( null == txnid)? null : new Long(txnid).longValue();
		TxnScope scope = TxnScopeFactory.getInstance().get(txnId,false,pool);
		ExecuteSqlFile sqlFileProcessor = new ExecuteSqlFile(scope);
		boolean status = sqlFileProcessor.executeSqlText(sqlBody);
		sendSuccessMessages(request, response, status);
	}	


	/**
	 * Execute a Query
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private final void executeQuery(final Request request, final Response response) throws ErrorCodeExp, IOException, SQLException
	{

		String query = request.getString("query", true, false, false);
		String pool = request.getString("pool", false, false, true);

		if ( DEBUG_ENABLED ) LOG.debug(pool + "\t" + query);

		List<UnitStep> querySteps = new ArrayList<UnitStep>(8);
		UserProfile user = request.getUser();
		String locale = user.getLocale();
		boolean validQuery = false;
		try {
			validQuery = SqlSensorInputParser.fromJson(query, querySteps);
		} catch (ErrorCodeExp ex) {
			response.setErrorMessage(ex.errorTitle, ErrorCodes.INVALID_DATA_API, ex.errorType,ex.getMessage(), true);
			return;
		}
		
		if ( !validQuery ) {
			response.setErrorCode("INVALID_API_ERROR", ErrorCodes.INVALID_DATA_API, locale, true);
			return;
		}

		/**
		 * Step#2 : Authorization check
		 */


		if ( OnelineServerConstants.SQL_CHECK_PERMISSIONS ) {

			UserProfile userProfile = isLoggedIn(request, response);
			if ( null == userProfile) return;

			boolean isPermitted = isAuthorized(querySteps, user);
			if ( ! isPermitted ) {
				if ( DEBUG_ENABLED ) LOG.debug( "Step#2 : Not Permitted by " +  authorizor.getClass().getName());
				response.setErrorCode("AUTHORIZATION_ERROR", ErrorCodes.UNAUTHORIZED_ACCESS, locale, true);
				return;
			}
			if ( DEBUG_ENABLED ) LOG.debug( "Step#2 : Over" );
		} else {
			if ( DEBUG_ENABLED ) LOG.debug( "Step#2 : Skipping Sql Authorization, enable using OnelineServerConstants.SQL_CHECK_PERMISSIONS" );
		}

		int totalQueries = querySteps.size();
		if ( DEBUG_ENABLED ) LOG.debug( "Step#3 : Execution => " + totalQueries + " queries to execute");
		if (totalQueries == 1 ) {
			if ( querySteps.get(0).isFunc) multiStatements(request, response, pool, querySteps);
			else singleStatement(request, response, pool, ((UnitQuery)querySteps.get(0)) , locale);
		} else if ( totalQueries > 1) {
			multiStatements(request, response, pool, querySteps);
		} 
	}

	private final boolean isAuthorized(final List<UnitStep> queryUnits, final UserProfile user) throws IOException 
	{
		if ( DEBUG_ENABLED ) LOG.debug( "Step#2 : Authorization check" );
		String sensorName = getName();
		List<String> titles = new ArrayList<>();

		for ( UnitStep queryUnit : queryUnits) {

			if ( queryUnit.isFunc ) {
				UnitFunction func = (UnitFunction) queryUnit;
				List<UnitStep> funcSteps = FunctionDefinationFactory.functions.get(func.stepId); 

				for (UnitStep unitStep : funcSteps) {
					if ( unitStep.isFunc ) { 
						boolean isPermitted = isAuthorized( ((UnitFunction)unitStep).getSteps(), user );
						if ( !isPermitted) return isPermitted;
					} else {
						UnitQuery unitQuery = (UnitQuery) queryUnit;
						titles.add(unitQuery.appConfig.title);
					}
				}
			} else {
				UnitQuery unitQuery = (UnitQuery) queryUnit;
				titles.add(unitQuery.appConfig.title);
			}
		}

		boolean isPermitted = true;
		for (String title : titles) {
			if ( null == authorizor) isPermitted = false;
			else isPermitted = authorizor.isAuthorized(user, sensorName, title);
			if( ! isPermitted ) break;
		}
		return isPermitted;
	}

	/**
	 * Only 1 statement to execute
	 * @param request
	 * @param response
	 * @param pool
	 * @param parsedQuery
	 * @param locale
	 * @throws SQLException
	 * @throws ErrorCodeExp 
	 */
	private final void singleStatement(final Request request, final Response response,
			final String pool, final UnitQuery queryUnit, final String locale) throws SQLException, ErrorCodeExp 
	{
		UnitVariables finalVariables = singularQueryVariableAssignemnt(request, queryUnit);
		executeQueryUnit( queryUnit, finalVariables, pool, request, response, locale);
	}

	private final void executeQueryUnit(final UnitQuery queryUnit, final UnitVariables finalVariables, final String pool, 
			final Request request, final Response response, final String locale) throws SQLException, ErrorCodeExp 
	{
		if ( ! evaluateExpr(queryUnit.expr,queryUnit.andExprs, queryUnit.orExprs, finalVariables) ) {
			if ( DEBUG_ENABLED ) LOG.debug("Evaluation failed. Skipping");
			return;
		}
		executeUnit(queryUnit,finalVariables, pool,request,response,locale);

		if(queryUnit.isRecursive){
			executeQueryUnit( queryUnit, finalVariables, pool, request, response, locale);				
		}
	}

	private final boolean executeUnit(final UnitQuery queryUnit, final UnitVariables finalVariables, final String pool, 
			final Request request, final Response response, final String locale) throws SQLException, ErrorCodeExp 
	{
		variableAsssignment(queryUnit, finalVariables);
		replaceCustomVars(queryUnit, finalVariables, response);
		
		String txnid = request.getString("txnid", false, false, true);
		Long txnId = ( null == txnid)? null : new Long(txnid).longValue();
		char queryConfigType = queryUnit.appConfig.configtype.charAt(0); 
		switch ( queryConfigType ) {
		case 'A' :
		{
			TxnScope scope = null;
			if( null != txnId) scope = TxnScopeFactory.getInstance().get(txnId, false, pool);

			String formatType = request.getString("format", false, false, true);
			if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

			Object[] output = null;
			try
			{
				output = assignQuery(request, response, scope, pool, queryUnit, finalVariables);
			}
			catch(SQLException sqlEx)
			{
				response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING,ErrorCodes.QUERY_KEY, 
											sqlEx.getMessage(), true);
				throw sqlEx;
			}

			int len = ( null == output ) ? 0 : output.length;
			String[] variableNames = queryUnit.appConfig.outvarA;

			if( null == variableNames ) {
				String errorMessage = "Output variables is not configured"; 
				LOG.warn(errorMessage);
				response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.QUERY_KEY,
						errorMessage, true);
				throw new SQLException();
			}

			
			if( 0 == len ) {
				output = new Object[variableNames.length];
				Arrays.fill(output, "");
				len = output.length;
			}
			
			if( len != variableNames.length ) {
				LOG.warn("Output variables size does not match with "
						+ "fetched columns size in assignment query fetchedColumns != outputVariablesLen " 
						+ len + "!=" + variableNames.length);
				response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.QUERY_KEY,
							"Output variables size does not match with fetched columns size in assignment query fetchedColumns != outputVariablesLen " 
							+ len + "!=" + variableNames.length, true);
				throw new SQLException();
			}

			if(formatType.equals("jsonp")) response.format = formatType;
			StringBuilder sb = new StringBuilder(128);
			sb.append("[{\"key\" : \"")
			.append(queryUnit.appConfig.title)
			.append("\", \"type\" : \"query")
			.append("\", \"values\" : [");
			
			for( int i = 0 ; i < len ; i++ ) {
				if( i > 0 ) sb.append(",");
				sb.append("{\"" + variableNames[i] + "\":\"")
				.append(new String(output[i].toString()))
				.append("\"}");
			}
			sb.append("], \"response\":\"data\" }");
			response.writeTextWithNoHeaderAndFooter(sb.toString());

			/**
			 * Write the out variables
			 */
			if ( null != finalVariables) 
			{ 
				Map<String, String> variablesM = finalVariables.variablesM;
				if ( null != variablesM ) 
				{
					StringBuilder varSb = new StringBuilder(128);
					boolean isFirstVar = true;
					for(String key : variablesM.keySet())
					{
						if ( isFirstVar ) isFirstVar = false;
						else varSb.append(',');
						
						String value = finalVariables.variablesM.get(key);
						value = (null == value) ? "" : value;
						varSb.append("{\"").append(key)
							.append("\":\"").append(value).append("\"}");
					}
					response.writeTextWithNoHeaderAndFooter(",{\"key\" : \"outputvariables\", \"type\" : \"variables\", \"values\" : [" + varSb.toString() + "]}");
				}
			}
			response.writeTextWithNoHeaderAndFooter("]");
		}
		break;

		case 'S' :
		{
			String formatType = request.getString("format", false, false, true);
			if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

			TxnScope scope = TxnScopeFactory.getInstance().get(txnId, false, pool);
			response.writeTextWithNoHeaderAndFooter("[{\"key\" : \"" + queryUnit.appConfig.title +"\",");
			response.writeTextWithNoHeaderAndFooter("\"type\" : \"query\",");
			try
			{
				selectQuery(request, response, scope, pool, queryUnit, finalVariables);
				response.writeTextWithNoHeaderAndFooter(",\"response\" : \"data\" }");

				/**
				 * Write the out variables
				 */
				if ( null != finalVariables) 
				{ 
					if ( null != finalVariables.variablesM ) 
					{
						StringBuilder varSb = new StringBuilder(128);
						boolean isFirstVar = true;
						for(String key : finalVariables.variablesM.keySet())
						{
							if ( isFirstVar ) isFirstVar = false;
							else varSb.append(',');
							
							String value = finalVariables.variablesM.get(key);
							value = (null == value) ? "" : value;
							varSb.append("{\"").append(key)
								.append("\":\"").append(value).append("\"}");
						}
						response.writeTextWithNoHeaderAndFooter(",{\"key\" : \"outputvariables\", \"type\" : \"variables\", \"values\" : [" + varSb.toString() + "]}");
					}
				}
				response.writeTextWithNoHeaderAndFooter("]");
			}
			catch(SQLException sqlEx)
			{
				if ( sqlEx.getMessage().toLowerCase().contains("duplicate")) {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.DUPLICATE_ENTRY,ErrorCodes.QUERY_KEY, 
							ErrorMessages.getInstance().getMessage(
									locale, ErrorCodes.DUPLICATE_ENTRY), true);
				} else {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY,sqlEx.getMessage(), true);
				}
				throw sqlEx;
			}
		}
		break;

		case 'R' :
		case 'C' :
		case 'I' :
		case 'U' :
		case 'M' :
		case 'D' :
		{
			TxnScope scope = TxnScopeFactory.getInstance().get(txnId, false, pool);
			if ( DEBUG_ENABLED ) LOG.debug("singleStatement >> executeQueryUnit >> insertOrdeleteOrUpdate");

			String formatType = request.getString("format", false, false, true);
			if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

			response.writeTextWithNoHeaderAndFooter("[");
			try
			{
				if( 'M' == queryConfigType ){
					UserProfile userProfile = request.getUser();
					String loginId = ( null == userProfile) ? "GUEST" : userProfile.loginid;
					String tenantId = ( null == userProfile) ? "-1" : userProfile.getTenantId() + "";

					String  query = enrichQuery(queryUnit, loginId, tenantId, request, finalVariables);
					queryUnit.appConfig.body = query;
				}

				int affectedRows = insertOrdeleteOrUpdate(scope, request, response, queryUnit, pool );

				StringBuilder sb = new StringBuilder(128);
				sb.append("{\"key\" : \"")
				.append(queryUnit.appConfig.title)
				.append("\",\"type\" : \"query\",\"values\" : [ {\"code\" :\"")
				.append(ErrorCodes.QUERY_EXECUTION_SUCCESS)
				.append("\",\"message\":\"")
				.append(ErrorMessages.getInstance().getMessage("en", ErrorCodes.QUERY_EXECUTION_SUCCESS))
				.append("\",\"title\":\"").append(queryUnit.appConfig.title)
				.append("\",\"affectedRows\":").append(affectedRows)
				.append("}], \"response\":\"resultCode\"}");
				
				response.writeTextWithNoHeaderAndFooter(sb.toString());
				
				
				/**
				 * Write the out variables
				 */
				if ( null != finalVariables) 
				{ 
					LOG.debug("input variables are check 1 : " +finalVariables);
					if ( null != finalVariables.variablesM ) 
					{
						StringBuilder varSb = new StringBuilder(128);
						boolean isFirstVar = true;
						for(String key : finalVariables.variablesM.keySet())
						{
							if ( isFirstVar ) isFirstVar = false;
							else varSb.append(',');

							String value = finalVariables.variablesM.get(key);
							value = (null == value) ? "" : value;
							varSb.append("{\"").append(key)
								.append("\":\"").append(value).append("\"}");
						}
						response.writeTextWithNoHeaderAndFooter(",{\"key\" : \"outputvariables\", \"type\" : \"variables\", \"values\" : [" + varSb.toString() + "]}");
					}
				}
			}
			catch(SQLException sqlEx)
			{
				if ( sqlEx.getMessage().toLowerCase().contains("duplicate")) {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.DUPLICATE_ENTRY, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				} else {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				}
				throw sqlEx;
			}
			finally
			{
				if ( !"xml".equals(formatType) ) response.writeTextWithNoHeaderAndFooter("]");
			}
		}
		break;
		default:
			response.setErrorCode("SYSTEM_ERROR", ErrorCodes.SYSTEM_CONFIGURATION_FAILURE, locale, true);
			return true;

		}
		return true;
	}


	/**
	 * Multiple statements to execute
	 * @param request
	 * @param response
	 * @param pool
	 * @param parsedQuery
	 * @throws SQLException
	 */
	private final void multiStatements(final Request request, final Response response, final String pool, 
										final List<UnitStep> querySteps) throws SQLException,  ErrorCodeExp
	{
		QueryCounters queryCounters = new QueryCounters();

		UnitVariables globalVariables = null;
		if ( request.mapData.containsKey("variables") ) {
			String jsonStr =  request.mapData.get("variables");
			if ( DEBUG_ENABLED) LOG.debug("Global Vraibles JSON : " + jsonStr);
			globalVariables = new UnitVariables(jsonStr);
		}

		countQueryTypes(querySteps, queryCounters);
		multiStatementsMixed(request, response, pool, querySteps, queryCounters, globalVariables);
		
	}

	private final void countQueryTypes(final List<UnitStep> querySteps, final QueryCounters queryCounters) throws ErrorCodeExp 
	{
		for ( UnitStep queryStep : querySteps) {

			if ( queryStep.isFunc ) {

				UnitFunction queryFunctions = (UnitFunction) queryStep;

				List<UnitStep> queryUnits = FunctionDefinationFactory.functions.get(queryFunctions.funcId);

				boolean exists = (null == queryUnits) ? false : true;

				if( ! exists ) {
					throw new ErrorCodeExp(queryFunctions.funcId, ErrorCodes.FUNCTION_NOT_FOUND, 
							"Function not found :" + queryFunctions.funcId, ErrorCodes.FUNCTION_KEY);
				}

				for (UnitStep querySubStep : queryUnits) {

					if ( querySubStep.isFunc ) {
						countQueryTypes(  ( (UnitFunction) querySubStep).getSteps(), queryCounters);
					} else {

						UnitQuery queryUnit = (UnitQuery) querySubStep;
						char queryType = queryUnit.appConfig.configtype.charAt(0) ;
						if ( queryType == 'S' || queryType == 'A' ) {
							queryCounters.selects++;
						}else if ( queryType == 'M') {
							queryCounters.creates++;
						} else {
							queryCounters.iuds++;
						}
					}
				}
				
				if( DEBUG_ENABLED ) {
					LOG.debug("FunctionId : " + queryFunctions.funcId 
							  + " Total Selects = " + queryCounters.selects
							  + ", Total Creates = " + queryCounters.creates 
							  + " and Total IUD = " + queryCounters.iuds);
				}

			} else {

				UnitQuery queryUnit = (UnitQuery)  queryStep;
				char queryType = queryUnit.appConfig.configtype.charAt(0) ;
				
				if ( queryType == 'S' || queryType == 'A')
					queryCounters.selects++;
				else if( queryType == 'M' ) 
					queryCounters.creates++;
				else 
					queryCounters.iuds++;
				
				if( DEBUG_ENABLED ) {
					LOG.debug("QueryId : " + queryUnit.appConfig.title 
							  + " Total Selects = " + queryCounters.selects
							  + ", Total Creates = " + queryCounters.creates 
							  + " and Total IUD = " + queryCounters.iuds);
				}
			}
		}
	}

	private final void multiStatementsMixed(final Request request, final Response response,
				final String pool, final List<UnitStep> querySteps, final QueryCounters queryCounters, 
				UnitVariables globalVariables) throws SQLException, ErrorCodeExp 
	{
		IsFirst isFirst = new IsFirst();
		IsFirst isFirstFunc = new IsFirst();
		response.writeTextWithNoHeaderAndFooter("[");
		queryCounters.totalIUDVisited = 0;

		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

		try
		{
			String txnid = request.getString("txnid", false, false, true);
			Long txnId = ( null == txnid)? null : new Long(txnid).longValue();
			TxnScope scope = TxnScopeFactory.getInstance().get(txnId, true, pool);
			
			if( queryCounters.creates > 0 ) {
				scope.openTempTransaction();
			}
			
			try {
				
				globalVariables = executeSteps(request, response, pool, querySteps, queryCounters,
												globalVariables, isFirst, isFirstFunc, scope, false);
				
				if ( null != scope && queryCounters.iuds > 0) {
					boolean isInOpenTransaction = queryCounters.creates > 0;
					if ( DEBUG_ENABLED ) LOG.debug("Committing transaction with isInOpenTransaction " + isInOpenTransaction);
					scope.commitTransaction(isInOpenTransaction);
					scope = null;
				} else if( null != scope && queryCounters.creates > 0 ) {
					scope.closeTempTransaction();
					scope = null;
				}
				
			} catch (Exception ex) {
				
				LOG.fatal("Rolling Back transaction since execution failed - " + ex.getMessage());
				ex.printStackTrace();
				
				if ( null != scope && queryCounters.iuds > 0 ) {
					scope.rollbackTransaction(); 
					scope = null;
				} else if( null != scope && queryCounters.creates > 0 ) {
					scope.closeTempTransaction();
					scope = null;
				}
				
				throw ex;
			} 
			
			/**
			 * Write the out variables
			 */
			if ( null != globalVariables) { 

				if ( null != globalVariables.variablesM ) {

					StringBuilder sb = new StringBuilder(128);
					boolean isFirstVar = true;
					for(String key : globalVariables.variablesM.keySet())
					{
						if ( isFirstVar ) isFirstVar = false;
						else sb.append(',');
						
						String value = globalVariables.variablesM.get(key);
						value = (null == value) ? "" : value;
						sb.append("{\"").append(key)
							.append("\":\"").append(value).append("\"}");
					}

					response.writeTextWithNoHeaderAndFooter(",{\"key\" : \"outputvariables\", \"type\" : \"variables\", \"values\" : [" + sb.toString() + "]}");
				}
			}
		}
		catch(SQLException sqlEx)
		{
			throw new SQLException();
		}
		finally
		{

			if ( ! "xml".equals(formatType) ) response.writeTextWithNoHeaderAndFooter("]");	
		}

	}

	private final UnitVariables executeSteps(final Request request, final Response response, final String pool,
			final List<UnitStep> querySteps, final QueryCounters queryCounters, UnitVariables globalVariables, 
			final IsFirst isFirst, final IsFirst isFirstFunc,
			final TxnScope writer, boolean isInFuncExec) throws SQLException, ErrorCodeExp 
	{

		for ( UnitStep  queryStep : querySteps) {
			globalVariables = executeQueryAndFunc(request, response, pool, queryStep, queryCounters,
												  globalVariables, isFirst, isFirstFunc, writer, querySteps,isInFuncExec);
		}
		return globalVariables;
	}

	private final UnitVariables executeQueryAndFunc(final Request request, final Response response, final String pool,
			final UnitStep queryStep, final QueryCounters queryCounters, UnitVariables globalVariables, 
			IsFirst isFirst, final IsFirst isFirstFunc, final TxnScope writer,
			final List<UnitStep> querySteps,boolean isInFuncExec) throws SQLException, ErrorCodeExp 
	{	

		if ( queryStep.isFunc ) {

			UnitFunction functionStep = (UnitFunction) queryStep;
			List<UnitStep> steps = functionStep.getSteps();

			UnitExpr expr = (null == functionStep.expr)?functionStep.expr :functionStep.expr.clone();

			isInFuncExec = true;
			if ( isFirstFunc.isFirst )
			{
				if ( !isFirst.isFirst )
				{
					response.writeTextWithNoHeaderAndFooter(",");
					isFirst.isFirst = true;
				}

				response.writeTextWithNoHeaderAndFooter("{\"key\" : \"" + functionStep.funcId +"\"" + "," );
				response.writeTextWithNoHeaderAndFooter("\"type\" : \"function\"," );
				response.writeTextWithNoHeaderAndFooter("\"values\" : [" );
			}

			if ( ! evaluateExpr(expr, functionStep.andExprs, functionStep.orExprs , globalVariables) ) 
			{
				if ( DEBUG_ENABLED ) LOG.debug("Function expression evaluation failed. Skipping");
				if ( isFirstFunc.isFirst  ) response.writeTextWithNoHeaderAndFooter("]}" );
				isInFuncExec = false;
				return globalVariables;
			}

			if(functionStep.isRecursive){
				if ( ! isFirstFunc.isFirst ) response.writeTextWithNoHeaderAndFooter("," );
			}
			/**
			 * Overwrite global variables with function local variables. 
			 */
			if(null != functionStep.variables){
				if( null == globalVariables ) globalVariables = new UnitVariables(functionStep.variables);
				else globalVariables.merge(functionStep.variables);
			}
			
			try
			{
				globalVariables = executeSteps(request, response, pool, steps, queryCounters, 
						globalVariables, isFirst, isFirstFunc, writer, isInFuncExec);
			}
			catch(SQLException sqlEx)
			{
				response.setErrorParentTitleAndType(functionStep.funcId, ErrorCodes.FUNCTION_KEY);
				throw sqlEx;
			}

			if ( isFirstFunc.isFirst ) 	isFirstFunc.isFirst = false;

			if(functionStep.isRecursive){

				isFirst = new IsFirst();
				executeQueryAndFunc(request, response, pool, queryStep, queryCounters, globalVariables, 
									isFirst, isFirstFunc, writer, querySteps, isInFuncExec);

			}
			
			if ( ! isFirstFunc.isFirst ) response.writeTextWithNoHeaderAndFooter("],\"response\":\"multidataset\"}" );
			isFirstFunc.isFirst = true;
			isFirst.isFirst = false;
			isInFuncExec = false;
		} else {
			UnitQuery queryUnit = (UnitQuery) queryStep.cloneIt();
			if( queryUnit.sequenceElem != null ) {
				Map<String, String> ids = SqlSensorInputParser.generateSequences(queryUnit.sequenceElem);
				globalVariables.merge(ids);
			}

			if(DEBUG_ENABLED) 
				LOG.debug("Executing queryid [ " + queryUnit.appConfig.title + " ]\nQuery-Counters =>" + queryCounters.toString());

			globalVariables = executeInTxn(request, response, writer, queryUnit, pool, 
					queryCounters, globalVariables, isFirst, isFirstFunc, querySteps, isInFuncExec);
			
			if(DEBUG_ENABLED) 
				LOG.debug("Executed queryid [ " + queryUnit.appConfig.title + " ]\nQuery-Counters =>" + queryCounters.toString());

		}
		return globalVariables;

	}

	private final UnitVariables executeInTxn(final Request request, final Response response, final TxnScope writer, 
			final UnitQuery queryUnit, final String pool, final QueryCounters queryCounters, UnitVariables globalVariables,
			final IsFirst isFirst, final IsFirst isFirstFunc,final List<UnitStep> querySteps, boolean isInFuncExec) 
					throws SQLException, ErrorCodeExp 
	{
		UnitVariables localAndGlobalVariables = null;
		if ( StringUtils.isEmpty( queryUnit.appConfig.variables ) ) {
			if ( null != globalVariables) localAndGlobalVariables = globalVariables;
		} else {
			localAndGlobalVariables = new UnitVariables(queryUnit.appConfig.variables);
			localAndGlobalVariables.merge(globalVariables);
		}

		/**
		 * Overwrite global variable with query specific local variables
		 */
		if ( null != queryUnit.variables) {
			if ( null == localAndGlobalVariables) localAndGlobalVariables = new UnitVariables(queryUnit.variables);
			else localAndGlobalVariables.merge(queryUnit.variables);
		}

		if ( DEBUG_ENABLED ) LOG.debug("Local and Global Merged Variables:" + 
				(( null == localAndGlobalVariables)? "Null" : localAndGlobalVariables.toString()));

		char configType = queryUnit.appConfig.configtype.charAt(0);


		if ( ! evaluateExpr(queryUnit.expr, queryUnit.andExprs, queryUnit.orExprs , localAndGlobalVariables) ) {
			if ( DEBUG_ENABLED ) LOG.debug("Query expression evaluation failed. Skipping for configType " + configType);
			if ( configType == 'I' || configType == 'U' || configType == 'D') queryCounters.totalIUDVisited++;
			return globalVariables;
		}

		globalVariables = executeTxn(request,response,writer,queryUnit,pool, queryCounters, globalVariables, 
									isFirst, configType, localAndGlobalVariables, isInFuncExec);
		if(queryUnit.isRecursive) {
			executeSteps(request, response, pool, querySteps, queryCounters, globalVariables, 
						isFirst, isFirstFunc, writer, isInFuncExec);
		}

		return globalVariables;
	}

	private final UnitVariables executeTxn(final Request request, final Response response, TxnScope writer, 
			final UnitQuery queryUnit, final String pool, final QueryCounters queryCounters, UnitVariables globalVariables,
			final IsFirst isFirst, final char configType,
			final UnitVariables localAndGlobalVariables, boolean isInFuncExec) throws SQLException, ErrorCodeExp 
	{
		variableAsssignment(queryUnit, localAndGlobalVariables);
		replaceCustomVars(queryUnit, localAndGlobalVariables, response);

		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";

		if ( configType == 'A' ) {

			if ( isFirst.isFirst ) isFirst.isFirst = false;
			else response.writeTextWithNoHeaderAndFooter(",");
			
			response.writeTextWithNoHeaderAndFooter("{\"key\" : \"" + queryUnit.appConfig.title +"\"" + "," );
			response.writeTextWithNoHeaderAndFooter("\"type\" : \"query\",");

			Object[] output = null;
			try
			{
				output = assignQuery(request, response, writer, pool, queryUnit, localAndGlobalVariables);
				int len = output.length;
				
				if ( DEBUG_ENABLED ) LOG.debug("Fetched Columns assignQuery length is " + len);
				
				String[] variableNames = queryUnit.appConfig.outvarA;
				
				if( null == variableNames ) {
					String errorMessage = "Output variables is not configured"; 
					LOG.warn(errorMessage);
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.QUERY_KEY,
							errorMessage, true);
					throw new SQLException();
				}
				
				if( 0 == len ) {
					output = new Object[variableNames.length];
					Arrays.fill(output, null);
					len = output.length;
				}
				
				if( len != variableNames.length ) {
					String errorMessage = "Output variables size does not match with fetched columns "
										+ "size in assignment query fetchedColumns != outputVariablesLen("
									    + len + "!=" + variableNames.length +")";
					LOG.warn(errorMessage);
					throw new SQLException(errorMessage);
				}
				
				/**
				 * Copy the assignment variables to the global variables.
				 */
				if ( null == globalVariables) globalVariables = new UnitVariables();
				StringBuilder sb = new StringBuilder(128);
				sb.append("\"values\" : [");
				for( int i = 0 ; i < len ; i++ ) {
					if( i > 0 ) sb.append(",");
					String variableValue = ( null == output[i] ) ? null : output[i].toString();
					sb.append("{\"" + variableNames[i] + "\":\"")
					.append( variableValue )
					.append("\"}");
					
					globalVariables.variablesM.put(variableNames[i], variableValue);
				}
				sb.append("], \"response\":\"data\" }");
				response.writeTextWithNoHeaderAndFooter(sb.toString());

			}
			catch(SQLException sqlEx)
			{
				response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				throw sqlEx;
			}
			
		} else if ( configType == 'S' ) {

			if ( isFirst.isFirst ) isFirst.isFirst = false;
			else response.writeTextWithNoHeaderAndFooter(",");

			response.writeTextWithNoHeaderAndFooter("{\"key\" : \"" + queryUnit.appConfig.title +"\"" + "," );
			response.writeTextWithNoHeaderAndFooter("\"type\" : \"query\",");
			try
			{
				selectQuery(request, response, writer, pool, queryUnit, localAndGlobalVariables);
				response.writeTextWithNoHeaderAndFooter(",\"response\" : \"data\"}");
			}
			catch(SQLException sqlEx)
			{
				response.setErrorMessage(queryUnit.appConfig.title , ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				throw new SQLException();
			}			

		} else {

			try
			{
				if('M' ==  configType) {
					
					UserProfile userProfile = request.getUser();
					String loginId = ( null == userProfile) ? "GUEST" : userProfile.loginid;
					String tenantId = ( null == userProfile) ? "-1" : userProfile.getTenantId() + "";
					queryUnit.appConfig.body = enrichQuery(queryUnit, loginId, tenantId, request, localAndGlobalVariables);
					
				} else {

					queryCounters.totalIUDVisited++;
					queryCounters.totalIUDExecuted++;

					if( queryCounters.totalIUDExecuted == 1 ) {
						if(DEBUG_ENABLED) LOG.debug("Begining transaction since IUD "
								+ "statements is greater than zero i.e [ " + queryCounters.iuds + " ]");
						writer.beginTransaction();
					} 
				}
				
				int affectedRows = insertOrdeleteOrUpdate(writer, request, response, queryUnit, pool);

				if ( isFirst.isFirst ) isFirst.isFirst = false;
				else response.writeTextWithNoHeaderAndFooter(",");
				
				StringBuilder sb = new StringBuilder(128);
				sb.append("{\"key\" : \"")
				.append(queryUnit.appConfig.title)
				.append("\",\"type\" : \"query\",\"values\" : [ {\"code\" :\"")
				.append(ErrorCodes.QUERY_EXECUTION_SUCCESS)
				.append("\",\"message\":\"")
				.append(ErrorMessages.getInstance().getMessage("en", ErrorCodes.QUERY_EXECUTION_SUCCESS))
				.append("\",\"title\":\"").append(queryUnit.appConfig.title)
				.append("\",\"affectedRows\":").append(affectedRows)
				.append("}], \"response\":\"resultCode\"}");
				response.writeTextWithNoHeaderAndFooter(sb.toString());
			}
			catch(SQLException sqlEx)
			{
				if ( sqlEx.getMessage().toLowerCase().contains("duplicate")) {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.DUPLICATE_ENTRY, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				} else {
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
				}
				throw sqlEx;
			} finally {
				if ( queryCounters.iuds > 0 && queryCounters.totalIUDVisited == queryCounters.iuds ) {
					boolean isInOpenTransaction = queryCounters.creates > 0;
					if ( DEBUG_ENABLED ) 
						LOG.debug("Committing transaction, Not waiting for select with isInOpenTransaction " + isInOpenTransaction);
					writer.commitTransaction(isInOpenTransaction);
				}
			}
		}
		return globalVariables;
	}	


	private final void selectQuery(final Request request, final Response response, final TxnScope txnScope, 
			final String pool, final UnitQuery queryUnit, UnitVariables finalVariables) throws SQLException
	{
		String docName = request.getString("docname", false, false, true);
		if ( StringUtils.isEmpty(docName)) docName = "document";

		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";


		UserProfile userProfile = request.getUser();
		String loginId = ( null == userProfile) ? "GUEST" : userProfile.loginid;

		String tenantId = ( null == userProfile) ? "-1" : userProfile.getTenantId() + "";

		String  query = enrichQuery(queryUnit, loginId, tenantId, request, finalVariables);

		if ( DEBUG_ENABLED ) LOG.debug("Enriched selectQuery => " + query);

		if ( formatType.equals("xml")) {
			ReadXml<Object> reader = new ReadXml<Object>(response.getWriter(), OBJECT_CLAZZ);
			reader.setPoolName(pool);
			if ( null != txnScope) txnScope.setReadOnTxnScope(reader);
			reader.docName = docName;
			response.writeHeader();
			reader.execute(query, queryUnit.params);
			response.writeFooter();
			return;
		} 		
		else if(formatType.equals("jsonp"))
		{
			ReadJsonArray jsonReader = new ReadJsonArray(response.getWriter());
			jsonReader.setPoolName(pool);
			if ( null != txnScope) txnScope.setReadOnTxnScope(jsonReader);
			response.format = formatType;
			response.writeHeader();
			jsonReader.execute(query,  queryUnit.params);
			response.writeFooter();
			return;
		}
		else if(formatType.equals("xlsx") || formatType.equals("xls"))
		{
			String templateFileName = DOWNLOAD_TEMPLATE_DIR +  queryUnit.appConfig.title + "." + formatType;
			int xlsFormat = (formatType.equals("xlsx") ) ? 0 : 1; 
			ReadXLS xlsReader = new ReadXLS(response.getBinaryWriter(), 0, xlsFormat);
			xlsReader.setPoolName(pool);
			if ( null != txnScope) txnScope.setReadOnTxnScope(xlsReader);
			xlsReader.setTemplateFile(templateFileName);
			xlsReader.execute(query, queryUnit.params);
			return;
		} 
		else {
			response.setErrorMessage("DATA_FORMAT_ERROR", ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.QUERY_KEY,
					ErrorMessages.getInstance().getMessage(
							userProfile.getLocale(), ErrorCodes.INVALID_DATA_FORMAT), true);
			return;
		}
	}

	private final Object[] assignQuery(final Request request, final Response response, final TxnScope txnScope, 
			final String pool, final UnitQuery queryUnit, UnitVariables finalVariables ) throws SQLException
	{

		UserProfile userProfile = request.getUser();
		String loginId = ( null == userProfile) ? "GUEST" : userProfile.loginid;
		String query = queryUnit.appConfig.body;

		/**
		 * Direct value assignment
		 */
		if ( query.length() > 7 ) {
			if ( ! (query.startsWith("select ") || query.startsWith("SELECT ") )) {
				throw new SQLException("Assign query does not start with select clause, given query [ " + query + " ]");
			}
		} else {
			throw new SQLException("Assign query is not proper, given query [ " + query + " ]");
		}

		String tenantId = ( null == userProfile) ? "-1" : userProfile.getTenantId() + "";

		query = enrichQuery(queryUnit, loginId, tenantId, request, finalVariables);

		if ( DEBUG_ENABLED ) LOG.debug("Enriched assignQuery => " + query);

		List<Object[]> outputList = null;
		Object[] output = null;
		try
		{
			ReadArray reader = new ReadArray();
			reader.setPoolName(pool);
			if ( null != txnScope) txnScope.setReadOnTxnScope(reader);
			outputList = (List<Object[]>) reader.execute(query, queryUnit.params);
			int size = outputList.size();
			if( size > 0 ) output = outputList.get(size - 1);
			else output = new Object[0];
		}
		catch(SQLException sqlEx)
		{
			setResponseSqlException(response, sqlEx, userProfile, queryUnit.appConfig.title);
			throw sqlEx;
		}

		return output;
	}

	private final void setResponseSqlException(final Response response, final SQLException sqlEx, final UserProfile user, final String errorTitle)
	{
		if ( sqlEx.getMessage().toLowerCase().contains("duplicate")) {
			response.setErrorMessage(errorTitle, ErrorCodes.DUPLICATE_ENTRY, ErrorCodes.QUERY_KEY,
					ErrorMessages.getInstance().getMessage(
							user.getLocale(), ErrorCodes.DUPLICATE_ENTRY), true);
		} else {
			response.setErrorMessage(errorTitle, ErrorCodes.UNEXPECTED_DATA_HANDLING, ErrorCodes.QUERY_KEY, sqlEx.getMessage(), true);
		}
	}

	private final int insertOrdeleteOrUpdate(TxnScope txnScope, final Request request, final Response response, 
			final UnitQuery queryUnit, final String pool) throws SQLException, ErrorCodeExp
	{
		String query = queryUnit.appConfig.body;
		if ( null == txnScope ) {
			LOG.warn("Transaction scope is null @ insertOrdeleteOrUpdate");
			String txnid = request.getString("txnid", false, false, true);
			Long txnId = ( null == txnid)? null : new Long(txnid).longValue();
			txnScope= TxnScopeFactory.getInstance().get(txnId, false, pool);
		}
		int affectedRows =  txnScope.execute(query, queryUnit.params);
		if ( DEBUG_ENABLED ) LOG.debug("Executed insertOrdeleteOrUpdate query title  > " + queryUnit.appConfig.title );
		return affectedRows; 
	}

	private final UserProfile isLoggedIn(final Request request, final Response response) 
	{
		if ( null == request.getUser()) {
			LOG.info("Guest is not authorized to perform this operation.");
			response.setErrorMessage("AUTHORIZATION_ERROR", ErrorCodes.UNAUTHORIZED_ACCESS, ErrorCodes.QUERY_KEY,
					ErrorMessages.getInstance().getMessage("en", ErrorCodes.UNAUTHORIZED_ACCESS)
					, true);
			return null;
		}

		if ( request.getUser().isGuest()) {
			LOG.info("Guest is not authorized to perform this operation.");
			response.setErrorMessage("AUTHORIZATION_ERROR",ErrorCodes.UNAUTHORIZED_ACCESS, ErrorCodes.QUERY_KEY,
					ErrorMessages.getInstance().getMessage("en", ErrorCodes.UNAUTHORIZED_ACCESS), true);
			return null;
		}

		UserProfile userProfile = request.getUser();
		return userProfile;
	}	

	private static final String enrichQuery(final UnitQuery queryUnit, final String loginId, 
			final String tenantId, final Request request, final UnitVariables queryVariables) throws SQLException
	{
		String query = queryUnit.appConfig.body;

		query = query.replace("__userid", loginId);
		query = query.replace("__tenantid", tenantId);


		//Sorting
		String sort = null;
		if ( null != queryUnit.sort ) {
			sort = queryUnit.sort;
		} else {
			if (  request.mapData.containsKey("sort") ) sort = request.mapData.get("sort");
		}
		if (  null != sort ) {
			query = query.replace("__sort", sort);
		} else {
			query = query.replace("order by __sort", "");
			query = query.replace("ORDER BY __sort", "");
		}

		//Filtering
		String whereReplace = "";
		String whereReplacePlaceHolder = " AND ( __where )";
		if ( null != queryUnit.where ) {
			whereReplace = queryUnit.where;
			whereReplacePlaceHolder = "__where";
		} else {
			if ( request.mapData.containsKey("where") ) {
				whereReplace = request.mapData.get("where");
				whereReplacePlaceHolder = "__where";
			}
		}		

		int queryL = query.length();
		query = query.replace(whereReplacePlaceHolder, whereReplace);
		int queryR = query.length();
		if ( queryL == queryR) {
			query = query.replace("__where", whereReplace);
		}


		//Offset
		if ( -1 != queryUnit.offset ) {
			query = query.replace("__offset", new Long(queryUnit.offset).toString());
		} else {
			if ( request.mapData.containsKey("offset")) {
				query = query.replace("__offset", request.mapData.get("offset"));
			} else {
				query = query.replace("offset __offset", "");
				query = query.replace("OFFSET __offset", "");
			}
		}


		//Limit
		if ( -1 != queryUnit.limit ) {
			query = query.replace("__limit", new Long(queryUnit.limit).toString());
		} else {
			if ( request.mapData.containsKey("limit")) {
				query = query.replace("__limit", request.mapData.get("limit"));
			} else {
				query = query.replace("limit __limit", "");
				query = query.replace("LIMIT __limit", "");
			}
		}		

		return query;
	}

	private static void replaceCustomVars(final UnitQuery queryUnit, final UnitVariables queryVariables, Response response)
			throws SQLException {
		/**
		 * Replace the Custom variables part of the query
		 */
		String query = queryUnit.appConfig.body;
		int cutomVarIndex = query.indexOf('&');
		if( -1 == cutomVarIndex ) return;
		
		if( null == queryVariables ) {
			String errMsg = "Query Variable are null but was required by query.";
			LOG.fatal(errMsg);
			response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING,ErrorCodes.QUERY_KEY,errMsg, true);
			throw new SQLException(errMsg);
		} else {
			List<String> customVarsName = queryUnit.appConfig.getCustomVars();
			
			if( null == customVarsName) {
				String errMsg = "Extracted custom variable are null but was required by query.";
				LOG.fatal(errMsg);
				response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING,ErrorCodes.QUERY_KEY,errMsg, true);
				throw new SQLException(errMsg);
			}

			if( DEBUG_ENABLED ) LOG.debug("Replacing following custom vars " + customVarsName);
			for (String customVar : customVarsName) {
				String variableVal = queryVariables.variablesM.get(customVar);
				if( null == variableVal ) {
					String errMsg = "[" + customVar + "] variable not initialized for query but was required";
					response.setErrorMessage(queryUnit.appConfig.title, ErrorCodes.UNEXPECTED_DATA_HANDLING,ErrorCodes.QUERY_KEY,errMsg, true);
					LOG.fatal(errMsg);
					throw new SQLException(errMsg);
				}
				query = query.replace("&" + customVar + "&", variableVal);
			}
			queryUnit.appConfig.body = query;
		}
	}

	private final static void variableAsssignment(final UnitQuery queryUnit, final UnitVariables localAndGlobalVariables ) 
	{
		/**
		 * Convert variables to prepared statement params.
		 */
		if ( null != localAndGlobalVariables) {
			if ( null != localAndGlobalVariables.variablesM) {
				if ( DEBUG_ENABLED ) LOG.debug("Converting variables to params : " 
						+ localAndGlobalVariables.variablesM.toString());
				List<String> vars = queryUnit.appConfig.getVars();
				if( null == vars ) return;

				if ( DEBUG_ENABLED ) LOG.debug("Variables in query are : " + vars.toString());

				for ( String key : vars ) {
					String variableVal = localAndGlobalVariables.variablesM.get(key);
					queryUnit.params.add(variableVal);
				}
			}
			if ( DEBUG_ENABLED ) LOG.debug("After Conversion Query params are : " + queryUnit.params.toString());
		}
	}

	private final UnitVariables singularQueryVariableAssignemnt(final Request request, final UnitQuery queryUnit) 
	{
		UnitVariables finalVariables = null;
		if ( ! StringUtils.isEmpty(queryUnit.appConfig.variables)) {
			finalVariables = new UnitVariables(queryUnit.appConfig.variables);
		}

		if ( request.mapData.containsKey("variables") ) {
			String variablesJson =  request.mapData.get("variables");
			if ( null == finalVariables ) finalVariables = new UnitVariables(variablesJson);  
			finalVariables.merge(variablesJson);
		}

		/**
		 * Overwrite with query specific variables
		 */
		if ( null != queryUnit.variables) {
			if ( null == finalVariables) finalVariables = new UnitVariables(queryUnit.variables);
			else finalVariables.merge(queryUnit.variables);
		}

		return finalVariables;
	}	

	private final boolean evaluateExpr(final UnitExpr singularExpr, final List<UnitExpr> andExprs, 
			final List<UnitExpr> orExprs , UnitVariables localAndGlobalVariables) 
	{
		boolean hasAnd = ( null != andExprs);
		boolean hasOr = ( null != orExprs);
		boolean hasSingular = ( null != singularExpr);

		if(!(hasAnd || hasOr || hasSingular)) return true;

		if ( DEBUG_ENABLED ) LOG.debug("Evaluating : " + 
				(hasAnd ? "and, " : "") + 
				(hasOr ? "or, " : "") + 
				(hasSingular ? "singular." : "") );

		boolean stateSingular = ( null == singularExpr) ? true : evaluateSingleExpr(singularExpr, localAndGlobalVariables);

		boolean stateAnd = true;
		if ( hasAnd) {
			for (UnitExpr expr : andExprs) {
				stateAnd = stateAnd &&  evaluateSingleExpr(expr, localAndGlobalVariables);
				if ( !stateAnd ) break;
			}
		}

		boolean stateOr = true;
		if ( hasOr) {
			boolean isFirst = true;
			for (UnitExpr expr : orExprs) {
				if ( isFirst ) {
					isFirst = false;
					stateOr = evaluateSingleExpr(expr, localAndGlobalVariables);
				} else {
					stateOr = stateOr ||  evaluateSingleExpr(expr, localAndGlobalVariables);
				}

			}
		}

		boolean finalState = false;
		if ( hasSingular ) {
			if ( hasAnd) {
				if ( hasOr) {
					finalState = stateAnd && stateOr && stateSingular;
				} else {
					finalState = stateAnd && stateSingular;
				}
			} else {
				if ( hasOr) {
					finalState = stateOr && stateSingular;
				} else {
					finalState = stateSingular;
				}
			}
		} else {
			if ( hasAnd) {
				if ( hasOr) {
					finalState = stateAnd && stateOr;
				} else {
					finalState = stateAnd;
				}
			} else {
				if ( hasOr) {
					finalState = stateOr;
				} else {
					finalState = true;
				}
			}
		}

		if ( DEBUG_ENABLED ) LOG.debug(
				"Final State : " + (hasAnd ? "(and = " + stateAnd + ") && " : "" ) +  (hasOr ? "(or = " + stateOr + ") && " : "" ) +
				(hasSingular ? "(singular = " + stateSingular +  ") = " : "" ) + " == " +  finalState);
		return finalState;

	}

	private final boolean evaluateSingleExpr(final UnitExpr expr, UnitVariables localAndGlobalVariables) 
	{
		if ( null == expr) return true;

		if ( null != expr.lhs) {
			if ( expr.lhs.length() > 1 ) {
				char firstChar = expr.lhs.charAt(0);
				if ( firstChar == '@' ) {
					if ( null != localAndGlobalVariables.variablesM ) {
						String variableName = expr.lhs.substring(1);
						LOG.debug("LHS variable is : " + variableName 
								+ " and value is : " + localAndGlobalVariables.variablesM.get(variableName));
						expr.lhs = localAndGlobalVariables.variablesM.get(variableName);
					} else {
						expr.lhs = null;
					}
				} else if ( firstChar == '_' ) {
					if ( expr.lhs.equals("__null")) expr.lhs = null; 
				}
			}
		}

		if ( null != expr.rhs) {
			if ( expr.rhs.length() > 1 ) {
				char firstChar = expr.rhs.charAt(0);
				if ( firstChar == '@' ) {
					if ( null != localAndGlobalVariables.variablesM ) {
						String variableName = expr.rhs.substring(1);
						LOG.debug("RHS variable is : " + variableName 
								+ " and value is : " + localAndGlobalVariables.variablesM.get(variableName));
						expr.rhs = localAndGlobalVariables.variablesM.get(variableName);
					} else {
						expr.rhs = null;
					}
				}
			}
		}

		if (DEBUG_ENABLED ) LOG.debug("Evaluating Single Expression : " + expr.toString());

		switch(expr.opr) {

		case "==" :
			if ( null == expr.lhs && null == expr.rhs) return true; 
			if ( null == expr.lhs || null == expr.rhs)  return false; 
			return ( expr.lhs.equals(expr.rhs)); 

		case "<>":
		case "!=":	
			if ( null == expr.lhs && null == expr.rhs) return false; 
			if ( null == expr.lhs || null == expr.rhs)  return true; 
			return ( ! expr.lhs.equals(expr.rhs)); 

		case "<=" :
		{
			if ( null == expr.lhs || null == expr.rhs) return false;
			Double l = Double.parseDouble(expr.lhs);
			Double r = Double.parseDouble(expr.rhs);
			return (  l.doubleValue() <= r.doubleValue());
		}

		case ">=" :	
		{
			if ( null == expr.lhs || null == expr.rhs) return false;
			Double l = Double.parseDouble(expr.lhs);
			Double r = Double.parseDouble(expr.rhs);
			return (  l.doubleValue() >= r.doubleValue());
		}


		case ">" :	
		{
			if ( null == expr.lhs || null == expr.rhs) return false;
			Double l = Double.parseDouble(expr.lhs);
			Double r = Double.parseDouble(expr.rhs);
			return (  l.doubleValue() > r.doubleValue());
		}

		case "<" :
		{
			if ( null == expr.lhs || null == expr.rhs) return false;
			Double l = Double.parseDouble(expr.lhs);
			Double r = Double.parseDouble(expr.rhs);
			return (  l.doubleValue() < r.doubleValue());
		}


		case "NOT IN" :
		{
			if ( null == expr.lhs || null == expr.rhs) return false;
			List<String> values = new ArrayList<>(6);
			LineReaderUtil.fastSplit(values, expr.rhs, ',');
			for (String aVal : values) {
				if ( aVal.equals(expr.lhs)) return false; 
			}
			return true;
		}


		case "IN" :
		{
			if ( null == expr.lhs || null == expr.rhs) return true;
			List<String> values = new ArrayList<>(6);
			LineReaderUtil.fastSplit(values, expr.rhs, ',');
			for (String aVal : values) {
				if ( aVal.equals(expr.lhs)) return true; 
			}
			return false;
		}

		default   : 
			return false;
		}
	}

	@SuppressWarnings("unused")
	private final String makeQuerySecure(final Request request, final Response response, String  query, final String variables, 
			final List<Object> paramL, final boolean allowUserScope, final boolean isUserOrTenantIdFirst) throws SQLException 
	{
		String loginId = ( null == request.getUser()) ? "anonymous" : request.getUser().loginid;
		String tenantId =  ( null == request.getUser()) ? "anonymous" :  request.getUser().getTenantId() + "";

		UserProfile user = request.getUser(); 

		int queryLenBefore = query.length(); 
		String replacedQuery = query.replace("__userid", " ? ");
		int queryLenAfter = replacedQuery.length();
		int totalReplacements =  (queryLenBefore - queryLenAfter)/ 5;

		queryLenBefore = replacedQuery.length(); 
		replacedQuery = replacedQuery.replace("__tenantid", " ? ");
		queryLenAfter = replacedQuery.length();
		int totalReplacementsTenantId =  (queryLenBefore - queryLenAfter)/ 7;

		if((totalReplacements == 0 || totalReplacementsTenantId == 0) && allowUserScope)
		{
			if ( DEBUG_ENABLED ) LOG.debug("SECURITY_COMPROMISED: Query Scope not limited to User");
			response.setErrorMessage("AUTHORIZATION_ERROR",ErrorCodes.UNAUTHORIZED_USER_ACCESS, ErrorCodes.QUERY_KEY,
					ErrorMessages.getInstance().getMessage(user.getLocale(), 
							ErrorCodes.UNAUTHORIZED_USER_ACCESS), true);
			throw new SQLException(query);
		}

		/**
		 * If there is only one replacement, add this to beginning
		 * Helps on making prepared statements
		 * We can not pass it inside parameters due to security risk.
		 */

		if(totalReplacements == 1 && totalReplacementsTenantId == 0)
		{
			if(isUserOrTenantIdFirst) paramL.add(0, loginId);
			else paramL.add(loginId);
			query = replacedQuery;

		}
		else if( totalReplacements == 0 && totalReplacementsTenantId == 1 )
		{
			if(isUserOrTenantIdFirst) paramL.add(0, tenantId);
			else paramL.add(tenantId);
			query = replacedQuery;
		}
		else if( ( totalReplacements == 1 && totalReplacementsTenantId == 1 ) )
		{

			if(isUserOrTenantIdFirst)
			{
				paramL.add(0, loginId);
				paramL.add(1, tenantId);
			}
			else
			{
				paramL.add(loginId);
				paramL.add(tenantId);

			}
			query = replacedQuery;
		}

		query = query.replace("__userid", loginId);
		query = query.replace("__tenantid", tenantId);

		if ( DEBUG_ENABLED ) {
			if ( DEBUG_ENABLED ) LOG.debug("Final Query:" + query);
			if ( DEBUG_ENABLED ) LOG.debug("paramL:" + paramL.size() + " , loginId=" + loginId);
			String params = "Params \r\n";
			for (Object param : paramL) {
				params = params + ">>>" + param + "\r\n";
			}
			if ( DEBUG_ENABLED ) LOG.debug(params);
		}
		return query;
	}

	private final void sendSuccessMessages(final Request request, final Response response, final boolean isOK) 
	{
		if (isOK) {
			String formatType = request.getString("format", false, false, true);
			if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";
			response.writeTextWithNoHeaderAndFooter("[");
			response.writeTextWithHeaderAndFooter(Response.getError(
					"xml".equals(formatType), ErrorCodes.QUERY_EXECUTION_SUCCESS, sucessMsg, "QUERY_SUCESS", ErrorCodes.QUERY_KEY, null));
			response.writeTextWithNoHeaderAndFooter("]");
		} else {
			response.setErrorMessage("API_REFRESH_ERROR", ErrorCodes.DATA_API_REFRESH_FAILURE, ErrorCodes.QUERY_KEY, "Unsucessful processing.", true);
		}

	}

	private final void executeStoredProc(final Request request, final Response response) throws ErrorCodeExp, IOException, SQLException
	{   
		
		PrintWriter out = response.getWriter();
		String poolName = request.getString("pool", false, false, true);
		String formatType = request.getString("format", false, false, true);
		String query = request.getString("query", false, false, true);
		
		List<UnitStep> spSteps = new ArrayList<UnitStep>(8);
		String StepId = null;
		boolean isFirst = true;
		
		try {
		SqlSensorInputParser.fromJson(query, spSteps);
		UnitVariables globalVariables = new UnitVariables(); 
		if ( request.mapData.containsKey("variables") ) {
			String variablesJson =  request.mapData.get("variables");
			globalVariables.merge(variablesJson);
		}
		
		out.write("[");
	    for(UnitStep spStep : spSteps )
	    {
		   if(!isFirst) out.write(",");
	       globalVariables = executeSps(request, response, poolName, spStep, globalVariables,out);
	       isFirst = false;
		   StepId = spStep.stepId;
		}
	    
		if ( globalVariables.variablesM.size() > 0 ) 
		{
			StringBuilder varSb = new StringBuilder(128);
			boolean isFirstVar = true;
			for(String key : globalVariables.variablesM.keySet())
			{
				if ( isFirstVar ) isFirstVar = false;
				else varSb.append(',');
					
				String value = globalVariables.variablesM.get(key);
				value = (null == value) ? "" : value;
				varSb.append("{\"").append(key)
					.append("\":\"").append(value).append("\"}");
			}
			out.write(",{\"key\" : \"outputvariables\", \"type\" : \"variables\", \"values\" : [" + varSb.toString() + "]}");
		}
		out.write("]");
		out.flush();
		out.close();

	    
		} catch(Exception ex){
			response.writeTextWithNoHeaderAndFooter(Response.getMsg("xml".equals(formatType), false, ErrorCodes.ERROR_IN_SP_EXECUTION, 
					ex.getMessage(),StepId, ErrorCodes.SP_KEY, true, true,null));
		}
	 }

	private UnitVariables executeSps(Request req, Response res,String poolName, UnitStep spStep, UnitVariables globalVariables,
								PrintWriter out) throws ErrorCodeExp,SQLException 
	{
		UnitSp sp = (UnitSp)spStep;
		
		/**
		 * Overwrite global variable with query specific local variables
		 */
		
		UnitVariables localAndGlobalVariables = (null == globalVariables) ? null : globalVariables;
		LOG.debug("local and global variables are : " +localAndGlobalVariables);
		
		if ( null != sp.variables) {
			LOG.debug("SP VARIABLES ARE : "+sp.variables );
			if ( null == localAndGlobalVariables) localAndGlobalVariables = new UnitVariables(sp.variables);
			else localAndGlobalVariables.merge(sp.variables);
		}

		if ( DEBUG_ENABLED ) LOG.debug("Local and Global Merged Variables:" + 
				(( null == localAndGlobalVariables)? "Null" : localAndGlobalVariables.toString()));

		if( null != sp.inputVars ) {
			if(DEBUG_ENABLED) LOG.debug("Stored proc input params are : " + sp.inputVars.toString());
			for ( String key : sp.inputVars ) {
				String variableVal = localAndGlobalVariables.variablesM.get(key);
				if( null == variableVal ) throw new ErrorCodeExp("VARIABLE_VALUE_MISSING", 
						ErrorCodes.INVALID_INPUT_PARAMETER, "Variable value not passed for variable " + key, ErrorCodes.SP_KEY);
				sp.inParams.add(variableVal);
			}
			if(DEBUG_ENABLED) LOG.debug("Stored proc call params are : " + sp.inParams.toString());
		}
			
		if(DEBUG_ENABLED) {
			if( null == sp.queryOutVars ) LOG.debug("Stored proc out params is empty");
			else LOG.debug("Stored proc out params are : " +sp. queryOutVars.toString());
		}
		
	
		StoredProcOutParam errorParam = sp.spId.getErrorParam();
		ReadStoredProc storedProcReader = new ReadStoredProc(out, spStep.stepId, sp.queryOutVars, errorParam);
		storedProcReader.setPoolName(poolName);
		storedProcReader.execute(sp.spId.spCallSyntax, sp.inParams, sp.queryOutVars);
	
		if( null != sp.queryOutVars ) {
			for (StoredProcOutParam outParam :sp.queryOutVars) {
				if( outParam.isError ) continue; 
				String aliasName = localAndGlobalVariables.variablesM.get(outParam.outParamName);
				if(null == aliasName )
					globalVariables.variablesM.put(outParam.outParamName, outParam.outParamValue.toString());
				else 
					globalVariables.variablesM.put(aliasName, outParam.outParamValue.toString());
				
			}
		}
		
		return globalVariables;
		
	   }

	private void initializePool(Request request, Response response) {

		String jsonData = request.mapData.get("jsonData"); 
		JsonObject jsonObject = new JsonParser().parse(jsonData).getAsJsonObject();
		
		String agentIP = jsonObject.getAsJsonPrimitive("agentIP").getAsString();
		String poolName = jsonObject.getAsJsonPrimitive("poolName").getAsString();
		
		try {
			DbConfigUtil.setupPoolByPoolName(agentIP, poolName);
			response.writeTextWithNoHeaderAndFooter("OK");
		} catch (Exception e) {
			LOG.warn("Error initializing drone user pool for ip - " + agentIP);
		}
	}
	
	private final void getBuildVersion(final Request request, final Response response) throws Exception
	{
		if ( INFO_ENABLED ) LOG.info("Getting current build version from manifest.. ");

		Class<SqlSensor> clazz = SqlSensor.class;
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if ( classPath.startsWith("jar") )
		{
			String manifestFilePath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +  "/META-INF/MANIFEST.MF";
			Manifest manifest = new Manifest(new URL(manifestFilePath).openStream());
			Attributes attributes = manifest.getMainAttributes();

			StringBuilder sb = new StringBuilder(128);
			sb.append("{\"values\":[{");

			boolean isFirst = true;
			for ( Object name : attributes.keySet() )
			{
				if ( isFirst ) isFirst = false;
				else sb.append(",\n");

				String val = ( null == attributes.get(name) ) ? "": attributes.get(name).toString();
				val = val.replace("\"", "\\\\\"");
				sb.append("\"").append(name.toString()).append("\":\"").append(val).append("\"");
			}
			sb.append("}]}");
			response.writeTextWithHeaderAndFooter(sb.toString());
		}
		else
		{
			if ( INFO_ENABLED ) LOG.info("Unable to find manifest information.");
			response.setErrorMessage("SYSTEM_ERROR", ErrorCodes.SYSTEM_CONFIGURATION_FAILURE, ErrorCodes.QUERY_KEY, ErrorMessages.getInstance().getMessage(request.getUser().getLocale(), ErrorCodes.SYSTEM_CONFIGURATION_FAILURE), true);
		}
	}

	@Override
	public final String getName() {
		return "sql";
	}

}
