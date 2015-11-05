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

package com.bizosys.oneline.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WriteToXls 
{
	private static final String EMPTY_STRING = "";
	private OutputStream out = null;
	
	private final static int XLSX_FORMAT = 0;
	private final static int XLS_FORMAT = 1;

	private int startRowIndex = 0;
	private int xlsFormat = 0;
	
	private String templateFile = null;
	
	public WriteToXls(OutputStream out,int startRowIndex, int xlsFormat) {
		this.out = out;
		this.startRowIndex = startRowIndex;
		this.xlsFormat = xlsFormat;
	}
	
	public void setTemplateFile(String templateFileName) {
		templateFile = templateFileName;
	}
	
	public void write(List<Object[]> records) throws Exception
	{
		Workbook workbook = getWorkbook();
		Sheet sheet = workbook.createSheet();
		
		if ( null != templateFile) 
		{
			File templateFileObject = new File(templateFile);
			if ( templateFileObject.exists()) {
				Workbook templateWorkbook = new HSSFWorkbook(new FileInputStream(templateFileObject));
				Sheet templatesheet = templateWorkbook.getSheetAt(0);
				Iterator<Row> rowIterator = templatesheet.iterator();
				
				while (rowIterator.hasNext()) {
					Row templateRow = rowIterator.next(); 
					Row row = sheet.createRow(startRowIndex++);
					
					Iterator<Cell> cellIterator =  templateRow.cellIterator();
					while (cellIterator.hasNext()) {
						Cell templateCell = cellIterator.next();
						Cell cell = row.createCell(templateCell.getColumnIndex());
						cell.setCellType(templateCell.getCellType());
						switch ( templateCell.getCellType()) {
							case Cell.CELL_TYPE_BLANK:
								break;
							case Cell.CELL_TYPE_BOOLEAN:
								cell.setCellValue(templateCell.getBooleanCellValue());
								break;
							case Cell.CELL_TYPE_ERROR:
								cell.setCellValue(templateCell.getErrorCellValue());
								break;
							case Cell.CELL_TYPE_FORMULA:
								cell.setCellValue(templateCell.getCellFormula());
								break;
							case Cell.CELL_TYPE_NUMERIC:
								cell.setCellValue(templateCell.getNumericCellValue());
								break;
							case Cell.CELL_TYPE_STRING:
								cell.setCellValue(templateCell.getStringCellValue());
								break;
						}
					}
				}					
			} else {
				System.err.println("Can not read " + templateFileObject.getAbsolutePath());
			}
		}
		
		for (Object[] cols : records) {
			createRecord(cols, sheet);
		}
		workbook.write(out);
		
	}
	
	
	private void createRecord(Object[] cols, Sheet sheet) throws SQLException {
		
		String colStr = null;
		
		Row row = sheet.createRow(startRowIndex++);
		int colI = 0;
		for ( Object colObj : cols) 
		{
			colStr = ( null == colObj) ? EMPTY_STRING: colObj.toString().trim();
			Cell cell = row.createCell(colI);
			cell.setCellValue(colStr);
			colI++;
		}
	}

	private Workbook getWorkbook()
	{
		switch (xlsFormat) {

		case XLSX_FORMAT: 
			return new XSSFWorkbook();

		case XLS_FORMAT: 
				return new HSSFWorkbook();
		
		default :
			return new XSSFWorkbook();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		String json = " { \"values\" : [ { \"name\" : \"ravi\" , \"id\" : \"334\" }, { \"name\" : \"kumar\" , \"id\" : \"335\" } ] }";
		JsonParser parser = new JsonParser();
		JsonObject o = (JsonObject) parser.parse(json);
		
		JsonArray values = o.getAsJsonArray("values");

		Set<Map.Entry<String, JsonElement>> entrySet = null;
		
		List<Object[]> records = new ArrayList<Object[]>();
		List<Object> cols = new ArrayList<Object>();
		
		List<String> labels = new ArrayList<String>();
		boolean isFirst = true;
		for(JsonElement elem : values)
		{
			JsonObject obj = elem.getAsJsonObject();
			entrySet = obj.entrySet();
			cols.clear();
			if( isFirst )
			{
				for(Map.Entry<String,JsonElement> entry:entrySet){
					labels.add(entry.getKey());
				}
				isFirst = false;
			}
			
			for( String aLabel : labels)
			{
				cols.add(obj.get(aLabel).getAsString());
			}
			records.add(cols.toArray());
		}
		
		OutputStream out = null;
		out = new FileOutputStream(new File("/tmp/test.xlsx"));
		WriteToXls writerXls = new WriteToXls(out, 0, 0);
		writerXls.write(records);
	}
}
