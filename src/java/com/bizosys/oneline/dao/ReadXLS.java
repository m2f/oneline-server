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
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ReadXLS extends ReadBase<String> {

	private final static Logger LOG = Logger.getLogger(ReadXLS.class);
	private static final String EMPTY_STRING = "";
	private OutputStream out = null;
	
	private final static int XLSX_FORMAT = 0;
	private final static int XLS_FORMAT = 1;

	private int startRowIndex = 0;
	private int xlsFormat = 0;
	
	private String templateFile = null;
	
	
	public ReadXLS(OutputStream out, int startRowIndex, int xlsFormat) {
		this.out = out;
		this.startRowIndex = startRowIndex;
		this.xlsFormat = xlsFormat;
	}
	
	public void setTemplateFile(String templateFileName) {
		templateFile = templateFileName;
	}
	
	@Override
	protected List<String> populate() throws SQLException {
		
		checkCondition();

		Workbook workbook = getWorkbook();
		Sheet sheet = workbook.createSheet();
		
		ResultSetMetaData md = rs.getMetaData() ;
		int totalCol = md.getColumnCount();
		String[] cols = createLabels(md, totalCol);
		
		try {
			
			if ( null != templateFile) {
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
			
			while (this.rs.next()) {
				createRecord(totalCol, cols, sheet);
			}
			workbook.write(out);
		} catch (IOException ex) {
			throw new SQLException(ex);
		}
		return null;
	}

	@Override
	protected String getFirstRow() throws SQLException {
		return null;
	}

	private void checkCondition() throws SQLException {
		if ( null == this.rs) {
			LOG.warn("Rs is not initialized.");
			throw new SQLException("Rs is not initialized.");
		}
	}

	private String[] createLabels(ResultSetMetaData md, int totalCol) throws SQLException {
		String[] cols = new String[totalCol];
		for ( int i=0; i<totalCol; i++ ) {
			cols[i] = md.getColumnLabel(i+1);
		}
		return cols;
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
	
	
	private void createRecord(int colsT, String[] cols, Sheet sheet) throws SQLException {
		
		Object colObj = null; 
		String colStr = null;
		
		Row row = sheet.createRow(startRowIndex++);
		for ( int colI=0; colI<colsT; colI++ ) 
		{
			colObj = rs.getObject(colI+1);
			colStr = ( null == colObj) ? EMPTY_STRING: colObj.toString().trim();
			Cell cell = row.createCell(colI);
			cell.setCellValue(colStr);
		}
	}
}
