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

package com.bizosys.oneline.model;

public class StoredProcOutParam {

	public String outParamName = null;
	public int outPramIndex = 0;
	public int outPramType = 0;
	
	public StoredProcOutParam( final String outParamName, final int outPramIndex,final int outPramType) {
		this.outParamName = outParamName;
		this.outPramIndex = outPramIndex;
		this.outPramType = outPramType;
	}
	
	public Object outParamValue = null;
	public void setOutPramValue( Object outParamValue ) {
		this.outParamValue = outParamValue;
	}

	public Object getOutParamValue(){
		return this.outParamValue;
	}
	
	public boolean isError = false;
	public void setError( boolean isError ) {
		this.isError = isError;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outParamName == null) ? 0 : outParamName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StoredProcOutParam other = (StoredProcOutParam) obj;
		if (outParamName == null) {
			if (other.outParamName != null) return false;
		} else if (!outParamName.equals(other.outParamName))
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return "Name:" + outParamName + ",Type:" + outPramType + ",Index:"+ outPramIndex;
	}
}
