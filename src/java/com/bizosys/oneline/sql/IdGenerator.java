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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bizosys.oneline.dao.IPool;
import com.bizosys.oneline.dao.PoolFactory;
import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.StringUtils;
import com.bizosys.oneline.web.sensor.Request;
import com.bizosys.oneline.web.sensor.Response;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class IdGenerator implements IIdGenerator {

	private static final String AMOUNT_STR = "amount";
	private static final String SEQUENCE_STRING = "sequence";
	private static final String SEQUENCES_STRING = "sequences";
	private final static Logger LOG = Logger.getLogger(IdGenerator.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	
	private static final String SEQUENCE_GENERATE_ID = "generateid";
	private static final String SEQUENCE_GENERATE_IDS = "generateids";
	
	/**
	* 
	* @param request ("{\"sequences\": [{\"sequence\": \"seq1\",\"amount\": 12},{\"sequence\": \"seq2\",\"amount\": 19},{\"sequence\": \"seq3\",\"amount\": 112}]}";)
	* @param response
	* @throws Exception
	*/
	public void generateIds(Request request, Response response) throws Exception
	{
		Gson gson = new Gson();
		String json = request.getString("query", true, false, false);
		JsonElement jsonElem = gson.fromJson(json, JsonElement.class);
		JsonObject jsonObj = jsonElem.getAsJsonObject();
		Iterator<JsonElement> queriesIterator = jsonObj.get(SEQUENCES_STRING).getAsJsonArray().iterator();
		
		
		Map<String, Integer> sequences = new HashMap<String, Integer>();
		while(queriesIterator.hasNext())
		{
			JsonObject queryObj = (JsonObject) queriesIterator.next();
			String seqId = queryObj.get(SEQUENCE_STRING).getAsString();
			String amt = queryObj.get(AMOUNT_STR).getAsString();
			int amount = -1;
			try {
				amount = Integer.parseInt(amt);
			} catch (NumberFormatException ex) {
				LOG.info("Sequence amount should be an integer", ex);
				response.setErrorCode(SEQUENCE_GENERATE_IDS, ErrorCodes.SEQUENCE_AMOUNT_ATLEAST_ONE, "en", true);
			}
			
			if ( amount == 0 ) {
				response.setErrorCode(SEQUENCE_GENERATE_IDS,ErrorCodes.SEQUENCE_AMOUNT_ATLEAST_ONE, "en", true);
				return;
			}
			
			sequences.put(seqId, amount);
		}
		
		
		/**
		* FIND NEXT SEQUENCES
		*/
		
		boolean isError = false;
		Map<String, Integer> startIds = new HashMap<String, Integer>();

		String poolName = request.getString("pool", false, false, true);
		IPool pool = ( StringUtils.isEmpty(poolName)) ?  PoolFactory.getDefaultPool() : PoolFactory.getInstance().getPool(poolName, false);
		
		Connection conn = null;
		PreparedStatement stmtU = null;
		PreparedStatement stmtS = null;
		ResultSet rs = null;
		
		String queryU = "update idsequence set sequenceno = sequenceno + ? where sequencename = ?";
		String queryS = "select sequenceno from idsequence where sequencename = ?";
		
		String idsequenceProccessing = null;
		
		try {
			conn = pool.getConnection();
			conn.setAutoCommit(false);
			
			stmtU = conn.prepareStatement(queryU);
			stmtS = conn.prepareStatement(queryS);
			
			for (String idsequence : sequences.keySet()) {

				idsequenceProccessing = idsequence;
				
				stmtU.setInt(1, sequences.get(idsequence) ) ;
				stmtU.setString(2, idsequence ) ;
				stmtU.execute();
				
				stmtS.setString(1, idsequence ) ;
				stmtS.execute();
				
				rs = stmtS.getResultSet();

				if ( rs.next() ) {
				
					int startId = rs.getInt(1) - sequences.get(idsequence);
					startIds.put(idsequence, startId);
				
				} else {
				
					conn.rollback();
					isError = true;
					startIds.clear();
					LOG.warn("Sequence Id is not setup for :" + idsequence);
					response.setErrorMessage(SEQUENCE_GENERATE_IDS, ErrorCodes.SEQUENCE_ID_NOTSETUP, ErrorCodes.SEQUENCE_GENERATION_KEY,
			       			ErrorMessages.getInstance().getMessage(
			       				request.getUser().getLocale(), ErrorCodes.SEQUENCE_ID_NOTSETUP), true);
				
				}
				rs.close(); rs = null;
			}
			if ( null != conn && !isError ) conn.commit();
			
		} catch (SQLException ex) {
		
			if ( null != conn ) conn.rollback();
			LOG.warn( "Error during id creation [" + idsequenceProccessing + "] under pool [" + poolName + "]", ex);
			throw ex;
			
		} finally {
			for (AutoCloseable resource : new AutoCloseable[] {rs,stmtS,stmtU,conn}) {
				try { 
					if ( null != resource) resource.close(); 
				} catch (Exception ex) {
					LOG.warn(ex);
				}
			}
		}
		
		if ( !isError ) {
			/**
			* Send the Ids to client
			*/
			String formatType = request.getString("format", false, false, true);
			if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";
			
			if(formatType.equals("jsonp"))
			{
				/**
				* MAKE JSON
				*/
				StringBuilder sb  = new StringBuilder(128);
				boolean isFirst = true;
				
				sb.append("[{\"key\":\"")
				.append(SEQUENCE_GENERATE_IDS)
				.append("\", \"type\" : \"SequenceGeneration\", \"values\" : [");
				for(String seqId : startIds.keySet())
				{
					if ( isFirst ) isFirst = false;
					else sb.append(',');
					
					sb.append("{\"").append(SEQUENCE_STRING).append("\":\"").append(seqId).append('"')
						.append(",\"start\":").append(startIds.get(seqId)).append("}");
				}
				sb.append("],\"response\" : \"data\"}]");
				
				response.writeTextWithHeaderAndFooter(sb.toString());
				return;
	       }
			else {
	       	response.setErrorMessage(SEQUENCE_GENERATE_IDS, ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.SEQUENCE_GENERATION_KEY,
	       			ErrorMessages.getInstance().getMessage(
	       				request.getUser().getLocale(), ErrorCodes.INVALID_DATA_FORMAT), true);
				return;
			}
		}

	}
	
	/**
	* Generate only 1 Id
	* @param request
	* @param response
	* @throws Exception
	*/
	public void generateId(Request request, Response response) throws Exception
	{

		String idsequence = request.getString(SEQUENCE_STRING, true, false, false);
		int amount = request.getInteger(AMOUNT_STR, 1);
		
		if ( DEBUG_ENABLED ) LOG.debug("idsequence:" + idsequence);
		
		long startId = -1;
		String query = "update idsequence set sequenceno = sequenceno + ? where sequencename = ?";
		String poolName = request.getString("pool", false, false, true);
		IPool pool = ( StringUtils.isEmpty(poolName)) ?  PoolFactory.getDefaultPool() : PoolFactory.getInstance().getPool(poolName, false);
		
		Connection conn = null;
		PreparedStatement stmtU = null;
		PreparedStatement stmtS = null;
		ResultSet rs = null;
		
		try {
			conn = pool.getConnection();
			stmtU = conn.prepareStatement(query);
			stmtU.setInt(1, amount ) ;
			stmtU.setString(2, idsequence ) ;
			
			stmtU.execute(); stmtU.close(); stmtU = null;
			
			stmtS = conn.prepareStatement("select sequenceno from idsequence where sequencename = ?");
			stmtS.setString(1, idsequence ) ;
			stmtS.execute(); 
			rs = stmtS.getResultSet();
			
			String error = null;
			if ( rs.next() ) {
				startId = rs.getInt(1) - amount;
			} else {
				error = ErrorCodes.SEQUENCE_ID_NOTSETUP;
			}
			
			rs.close(); rs = null;
			stmtS.close(); stmtS = null;
			conn.close(); conn = null; //Returns the connection
			
			if ( null != error) {
				
				response.setErrorMessage(SEQUENCE_GENERATE_IDS, ErrorCodes.SEQUENCE_ID_NOTSETUP, ErrorCodes.SEQUENCE_GENERATION_KEY,
		       			ErrorMessages.getInstance().getMessage(
		       				request.getUser().getLocale(), ErrorCodes.SEQUENCE_ID_NOTSETUP), true);
				return;
			}
			
		} catch (SQLException ex) {
			LOG.fatal("Error during idgenerator." , ex);
			for (AutoCloseable closable : new AutoCloseable[]{rs,stmtU,stmtS,conn}) {
				if ( null != closable ) {
					try {
						closable.close();
					} catch (Exception cex) {
						LOG.warn( "Closing issue during id creation [" + idsequence + "] , for amount [" +  amount + "] under pool [" + poolName + "]");
					}
				}
			}
			throw ex;
		}
		
		if ( startId == -1) {
			throw new SQLException( "Not able to generate id [" + idsequence + "] , for amount [" +  amount + "] under pool [" + poolName + "]");
		}
		
		String formatType = request.getString("format", false, false, true);
		if ( StringUtils.isEmpty(formatType)) formatType = "jsonp";
		
		if ( formatType.equals("xml")) {
			response.writeTextWithNoHeaderAndFooter("<result><key>generateid</key><type>query</type><idsequence>" + idsequence + "</idsequence><starting>" + startId + "</starting><response>data</response></result>");
			return;
		} 		
		else if(formatType.equals("jsonp"))
		{
			StringBuilder sb = new StringBuilder(128);
			sb.append("[{\"key\":\"")
			.append(SEQUENCE_GENERATE_ID)
			.append("\", \"type\" : \"SequenceGeneration\", \"values\" : [ {\"idsequence\":\"")
			.append(idsequence)
			.append("\",\"starting\":")
			.append(startId)
			.append("}],\"response\" : \"data\"}]");
			response.writeTextWithHeaderAndFooter(sb.toString());
			return;
        }
		else {
        	response.setErrorMessage(SEQUENCE_GENERATE_ID, ErrorCodes.INVALID_DATA_FORMAT, ErrorCodes.SEQUENCE_GENERATION_KEY,
        			ErrorMessages.getInstance().getMessage(
        				request.getUser().getLocale(), ErrorCodes.INVALID_DATA_FORMAT), true);
			return;
		}
			
	}
	
}
