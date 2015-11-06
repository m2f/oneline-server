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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExecuteSqlFile {

	/**
	 * @param args
	 */
	private final static Logger LOG = LogManager.getLogger(SqlSensor.class);
	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
	public static void main(String[] args) {
		
		ExecuteSqlFile sqlFile = new ExecuteSqlFile(null);
		String fileName = sqlFile.createTempFile();
		sqlFile.downloadUrl("http://india.gov.in/", fileName);
		sqlFile.executeSqlFile(fileName);
	}
	
	public String createTempFile() {
		Long filePrefix = new Random(1000000).nextLong();
		String fileName = "/tmp/" + filePrefix.toString() + ".sql";
		return fileName;
	}
	
	public void deleteTempFile(String fileName) {
		try {
			new File(fileName).delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private TxnScope scope = null; 
	
	public ExecuteSqlFile(TxnScope scope) {
		this.scope = scope;
	}

	public boolean downloadUrl(String  urlName, String fileName) {
		URL url = null;
		OutputStream output = null;
		InputStream input = null;
		try {
			try {
				url = new URL(urlName);
				URLConnection con = url.openConnection();
				input = con.getInputStream();
			} catch (FileNotFoundException fnfe) {
				System.err.println("Unknown File:" + fnfe);
				return false;
			} catch (UnknownHostException uhe) {
				System.err.println("Unknown Url:" + urlName);
				return false;
			} catch (Exception ex) {
				System.err.println("Invalid Url:" + urlName);
				ex.printStackTrace();
				return false;
			}
			
			output = new BufferedOutputStream(( new FileOutputStream(fileName) ) );
			writeToAFile(output, new BufferedInputStream(input));
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			for ( Closeable closable: new Closeable[]{input,output} ) {
				if ( null != closable ) {
					try {closable.close();}catch (Exception ex){}
				}
			}
		}
	}
	
	public boolean executeSqlFile(String fileName) {
		
		File aFile = new File(fileName);
		BufferedReader reader = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(aFile); 
			reader = new BufferedReader ( new InputStreamReader (stream) );
			String line = null;
			
			scope.beginTransaction();
			while((line=reader.readLine())!=null) {
				if (line.length() == 0) continue;
				char first=line.charAt(0);
				switch (first) {
					case '\n' : // skip blank & comment lines
					continue;
				}
				if(DEBUG_ENABLED) LOG.debug("line is :" +line.toString());
				//Execute in a 
				scope.execute(line);
			}
			scope.commitTransaction();
			return true;
		} catch (Exception ex) {
			try {
				scope.rollbackTransaction();
			} catch (SQLException e) {
				e.printStackTrace();
				//Eat and print warn
			}
			ex.printStackTrace();
			return false;
		} finally {
			for ( Closeable closable: new Closeable[]{stream} ) {
				if ( null != closable ) {
					try {closable.close();}catch (Exception ex){}
				}
			}
		}
	}
	
	public boolean executeSqlText(String text) {
		
		if(DEBUG_ENABLED) LOG.debug("text is :" +text);
		InputStream stream = null;
		
		try {
			stream = new ByteArrayInputStream(text.getBytes()); 
			BufferedReader reader = new BufferedReader ( new InputStreamReader (stream) );
			String line = null;
			
			scope.beginTransaction();
			while((line=reader.readLine())!=null) {
				if (line.length() == 0) continue;
				char first=line.charAt(0);
				switch (first) {
					case '\n' : // skip blank & comment lines
					continue;
				}
				if(DEBUG_ENABLED) LOG.debug("line is :" +line.toString());
				//Execute in a 
				scope.execute(line);
			}
			scope.commitTransaction();
			return true;
		} catch (Exception ex) {
			try {
				scope.rollbackTransaction();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ex.printStackTrace();
			return false;
		} finally {
			for ( Closeable closable: new Closeable[]{stream} ) {
				if ( null != closable ) {
					try {closable.close();}catch (Exception ex){}
				}
			}
		}
	}	
	
	public void writeToAFile(OutputStream os, InputStream is) 
	{
		try 
		{
			int chunk = 1024;
			byte[] bytes = new byte[chunk];
			boolean isAvailable = true;

			while(isAvailable) 
			{
				int packet = is.read(bytes,0,chunk);
				if ( -1 == packet ) isAvailable  = false; 
				else os.write(bytes,0,packet);
			}
		} 
		catch(IOException e) 
		{
			System.err.println(e);
		} 
		finally
		{
			if(null != is) try { is.close(); } catch(Exception e) {}
		}
	}
	
	public void writeToAFile(String fileName, String text) 
	{
		FileWriter fw = null;
		try 
		{
			File file = new File(fileName);
			if ( file.exists()) {
				if ( file.canWrite()) {
					
				}
			} else {
				
			}
			
			fw = new FileWriter(file,false);
			fw.write(text);
		} 
		catch(IOException e) 
		{
			System.err.println(e);
		} 
		finally
		{
			if ( null != fw) try { fw.close(); } catch (Exception ex) {ex.printStackTrace();}
		}
	}	

}
