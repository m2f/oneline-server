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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class RegularRowReader implements RowReader {

	BufferedReader br = null;
	char separator = ',';
	int expectedRow = -1;
	
	public RegularRowReader(Reader reader, String separator) {
		this.br = new BufferedReader(reader);
		this.separator = separator.charAt(0);
	}
	
	public String[] readNext() throws IOException {
		String nextLine = br.readLine();
	    if (nextLine == null) return null;
	    if ( 0 == nextLine.length()) return null;
	    
	    String[] cells = fastSplit(nextLine);
	    return cells;
	}

	public void close() throws IOException {
		if ( null != this.br)
			this.br.close();
	}
	

	public String[] fastSplit(String text) {
		if ( -1 == expectedRow) {
			expectedRow = 0;
			  int index1 = 0;
			  int index2 = text.indexOf(separator);
			  while (index2 >= 0) {
				  index1 = index2 + 1;
				  index2 = text.indexOf(separator, index1);
				  expectedRow++;
			  }
		            
			  if (index1 < text.length() - 1) {
				  expectedRow++;
			  }
		}
		
		String[] cells = new String[expectedRow];
		
		int cellPos = 0;
		int index1 = 0;
		int index2 = text.indexOf(separator);
		String token = null;
		while (index2 >= 0) {
			token = text.substring(index1, index2);
			cells[cellPos] = token;
			index1 = index2 + 1;
			index2 = text.indexOf(separator, index1);
			cellPos++;
		}
	            
		if (index1 < text.length() - 1) {
			cells[cellPos] = text.substring(index1);
		}
		return cells;			
	}
}
