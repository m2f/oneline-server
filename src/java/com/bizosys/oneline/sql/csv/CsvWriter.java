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

package com.bizosys.oneline.sql.csv;

import com.bizosys.oneline.util.StringUtils;

public class CsvWriter {
	
	public static final int INITIAL_STRING_SIZE = 128;	
	private char separator = ',';
	private char escapechar;
	private String lineEnd = StringUtils.Empty;
    private char quotechar;
    /** The quote constant to use when you wish to suppress all quoting. */
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    
    /** The escape constant to use when you wish to suppress all escaping. */
    public static final char NO_ESCAPE_CHARACTER = '\u0000';

    public CsvWriter(char separator) {
        this.separator = separator;
        this.quotechar = NO_QUOTE_CHARACTER;
        this.escapechar = NO_ESCAPE_CHARACTER;
    }

    public CsvWriter(char separator, char quotechar, char escapechar) {
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
    }    
	
    /**
     * Write a row.
     * @param cells
     * @param sb
     * @return
     */
    public String writeRow(String[] cells, StringBuilder sb) {
        	
        	if (cells == null) return StringUtils.Empty;
        	
        	sb.delete(0, sb.capacity());
            for (int i = 0; i < cells.length; i++) {

                if (i != 0) {
                    sb.append(separator);
                }

                String nextElement = cells[i];
                if (nextElement == null)
                    continue;
                if (quotechar !=  NO_QUOTE_CHARACTER)
                	sb.append(quotechar);
                
                sb.append(stringContainsSpecialCharacters(nextElement) ? processRow(nextElement) : nextElement);

                if (quotechar != NO_QUOTE_CHARACTER)
                	sb.append(quotechar);
            }
            
            sb.append(lineEnd);
            return sb.toString();
        }    
    
	private boolean stringContainsSpecialCharacters(String line) {
	    return line.indexOf(quotechar) != -1 || line.indexOf(escapechar) != -1;
    }
	
	private StringBuilder processRow(String nextElement)
    {
		StringBuilder sb = new StringBuilder(INITIAL_STRING_SIZE);
	    for (int j = 0; j < nextElement.length(); j++) {
	        char nextChar = nextElement.charAt(j);
	        if (escapechar != NO_ESCAPE_CHARACTER && nextChar == quotechar) {
	        	sb.append(escapechar).append(nextChar);
	        } else if (escapechar != NO_ESCAPE_CHARACTER && nextChar == escapechar) {
	        	sb.append(escapechar).append(nextChar);
	        } else {
	            sb.append(nextChar);
	        }
	    }
	    return sb;
    }	
}
