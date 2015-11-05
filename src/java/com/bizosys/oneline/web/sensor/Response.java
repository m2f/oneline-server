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

package com.bizosys.oneline.web.sensor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.apache.log4j.Logger;

import com.bizosys.oneline.util.ErrorCodes;
import com.bizosys.oneline.util.ErrorMessages;
import com.bizosys.oneline.util.StringUtils;
 

public class Response { 

	private PrintWriter out = null;
    public String callback = StringUtils.Empty;
    public String format = StringUtils.Empty; //JSONP, XML, XSL, CSV

	static final int FORMAT_TEXT_OR_HTML = -1;
	static final int FORMAT_XML = 0;
	static final int FORMAT_JSONP = 1;
	static final int FORMAT_XSL = 2;
	static final int FORMAT_CSV = 3;
    int formatIndex = FORMAT_TEXT_OR_HTML;
	
    public Object data = null;
    private String errorMessage = null;
    public String errorCode = null;
    private String errorTitle = null;
    private String parentTitle = null;
    private String errorType = null;
    public Boolean isError = false;
    public boolean skipErrorRewrite = false;
    
    private static final Logger LOG = Logger.getLogger(Response.class);

	private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
    
	private OutputStream os = null;
	
	private StringWriter sw =null;
	
    public PrintWriter getWriter() {
    	return this.out;
    }
    
    public OutputStream getBinaryWriter() {
    	return this.os;
    }

    public Response(PrintWriter pw, OutputStream os) {
    	this.sw = new StringWriter();
    	this.out = new PrintWriter(this.sw);
    	this.os = os;
    }

    public String getOutData()
    {
    	return this.sw.toString();
    }
    
    public boolean hasNoErrors()
    {
    	return !(this.isError);
    }
    
    public void writeTextWithNoHeaderAndFooter(String result) {
    	out.println(result);
    }

    public void writeTextWithHeaderAndFooter(String xmlText) {
    	writeHeader();
    	out.println(xmlText);
    	writeFooter();
    }

    public void writeTextListWithHeaderAndFooter(@SuppressWarnings("rawtypes") List serializeL) {
    	writeHeader();
    	if ( null != serializeL ){ 
	    	int serializeT = serializeL.size();
	    	for ( int i=0; i<serializeT; i++ ) {
	        	out.println(serializeL.get(i).toString());
	        	if ( DEBUG_ENABLED ) LOG.debug("\n" + serializeL.get(i) + "\n");
	    	}
    	}
    	writeFooter();
    }

    public void writeXMLArray(String[] xmlStrings, String tag) {
    	writeHeader();
    	StringBuilder sb = new StringBuilder(100);
    	if ( null != xmlStrings ){ 
	    	int serializeT = xmlStrings.length;
	    	
	    	for ( int i=0; i<serializeT; i++ ) {
	        	sb.append('<').append(tag).append('>');
	        	sb.append(xmlStrings[i]);
	        	sb.append("</").append(tag).append('>');
	        	out.println('<');
	    		out.println(sb.toString());
        		LOG.debug(sb.toString());
        		sb.delete(0, sb.capacity());
	    	}
    	}
    	writeFooter();
    }

    public void setErrorCode(String errorTitle, String errorCode, String language, boolean isError) 
    {
    	if ( this.isError ) return;
    	
   		this.isError = true;
   		this.errorTitle = errorTitle;
   		this.errorMessage = ErrorMessages.getInstance().getMessage(language, errorCode);
		this.errorCode = errorCode;
		
		if ( isError ) System.out.println("Error : " + this.errorMessage + " > " + this.errorCode);
    }

    public void setErrorCode(String errorTitle, String errorCode, String errorType, String language, boolean isError) 
    {
    	if ( this.isError ) return;
    	
   		this.isError = true;
   		this.errorTitle = errorTitle;
   		this.errorType = errorType;
		this.errorMessage = ErrorMessages.getInstance().getMessage(language, errorCode);
		this.errorCode = errorCode;
		
		if ( isError ) System.out.println("Error : " + this.errorMessage + " > " + this.errorCode);
    }

    public void setErrorMessage(String errorTitle, String errorCode, String errorType, String message, boolean isError) 
    {
    	if ( this.isError ) return;

    	this.isError = true;
    	this.errorTitle = errorTitle;
		this.errorCode = errorCode;
		this.errorType = errorType;
		this.errorMessage = message;
		if ( isError ) System.out.println("Error : " + this.errorMessage + " > " + this.errorCode);
	}

    public void setErrorParentTitleAndType(String parentTitle, String errorType) 
    {
    	this.parentTitle = parentTitle;
		this.errorType = errorType;
	}

    
    public void setErrorMessage(String errorTitle, String errorCode, String message, boolean isError) 
    {
    	if ( this.isError ) return;
    	setErrorMessage(errorTitle, errorCode, ErrorCodes.QUERY_KEY, message, isError);
	}

    public void setErrorMessage(String errorTitle, String errorCode, String errorType, String message, Exception ex) 
    {
   		this.isError = true;
   		this.errorTitle = errorTitle;
   		this.errorType = errorType;
		this.errorCode = errorCode;
		this.errorMessage = message;
		if ( null != ex ) LOG.error(message, ex);
	}

    public void setErrorMessage(String errorTitle, String errorCode, String message, Exception ex) 
    {
    	setErrorMessage(errorTitle, errorCode, ErrorCodes.QUERY_KEY, message, ex);
	}

    public String getError(boolean isXml) 
    {
    	return getError(isXml, false, this.errorCode, this.errorMessage, this.errorTitle, this.errorType, this.parentTitle);
    }

    public String getError(boolean isXml, boolean isSqlError) 
    {
    	return getError(isXml, false, this.errorCode, this.errorMessage, this.errorTitle,this.errorType, true, isSqlError, this.parentTitle);
    }

    
    /**
     * We will support JSONP, XML and XSL type headers.
     * XSL will render the output using a XSL file.
     * JSONP and XML calls appropriate callback function.
     * In JSONP whole array is passed to the callback function.
     * XML it is stamped as message id.
     */
    public void writeHeader() {
    	StringBuilder sb = new StringBuilder(100);

    	if ( ! StringUtils.isEmpty(format) ) {
    		if ( format.length() == 5 ) formatIndex = FORMAT_JSONP;
    		else if ( format.equals("xml") )  formatIndex = FORMAT_XML;
    		else formatIndex = FORMAT_XSL;
    	}
    	
    	switch ( formatIndex ) {
    		case FORMAT_XML:
    		   	if ( StringUtils.isEmpty(this.callback) ) {
    	    		sb.append("<result>");
    	    	} else {
    	    		sb.append("<result callback=\"").append(this.callback).append("\" >");
    	    	}
    		   	break;
    		
    		case FORMAT_JSONP:
    		   	if ( ! StringUtils.isEmpty(this.callback) )
    		   		sb.append(this.callback).append('(');
    		   	break;
    		
    		case FORMAT_XSL:
        		sb.append("<?xml version=\"1.0\" ?>");
        		sb.append("<?xml-stylesheet type=\"text/xsl\" href=\"");
        		sb.append(this.callback); //This is the XSL file name
        		sb.append("\" ?>");
        		sb.append("<result>");
    		   	break;
    		
    		default: //HTML and CSV formats.
    		   	break;
    	}
    	
    	out.print(sb.toString());
    	sb.delete(0, sb.capacity());
    }
    
    /**
     * Footer at the end of the result
     */
    public void writeFooter() {
    	switch ( formatIndex ) {
			case FORMAT_XML:
		    	out.print("</result>");
			   	break;
			
			case FORMAT_JSONP:
			   	if ( ! StringUtils.isEmpty(this.callback) ) out.print(");");
			   	break;
			   	
			case FORMAT_XSL:
		    	out.print("</result>");
			   	break;

			default: //HTML and CSV formats.
    		   	break;
	    }
    }
    
    
    public static String getError(boolean isXml, String errorCode, String errorMessage, String errorTitle, String errorType, String parentTitle) 
    {
    	return getError(isXml, false, errorCode, errorMessage, errorTitle, errorType, parentTitle);
    }

    public static String getError(boolean isXml, boolean isHeader, String errorCode, String errorMessage, String errorTitle, String errorType,  String parentTitle) 
    {
    	return getError(isXml, isHeader, errorCode, errorMessage, errorTitle, errorType, true, false, parentTitle);
    }
    
    public static String getError(boolean isXml, boolean isHeader, String errorCode, String errorMessage, 
    							String errorTitle, String errorType, boolean isTitle, boolean skipWriteError, String parentTitle) 
    {
    	if ( skipWriteError ) return "";
    	
    	if ( null == parentTitle) parentTitle = errorTitle;
    	
    	if ( isXml ) {
        	if (null != errorCode) {
    			StringBuffer errors = new StringBuffer();
    			if ( isHeader ) errors.append("<result>");
    			if ( isTitle )
    			{
    				errors.append("<parenttitle>").append(parentTitle).append("</parenttitle>");
    				errors.append("<type>").append(errorType).append("</type>");
    			}
    			errors.append("<code>").append(errorCode).append("</code>");
    			errors.append("<message>").append(errorMessage).append("</message>");
    			errors.append("<title>").append(errorTitle).append("</title>");
    			errors.append("<response>").append("resultCode").append("</response>");
    			if ( isHeader ) errors.append("</result>");
        		String msg = errors.toString();
    			return msg;
        	}
        	return "<title>NO_ERROR</title><code>0</code><message>No Error</message><response>unknown</response>";
    	
    	} else {
        	if (null != errorCode) {
    			StringBuilder errors = new StringBuilder();
    			if ( isTitle )
    			{
    				errors.append("{\"key\" : \"").append(parentTitle).append("\",");
    				errors.append("\"type\" : \"").append(errorType).append("\",");
    			}
    			errors.append("\"values\":[");
    			errors.append('{');
    			errors.append("\"code\":\"").append(errorCode).append("\",");
    			errors.append("\"message\":\"").append(errorMessage).append("\",");
    			errors.append("\"title\":\"").append(errorTitle).append("\"");
    			errors.append('}');
    			errors.append("]");
    			if ( isTitle )
    			{
    				errors.append(",\"response\": \"resultCode\"");
    				errors.append('}');
    			}
        		String msg = errors.toString();
    			return msg;
        	} else {
        		return "{\"key\" : \"UNKNOWN_ERROR\", \"type\" : \"unknwon\", \"values\" : [ {\"code\": \"UNKNOWN_ERROR\",\"message\": \"Error is not known\"}], \"response\" : \"unknown\" }";
        	}
    	}
    }

    public static String getMsg(boolean isXml, boolean isHeader, String messageCode, String message, 
    							String title, String errorType, boolean isTitle, boolean isError, String parentTitle) 
    {
    	
    	if ( null == parentTitle) parentTitle = title;
    	
    	if ( isXml ) {
        	if (null != messageCode) {
    			StringBuffer msgB = new StringBuffer();
    			if ( isHeader ) msgB.append("<result>");
    			if ( isTitle )
    			{
    				msgB.append("<parenttitle>").append(parentTitle).append("</parenttitle>");
    				msgB.append("<type>").append(errorType).append("</type>");
    			}
    			msgB.append("<code>").append(messageCode).append("</code>");
    			msgB.append("<message>").append(message).append("</message>");
    			msgB.append("<title>").append(title).append("</title>");
    			msgB.append("<response>");
    			msgB.append(messageCode);
    			msgB.append("</response>");
    			if ( isHeader ) msgB.append("</result>");
        		String msg = msgB.toString();
    			return msg;
        	}
        	return "<title>NO_TITLE</title><type>unknown</type><code>0</code><message>No Message</message><response>unknown</response>";
    	
    	} else {
        	if (null != messageCode) {
    			StringBuilder msgB = new StringBuilder();

    			if ( isTitle )
    			{
    				msgB.append("{\"key\" : \"").append(parentTitle).append("\",");
    				msgB.append("\"type\" : \"").append(errorType).append("\",");
    			}
    			
    			msgB.append("\"values\":[");
    			msgB.append('{');
    			msgB.append("\"code\":\"").append(messageCode).append("\",");
    			msgB.append("\"message\":\"").append(message).append("\",");
    			msgB.append("\"title\":\"").append(title).append("\"");
    			msgB.append('}');
    			msgB.append("]");
    			
    			if ( isTitle )
    			{
    				msgB.append(",\"response\" : \"");
    				msgB.append("resultCode").append("\"");
    				msgB.append('}');
    			}
        		String msg = msgB.toString();
    			return msg;
        	} else {
        		return "{\"key\" : \"NO_TITLE\", \"type\" : \"unknown\", \"values\" : [ {\"code\": \"0\",\"message\": \"No Message\"}], \"response\" : \"unknown\" }";
        	}
    	}
    }
    
}
	