

// ######################################################################################################################################
// ############################################		Add and Update App Config	  ###############################################
// ######################################################################################################################################

var appConfigRecords = [];
var functionRecords = [];
var spAppConfigRecords = [];

function validateAppConfigForm() {

	var configTypeElem = document.getElementById("editableConfigTypeEl");
	var configNameElem = document.getElementById("titleEl");
	var configQueryElem = document.getElementById("bodyEl");
	var configVariabesElem = document.getElementById("variablesEl");
	var configOutvarElem = document.getElementById("outvarEl");
	var configStatusElem = document.getElementById("editableStatusEl");

	var isSuccess = true;
	var errorMsg = "";

	if ( isEmpty(configTypeElem.value, configTypeElem) ) {
		errorMsg = errorMsg + "Configuration Type cannot be empty. ";
		isSuccess = false;
	}
	if ( isEmpty(configNameElem.value, configNameElem) ) {
		errorMsg = "Title cannot be empty. ";
		isSuccess = false;
	}
	if ( isEmpty(configQueryElem.value, configQueryElem) ) {
		errorMsg = errorMsg + "Query cannot be empty. ";
		isSuccess = false;
	}
	if ( isEmpty(configStatusElem.value, configStatusElem) ) {
		errorMsg = errorMsg + "Status cannot be empty.";
		isSuccess = false;
	}

	if (!isSuccess) {
		$('#errorMessages').html(errorMsg);
	}
	return isSuccess;
}

function validateSpAppConfigForm() {

	var spPoolElem = document.getElementById("sp_poolnameEl");
	var spTitleElem = document.getElementById("sp_titleEl");
	var spBodyElem = document.getElementById("sp_bodyEl");
	var spCallElem = document.getElementById("sp_call_syntaxEl");
	var spOutvarElem = document.getElementById("sp_out_varEl");
	var spStatusElem = document.getElementById("editableStatusEl");

	var isSuccess = true;
	var errorMsg = "";

	if ( isEmpty(spPoolElem.value, spPoolElem) ) {
		errorMsg = errorMsg + "SP Pool cannot be empty. ";
	}
	if ( isEmpty(spTitleElem.value, spTitleElem) ) {
		errorMsg = "SP Title cannot be empty. ";
		isSuccess = false;
	}
	if ( isEmpty(spBodyElem.value, spBodyElem) ) {
		errorMsg = errorMsg + "SP Body cannot be empty. ";
		isSuccess = false;
	}
	if ( isEmpty(spCallElem.value, spCallElem) ) {
		errorMsg = errorMsg + "SP Call cannot be empty.";
		isSuccess = false;
	}
	
	if ( isEmpty(spStatusElem.value, spStatusElem) ) {
		errorMsg = errorMsg + "SP Status cannot be empty.";
		isSuccess = false;
	}
	if (!isSuccess) {
		$('#errorMessages').html(errorMsg);
	}
	return isSuccess;
}

function validateFunctionForm() {

	var statusElem = document.getElementById("statusEl");
	var funcIdElem = document.getElementById("funcIdEl");
	var funcBodyElem = document.getElementById("funcBodyEl");

	var isSuccess = true;
	var errorMsg = "";

	if ( isEmpty(funcBodyElem.value, funcBodyElem) ) {
		errorMsg = "Function Body cannot be empty. ";
		isSuccess = false;
	}	
	if ( isEmpty(funcIdElem.value, funcIdElem) ) {
		errorMsg = "Function Id cannot be empty. " + errorMsg;
		isSuccess = false;
	}
	if ( isEmpty(statusElem.value, statusElem) ) {
		errorMsg = "Status cannot be empty. " + errorMsg;
		isSuccess = false;
	}
	if (!isSuccess) {
		$('#errorMessages').html(errorMsg);
	}
	return isSuccess;
}

//	Add a new App Config record
function validateQuery() {
	var configQuery = document.getElementById("bodyEl").value;
	var configName = document.getElementById("titleEl").value;
	var postData = {
			service : "sql", action : "validatesql", query : configQuery, title : configName, pool: "configpool", format : "jsonp"
		};

		try {
			serverCallStart();
			$.ajax ({
				type : "GET",
				async : false,
				url : "dataservice.html",
				data : postData,
				cache : false,
				dataType : "text",
				success : function onSuccess(data) {
					try {
						document.getElementById("errorMessages").innerHTML = data;
					} finally {
						serverCallEnd();
					}
				},
				error : function onError(data) {
					document.getElementById("errorMessages").innerHTML = data.responseText;
					serverCallEnd();
			}
			});
		} catch (error) {
			serverCallEnd();
		};
	
}
	
function addAppConfig() {

	var configType = document.getElementById("editableConfigTypeEl").value;
	var configName = document.getElementById("titleEl").value;
	var configQuery = document.getElementById("bodyEl").value;
	var configVariabes = document.getElementById("variablesEl").value;
	var configOutvar = document.getElementById("outvarEl").value;
	var configStatus = document.getElementById("editableStatusEl").value;

	if ( checkExistenceOfObject(configName)[0] ) {
		$('#errorMessages').html("Please enter a different title, as the given title already exists.");
		return;
	}

	var queryJson = '{ queries : [ {queryid : "ADD_AN_APP_CONFIG", params : ["' + 
		configType + '", "' + 
		configName + '", "' + 
		configQuery + '", "' + 
		configVariabes + '", "' + 
		configOutvar + '", "' + 
		configStatus + '"] }, ';
	
	queryJson = queryJson + '{queryid : "ADD_A_NEW_OBJECT", params : ["' + configName + '"] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Data API successfully set up.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

	lightboxClose();
}

function addSPAppConfig() {
	
	
	var spPool = document.getElementById("sp_poolnameEl").value;
	var spTitle = document.getElementById("sp_titleEl").value;
	var spBody = document.getElementById("sp_bodyEl").value;
	var spCall = document.getElementById("sp_call_syntaxEl").value;
	var spOutvar = document.getElementById("sp_out_varEl").value;
	var spErrorvar = document.getElementById("sp_err_varEl").value;
	var spStatus = document.getElementById("editableStatusEl").value;
	
	if ( !createSP(spTitle, spBody, spPool) ) return;
	
	if ( checkExistenceOfObject(spTitle)[0] ) {
		$('#errorMessages').html("Please enter a different title, as the given title already exists.");
		return;
	}

	spBody = btoa(spBody);
	var queryJson = '{ queries : [ {queryid : "ADD_SP_APP_CONFIG", params : ["' + 
		spTitle + '", "' + 
		spBody + '", "' + 
		spPool + '", "' + 
		spCall + '", "' + 
		spOutvar + '", "' + 
		spErrorvar + '", "' + 
		spStatus + '"] }, ';
	
	queryJson = queryJson + '{queryid : "ADD_A_NEW_OBJECT", params : ["' + spTitle + '"] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "POST",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Data API successfully set up.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

	lightboxClose();
}

function createSP(spTitle, spBody, spPool)
{
	var isSuccess = true;
	
	var postData = {
		service : "sql",
		action : "createsp",
		pool : spPool,
		format : "jsonp",
		spbody : spBody,
		sptitle : spTitle
	}
	try {
		serverCallStart();
		$.ajax ({
			type : "POST",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData  = $.parseJSON(data);
					jsonData = jsonData[0];
					if ( jsonData['response'] == 'resultCode' )
					{
						if ( jsonData['values'][0]['code'] != "0037" )
						{
							isSuccess = false;
							document.getElementById("errorMessages").innerHTML = jsonData['values'][0]['message'];
						}
					}
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				isSuccess = false;
				serverCallEnd();
				document.getElementById("errorMessages").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		isSuccess = false;
		serverCallEnd();
	};
	return isSuccess;
}
function validateJson(jsonData,isValid) {
	
	try{
		var jsonData = $.parseJSON(jsonData);
		if (jsonData != null || jsonData != "" || jsonData != undefined) {
			isValid = true;
			return isValid;
		}else return isValid;
	}catch(error){
		var errorMsg = error;
		document.getElementById("errorMessages").innerHTML = errorMsg;
	}
}

function addFunction() {
	var status = document.getElementById("statusEl").value;
	var id = document.getElementById("funcIdEl").value;
	var body = document.getElementById("funcBodyEl").value;

	var queryJson = "{ queries : [ {queryid : ADD_FUNCTION, params : ['" + 
	status + "', '" + 
	id + "', '" + 
	body + "'] } ] }";
	
	var isValid = false;
	
	
	isValid = validateJson(body,isValid);
	if(isValid){
		
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};
	
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Function successfully added.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};
	
	lightboxClose();
   }
}


//	Update an existing App Config record
function updateSelectedAppConfigRecord() {

	var configId = document.getElementById("idEl").value;
	var configType = document.getElementById("editableConfigTypeEl").value;
	var configName = document.getElementById("titleEl").value;
	var configQuery = document.getElementById("bodyEl").value;
	var configVariabes = document.getElementById("variablesEl").value;
	var configOutvar = document.getElementById("outvarEl").value;
	var configStatus = document.getElementById("editableStatusEl").value;

	var oldConfigName = document.getElementById("titleOldEl").value;

	if (configName != oldConfigName) {
		if ( checkExistenceOfObject(configName)[0] ) {
			$('#errorMessages').html("Please enter a different title, as the given title already exists.");
			return;
		}
	}

	var queryJson = '{ queries : [ {queryid : "UPDATE_APP_CONFIG", params : ["' + 
	configType + '", "' + 
	configName + '", "' + 
	configQuery + '", "' + 
	configVariabes + '", "' + 
	configOutvar + '", "' + 
	configStatus + '", ' + 
	oldConfigName + '] }';

	if (configName != oldConfigName) {
		queryJson = queryJson + ', {queryid : "UPDATE_OBJECT_BY_NAME", params : ["' + configName + '", "' + oldConfigName + '"] }';
	}
	queryJson = queryJson + ' ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Data API successfully updated.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

	lightboxClose();
}

//	Update an existing App Config record
function updateSelectedSPAppConfigRecord() {

	var spPool = document.getElementById("sp_poolnameEl").value;
	var spTitle = document.getElementById("sp_titleEl").value;
	var spBody = document.getElementById("sp_bodyEl").value;
	var spCall = document.getElementById("sp_call_syntaxEl").value;
	var spOutvar = document.getElementById("sp_out_varEl").value;
	var spErrorvar = document.getElementById("sp_err_varEl").value;
	var spStatus = document.getElementById("editableStatusEl").value;
	
	var configId = document.getElementById("idEl").value;
	
	var oldSpTitle = document.getElementById("sp_titleOldEl").value;
	var oldSpBody = document.getElementById("sp_bodyOldEl").value;

	if (spTitle != oldSpTitle) {
		if ( checkExistenceOfObject(spTitle)[0] ) {
			$('#errorMessages').html("Please enter a different title, as the given title already exists.");
			return;
		}
	}
	
	var isCreateSuccess = true;
	if ( spBody.trim() != oldSpBody.trim() )
	{
			if ( !deleteSPFromDb(spTitle, spPool, false)) return;
			isCreateSuccess = createSP(spTitle, spBody, spPool);
	}
	
	spBody = btoa(spBody);
	var queryJson = '{ queries : [ {queryid : "UPDATE_SP_APP_CONFIG", params : ["' + 
	spTitle + '", "' + 
	spBody + '", "' + 
	spCall + '", "' + 
	spOutvar + '", "' + 
	spErrorvar + '", "' + 
	spStatus + '", ' + 
	oldSpTitle + '] }';

	if (spTitle != oldSpTitle) {
		queryJson = queryJson + ', {queryid : "UPDATE_OBJECT_BY_NAME", params : ["' + spTitle + '", "' + oldSpTitle + '"] }';
	}
	queryJson = queryJson + ' ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};
	
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					if ( isCreateSuccess ) showMessage("Data API successfully updated.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

	if ( isCreateSuccess ) lightboxClose();
}


function updateSelectedFunctionRecord() {

	var status = document.getElementById("statusEl").value;
	var funcId = document.getElementById("funcIdEl").value;
	var funcBody = document.getElementById("funcBodyEl").value;

	var oldfuncId = document.getElementById("funcIdOldEl").value;
			
	var queryJson = "{ queries : [ {queryid : 'UPDATE_FUNCTION', params : ['" + 
	status + "', '" + 
	funcId + "', '" + 
	funcBody + "', " + 
	oldfuncId + "] } ] }";
	
    var isValid = false;
   	
	isValid = validateJson(funcBody,isValid);
	if(isValid){

	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Function successfully updated.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

	lightboxClose();
	}
}

function prepareAppConfigRecords(data) {

	var appConfigJson = $.parseJSON(data);
	
	var preparedJson = appConfigJson[0];
	var records = [];
	var configRecords = [];

	var configRecords = preparedJson["values"];
	
	for( var index in configRecords )
	{
		var aRecord = configRecords[index];
		appConfigRecords[aRecord['title']] = aRecord;
	}
}

function prepareSpAppConfigRecords(data) {

	var appConfigJson = $.parseJSON(data);
	
	var preparedJson = appConfigJson[0];
	var records = [];
	var configRecords = [];

	var configRecords = preparedJson["values"];
	
	for( var index in configRecords )
	{
		var aRecord = configRecords[index];
		spAppConfigRecords[aRecord['sp_title']] = aRecord;
	}
}

function prepareFunctionRecords(data) {
	var functionJson = $.parseJSON(data);
	var preparedJson = functionJson[0];
	var records ;
    var configRecords = [];
	
	var configRecords = preparedJson["values"];
	
	for( var index in configRecords )
	{
		var aRecord = configRecords[index];
		functionRecords[aRecord['funcId']] = aRecord;
	}
}


function prepForAdd() {

	prepForEdit();
	document.getElementById("form-submit-add").style.display = "inline-block";
	document.getElementById("form-submit-update").style.display = "none";
	document.getElementById("titleEl").value = null;
	document.getElementById("bodyEl").value = null;
	document.getElementById("variablesEl").value = null;
	document.getElementById("outvarEl").value = null;
	document.getElementById("statusDiv").style.display = "none";
	document.getElementById("configTypeDiv").style.display = "none";
	document.getElementById("configTypeDropdown").style.display = "block";
	document.getElementById("statusDropdown").style.display = "block";
	document.getElementById("lightboxHeader").innerHTML = "Add New Data API";
	lightboxOpen();
}

function prepForAddSP() {

	prepForEditSP();
	document.getElementById("form-submit-add").style.display = "inline-block";
	document.getElementById("form-submit-update").style.display = "none";
	document.getElementById("sp_poolnameEl").value = null;
	document.getElementById("sp_titleEl").value = null;
	document.getElementById("sp_call_syntaxEl").value = null;
	document.getElementById("sp_bodyEl").value = null;
	document.getElementById("sp_out_varEl").value = null;
	document.getElementById("sp_err_varEl").value = null;
	document.getElementById("statusDiv").style.display = "none";
	document.getElementById("statusDropdown").style.display = "block";
	document.getElementById("lightboxHeader").innerHTML = "Add New Data API";
	lightboxOpen();
}


function prepForAddF() {

	prepForEditF();
	document.getElementById("form-submit-add").style.display = "inline-block";
	document.getElementById("form-submit-update").style.display = "none";
	document.getElementById("statusEl").value = null;
	document.getElementById("funcIdEl").value = null;
	document.getElementById("funcBodyEl").value = null;
	document.getElementById("lightboxHeader").innerHTML = "Add New Function";
	lightboxOpen();
}

function prepForEditSP() {

	$('#errorMessages').html(null);
	document.getElementById("lightboxHeader").innerHTML = "Edit SP Details";

	document.getElementById("sp_poolnameEl").removeAttribute("readonly");
	document.getElementById("sp_titleEl").removeAttribute("readonly");
	document.getElementById("sp_call_syntaxEl").removeAttribute("readonly");
	document.getElementById("sp_bodyEl").removeAttribute("readonly");
	document.getElementById("sp_out_varEl").removeAttribute("readonly");
	document.getElementById("sp_err_varEl").removeAttribute("readonly");
	document.getElementById("statusDiv").style.display = "none";
	document.getElementById("statusDropdown").style.display = "block";
	document.getElementById("nonEditableFld1").style.display = "none";
	document.getElementById("nonEditableFld2").style.display = "none";
	document.getElementById("form-submit-add").style.display = "none";
	document.getElementById("form-submit-edit").style.display = "none";
	document.getElementById("form-submit-update").style.display = "inline-block";

	document.getElementById("spanEl1").style.display = "inline-block";
	document.getElementById("spanEl2").style.display = "inline-block";
	document.getElementById("spanEl3").style.display = "inline-block";
	document.getElementById("spanEl4").style.display = "inline-block";
	document.getElementById("spanEl5").style.display = "inline-block";
	document.getElementById("spanEl6").style.display = "inline-block";
	document.getElementById("spanEl7").style.display = "inline-block";
	document.getElementById("legendEl").style.display = "inline-block";
}

function prepForEdit() {

	$('#errorMessages').html(null);
	document.getElementById("lightboxHeader").innerHTML = "Edit App Config Details";

	document.getElementById("titleEl").removeAttribute("readonly");
	document.getElementById("bodyEl").removeAttribute("readonly");
	document.getElementById("variablesEl").removeAttribute("readonly");
	document.getElementById("outvarEl").removeAttribute("readonly");
	document.getElementById("statusDiv").style.display = "none";
	document.getElementById("configTypeDiv").style.display = "none";
	document.getElementById("configTypeDropdown").style.display = "block";
	document.getElementById("statusDropdown").style.display = "block";
	document.getElementById("nonEditableFld1").style.display = "none";
	document.getElementById("nonEditableFld2").style.display = "none";
	document.getElementById("form-submit-add").style.display = "none";
	document.getElementById("form-submit-edit").style.display = "none";
	document.getElementById("form-submit-update").style.display = "inline-block";

	document.getElementById("spanEl1").style.display = "inline-block";
	document.getElementById("spanEl2").style.display = "inline-block";
	document.getElementById("spanEl3").style.display = "inline-block";
	document.getElementById("spanEl4").style.display = "inline-block";
	document.getElementById("spanEl5").style.display = "inline-block";
	document.getElementById("spanEl6").style.display = "inline-block";
	document.getElementById("legendEl").style.display = "inline-block";
}

function prepForEditF() {

	$('#errorMessages').html(null);
	document.getElementById("lightboxHeader").innerHTML = "Edit Function Details";

	document.getElementById("statusEl");
	document.getElementById("funcIdEl");
	document.getElementById("funcBodyEl");
	document.getElementById("form-submit-add").style.display = "none";
	document.getElementById("form-submit-edit").style.display = "none";
	document.getElementById("form-submit-update").style.display = "inline-block";

	document.getElementById("spanEl1").style.display = "inline-block";
	document.getElementById("spanEl2").style.display = "inline-block";
	document.getElementById("spanEl3").style.display = "inline-block";
	document.getElementById("legendEl").style.display = "inline-block";
}


function prepForView() {

	$('#errorMessages').html(null);
	document.getElementById("lightboxHeader").innerHTML = "App Config Details";

	document.getElementById("titleEl").setAttribute("readonly", "readonly");
	document.getElementById("bodyEl").setAttribute("readonly", "readonly");
	document.getElementById("variablesEl").setAttribute("readonly", "readonly");
	document.getElementById("outvarEl").setAttribute("readonly", "readonly");
	document.getElementById("statusDiv").style.display = "block";
	document.getElementById("configTypeDiv").style.display = "block";
	document.getElementById("configTypeDropdown").style.display = "none";
	document.getElementById("statusDropdown").style.display = "none";
	document.getElementById("nonEditableFld1").style.display = "block";
	document.getElementById("nonEditableFld2").style.display = "block";
	document.getElementById("form-submit-add").style.display = "none";
	document.getElementById("form-submit-edit").style.display = "inline-block";
	document.getElementById("form-submit-update").style.display = "none";

	document.getElementById("spanEl1").style.display = "none";
	document.getElementById("spanEl2").style.display = "none";
	document.getElementById("spanEl3").style.display = "none";
	document.getElementById("spanEl4").style.display = "none";
	document.getElementById("spanEl5").style.display = "none";
	document.getElementById("spanEl6").style.display = "none";
	document.getElementById("legendEl").style.display = "none";
}

function prepForViewSP() {

	$('#errorMessages').html(null);
	document.getElementById("lightboxHeader").innerHTML = "SP Details";

	document.getElementById("sp_poolnameEl").setAttribute("readonly", "readonly");
	document.getElementById("sp_titleEl").setAttribute("readonly", "readonly");
	document.getElementById("sp_call_syntaxEl").setAttribute("readonly", "readonly");
	document.getElementById("sp_bodyEl").setAttribute("readonly", "readonly");
	document.getElementById("sp_out_varEl").style.display = "block";
	document.getElementById("sp_err_varEl").style.display = "block";
	document.getElementById("statusDiv").style.display = "none";
	document.getElementById("statusDropdown").style.display = "none";
	document.getElementById("nonEditableFld1").style.display = "block";
	document.getElementById("nonEditableFld2").style.display = "block";
	document.getElementById("form-submit-add").style.display = "none";
	document.getElementById("form-submit-edit").style.display = "inline-block";
	document.getElementById("form-submit-update").style.display = "none";

	document.getElementById("spanEl1").style.display = "none";
	document.getElementById("spanEl2").style.display = "none";
	document.getElementById("spanEl3").style.display = "none";
	document.getElementById("spanEl4").style.display = "none";
	document.getElementById("spanEl5").style.display = "none";
	document.getElementById("spanEl6").style.display = "none";
	document.getElementById("spanEl7").style.display = "none";
	document.getElementById("legendEl").style.display = "none";
}

function viewRecordDetails(configId) {

	selectedRecord = appConfigRecords[configId];
	if( !selectedRecord || null == selectedRecord) return;
	lightboxOpen();
	for (var label in selectedRecord) {
		try {
			if (label == "title") {
				document.getElementById(label + "El").value = selectedRecord[label];
				document.getElementById(label + "OldEl").value = selectedRecord[label];
			}
			else if (label == "status") {
				var val = selectedRecord[label];
				var displayVal = val;
				if (val == "Y" || val == "y") displayVal = "Active";
				else if (val == "N" || val == "n") displayVal = "Inactive";
				document.getElementById(label + "El" ).value = displayVal;
			}
			else if (label == "createTime") {
				document.getElementById(label + "El" ).value = trimTimeFrom(selectedRecord[label]);
			}
			else if (label == "touchTime") {
				document.getElementById(label + "El" ).value = removeTrailingZeroFrom(selectedRecord[label]);
			}
			else if (label == "configtype") {
				$("#editableConfigTypeEl").val(selectedRecord[label]);
			}
			else {
				document.getElementById(label + "El" ).value = selectedRecord[label];
			}
		} catch(error) { } ;
	}
}

function viewSPDetails(configId) {

	selectedRecord = spAppConfigRecords[configId];
	if( !selectedRecord || null == selectedRecord) return;
	lightboxOpen();
	for (var label in selectedRecord) {
		try {
			if (label == "sp_title") {
				document.getElementById(label + "El").value = selectedRecord[label];
				document.getElementById(label + "OldEl").value = selectedRecord[label];
			}
			else if (label == "sp_body") {
				document.getElementById(label + "El").value = atob(selectedRecord[label]);
				document.getElementById(label + "OldEl").value = atob(selectedRecord[label]);
			}
			else if (label == "status") {
				var val = selectedRecord[label];
				var displayVal = val;
				if (val == "Y" || val == "y") displayVal = "Active";
				else if (val == "N" || val == "n") displayVal = "Inactive";
				document.getElementById(label + "El" ).value = displayVal;
			}
			else if (label == "createTime") {
				document.getElementById(label + "El" ).value = trimTimeFrom(selectedRecord[label]);
			}
			else if (label == "touchTime") {
				document.getElementById(label + "El" ).value = removeTrailingZeroFrom(selectedRecord[label]);
			}
			else if (label == "configtype") {
				$("#editableConfigTypeEl").val(selectedRecord[label]);
			}
			else {
				document.getElementById(label + "El" ).value = selectedRecord[label];
			}
		} catch(error) { } ;
	}
}


function viewRecordDetailsF(funcId) {
	selectedRecord = functionRecords[funcId];
	if( !selectedRecord || null == selectedRecord) return;
	lightboxOpen();
	
	for (var label in selectedRecord) {
		try {
			if (label == "funcId") {
				document.getElementById(label + "El").value = selectedRecord[label];
				document.getElementById(label + "OldEl").value = selectedRecord[label];
			}
			else if (label == "funcBody") {
				document.getElementById(label + "El" ).value = selectedRecord[label];
			}
			else {
				document.getElementById(label + "El" ).value = selectedRecord[label];
			}
		} catch(error) { } ;
	}
}

// ######################################################################################################################################
// ####################################################		Permissions	  #######################################################
// ######################################################################################################################################


var allRoles = [];
var allRoleIds = [];
var oldRoleId;
var selectedAppConfigTitle = "";
var selectedObjectId;
var selectedObjectType;

function getAuthorizedUsers(objectName) {

	selectedAppConfigTitle = objectName;
	
	var queryJson = '{ queries : [ {queryid : "GET_AUTHORIZED_USERS_FOR_AN_ACTION", params : ["' + objectName + '"] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

		try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData = $.parseJSON(data);
					var json = jsonData[0];
					jsonData = json["values"];
					var count = 1;
					if (jsonData == null || jsonData == "" || jsonData == undefined) {
						var check = checkExistenceOfObject(objectName);
						if (check[0]) {
							prepForFirstTimeAdd(check[1]);
							lightboxOpenAlternate();
						} else {
							showPermanentMessage("No corresponding record found in the `objects` table for '" + objectName + "'");
						}
					} else {
						for (var i in jsonData) {
							if (count < 6) {
								selectedObjectId = jsonData[i]['objectid'];
								selectedObjectType = jsonData[i]['objecttype'];
								document.getElementById("userDiv" + count).style.display = "block";
								document.getElementById("user" + count + "El").value = jsonData[i]['rolename'];
							}
							count++;
						}
						lightboxOpenAlternate();
					}
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
	};

}

function checkExistenceOfObject(objectName) {

	var queryJson = '{ queries : [ {queryid : "CHECK_OBJECT_EXISTENCE", params : ["' + objectName + '"] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	var objectId = -1;
	var exists = true;
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData = $.parseJSON(data);
					var json = jsonData[0];
					jsonData = json["values"];
					if (jsonData == null || jsonData == "" || jsonData == undefined) exists = false;
					else {
						var count = parseInt(jsonData[0]['count']);
						// Check object's existence in the database
						if ( count == 0 || count == undefined || count == null) {
							exists = false;
							objectId = -1;
						} else objectId = parseInt(jsonData[0]['OBJECT_ID']);
					}
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				exists = false;
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch (error) {
		serverCallEnd();
		exists = false;
	};

	return [exists, objectId];
}

function prepForNewUserPermission() {

	resetPermissionDivs();
	document.getElementById("form-submit-permission").style.display = "none";
	document.getElementById("form-submit-permission-grant").style.display = "inline-block";
	document.getElementById("roles").style.display = "block";
}

function prepForFirstTimeAdd(objectIdParam) {

	selectedObjectId = objectIdParam;
	$('#roles').show();
	$('#form-submit-permission').hide();
	$('#form-submit-permission-grant').show();
}

function resetUserDivs() {

	for (var index = 1 ; index < 6 ; index++) {
		document.getElementById("userDiv" + index).style.display = "none";
		document.getElementById("user" + index + "El").value = null;
	}
}

function resetButtonDivs() {

	document.getElementById("form-submit-permission").style.display = "inline-block";
	document.getElementById("form-submit-permission-grant").style.display = "none";
	document.getElementById("form-submit-permission-update").style.display = "none";
}

function resetPermissionDivs() {

	document.getElementById("form-submit-permission").style.display = "inline-block";
	document.getElementById("roles").style.display = "none";
}

function getUserRoles() {

	var queryJson = '{ queries : [ {queryid : "GET_ROLES", params : [] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData = $.parseJSON(data);
					var json = jsonData[0];
					jsonData = json["values"];
					if (jsonData == null || jsonData == "" || jsonData == undefined) return;
					var roleListDiv = document.getElementById("roleEl");
					for (var i in jsonData) {
						roleListDiv.options[roleListDiv.options.length] = new Option(jsonData[i]["roleName"], jsonData[i]["roleId"]);
						allRoles[i] = jsonData[i]["roleName"];
						allRoleIds[i] = jsonData[i]["roleId"];
					}
				} finally {
				}
			},
			error : function onError(data) {
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
	};
}

function grantNewUserPermission() {

	var roleId = document.getElementById("roleEl").value;

	var queryJson = '{ queries : [ {queryid : "GRANT_NEW_USER_PERMISSION", params : [' + roleId + ', ' + selectedObjectId + '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : true,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Permission successfully granted for the selected user.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
	};

	lightboxCloseAlternate();
}

function prepForPermissionUpdate(elemId) {

	var oldRoleName = document.getElementById(elemId).value;

	var rolesCount = allRoles.length;
	var selectedIndex = 0;
	for ( var i = 0 ; i < rolesCount ; i++) {
		if (allRoles[i] == oldRoleName) {
			selectedIndex = i;
			oldRoleId = allRoleIds[i];
			break;
		}
	}

	$('#' + elemId).parent().hide();
	document.getElementById("form-submit-permission-update").style.display = "inline-block";
	document.getElementById("roles").style.display = "block";
	document.getElementById("roleEl").selectedIndex = selectedIndex;
	document.getElementById("form-submit-permission").style.display = "none";
	document.getElementById("form-submit-permission-grant").style.display = "none";
}

function viewLastInstance() {

	$('#' + elemId).parent().show();
	document.getElementById("form-submit-permission-update").style.display = "none";
	document.getElementById("roles").style.display = "none";
//	document.getElementById("roleEl").selectedIndex = selectedIndex;
	document.getElementById("form-submit-permission").style.display = "inline-block";
//	$('#form-submit-permission-view').show();
//	document.getElementById("form-submit-permission-grant").style.display = "none";
}

function updatePermission() {

	if (oldRoleId == "" || oldRoleId == null || oldRoleId == undefined) {
		lightboxCloseAlternate();
		return;
	}

	var newRoleId = document.getElementById("roleEl").value;
	if (newRoleId == "" || newRoleId == null || newRoleId == undefined) {
		lightboxCloseAlternate();
		return;
	}

	if (selectedObjectId == "" || selectedObjectId == undefined || selectedObjectId == null) {
		lightboxCloseAlternate();
		return;
	}

	if (oldRoleId == newRoleId) {
		lightboxCloseAlternate();
		return;
	}

	var queryJson = '{ queries : [ {queryid : "CHANGE_PERMISSION_FOR_A_USER", params : [' + newRoleId + ', ' + oldRoleId + ', ' +
			 selectedObjectId + '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson, pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Permission successfully revised.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError") = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
	};

	lightboxCloseAlternate();
}



// ######################################################################################################################################
// ##################################################		Revoke Permission	  ###############################################
// ######################################################################################################################################


function deletePermission(elemId) {

	if (selectedObjectId == "" || selectedObjectId == undefined || selectedObjectId == null) return;
	var roleName = document.getElementById(elemId).value;

	var userAction = confirm("Are you sure you wish to revoke this permission?");
	if (!userAction) return;

	var queryJson = '{ queries : [ {queryid : "REVOKE_PERMISSION_FOR_A_USER", params : ["' + roleName + '", ' + selectedObjectId +
			 '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool", format : "jsonp"
	};

	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Permission successfully revoked.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				document.getElementById("spanError") = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
	};

	lightboxCloseAlternate();
}



// ######################################################################################################################################
// ##################################################		Delete App Config	  ###############################################
// ######################################################################################################################################


function deleteAppConfig(configTitle) {

	var confirmDelete = confirm("Are you sure you wish to delete this configuration record?");
	if (!confirmDelete) return false;
	

	var objectId = checkExistenceOfObject(configTitle)[1];
	if (objectId <= 0) {
		return false;
	}

	var queryJson = '{ queries : [ {queryid : "DELETE_FROM_APP_CONFIG_BY_ID", params : [' + configTitle + '] },';
	queryJson = queryJson + ' {queryid : "DELETE_OBJECT_BY_ID", params : [' + objectId + '] },';
	queryJson = queryJson + ' {queryid : "REMOVE_PERMISSION_BY_OBJECT_ID", params : [' + objectId + '] } ] }';
	var xmlData = {
		service : "sql", action : "execute", query : queryJson, pool: "configpool", 
	};

	var isSuccess = true;
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : xmlData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Configuration record successfully removed.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				isSuccess = false;
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
		isSuccess = false;
	};

	return isSuccess;
}

function deleteSPFromDb(spTitle, spPoolName, isDeleteAppConfig) {

	if ( !isDeleteAppConfig) isDeleteAppConfig = false;
	
	var confirmDelete = confirm("Are you sure you wish to delete/recreate this configuration record?");
	if (!confirmDelete) return false;
	
	var objectId = checkExistenceOfObject(spTitle)[1];
	if (objectId <= 0) {
		return false;
	}

	var xmlData = {
		service : "sql", action : "deletesp", pool: spPoolName, sptitle: spTitle, format:"jsonp"
	};

	var isSuccess = true;
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : xmlData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData  = $.parseJSON(data);
					jsonData = jsonData[0];
					if ( jsonData['response'] == 'resultCode' )
					{
						if ( jsonData['values'][0]['code'] != "0037" )
						{
							isSuccess = false;
							document.getElementById("errorMessages").innerHTML = jsonData['values'][0]['message'];
						}
						else 
						{
							 if ( isDeleteAppConfig )
							 {
								if (deleteAppConfigSilent(spTitle) )
								{
									showMessage("Configuration record successfully removed.");
								}
							}
						}
					}
					
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				isSuccess = false;
				document.getElementById("errorMessages").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
		isSuccess = false;
	};

	return isSuccess;
}


function deleteAppConfigSilent(configTitle) {

	var objectId = checkExistenceOfObject(configTitle)[1];
	if (objectId <= 0) {
		return false;
	}
	
	var queryJson = '{ queries : [ {queryid : "DELETE_FROM_SP_APP_CONFIG_BY_ID", params : [' + configTitle + '] },';
	queryJson = queryJson + ' {queryid : "DELETE_OBJECT_BY_ID", params : [' + objectId + '] },';
	queryJson = queryJson + ' {queryid : "REMOVE_PERMISSION_BY_OBJECT_ID", params : [' + objectId + '] } ] }';
	
	var xmlData = {
		service : "sql", action : "execute", query : queryJson, pool: "configpool", format: "jsonp"
	};

	var isSuccess = true;
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : xmlData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					var jsonData  = $.parseJSON(data);
					jsonData = jsonData[0];
					if ( jsonData['response'] == 'resultCode' )
					{
						if ( jsonData['values'][0]['code'] != "0037" )
						{
							isSuccess = false;
							document.getElementById("errorMessages").innerHTML = jsonData['values'][0]['message'];
						}
					}
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				isSuccess = false;
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
		isSuccess = false;
	};

	return isSuccess;
}

function deleteFunction(funcId) {
	var confirmDelete = confirm("Are you sure you wish to delete this function record?");
	if (!confirmDelete) return false;

	var queryJson = '{ queries : [ {queryid : "DELETE_FUNCTION", params : [' + funcId + '] } ] }';
	
	var xmlData = {
		service : "sql", action : "execute", query : queryJson, pool: "configpool", 
	};

	var isSuccess = true;
	try {
		serverCallStart();
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : xmlData,
			cache : false,
			dataType : "text",
			success : function onSuccess(data) {
				try {
					showMessage("Function record successfully removed.");
				} finally {
					serverCallEnd();
				}
			},
			error : function onError(data) {
				serverCallEnd();
				isSuccess = false;
				document.getElementById("spanError").innerHTML = data.responseText;
			}
		});
	} catch(error) {
		serverCallEnd();
		isSuccess = false;
	};

	return isSuccess;
}


// ######################################################################################################################################
// ############################################	     Small pop-up for user role permissions	  #######################################
// ######################################################################################################################################



window.document.onkeydown = function (e) {
	if (!e){
		e = event;
	}
	if (e.keyCode == 27){
		lightboxClose();
		lightboxCloseAlternate();
	}
}

function lightboxOpenAlternate() {
	document.getElementById('overlay-modal-alternate').style.display='block';
	document.getElementById('overlay-background-alternate').style.display='block';  
}

function lightboxCloseAlternate() {
	resetPermissionDivs();
	resetUserDivs();
	resetButtonDivs();
	$('#mainPermissionDiv').html('User Permissions');
	document.getElementById('overlay-modal-alternate').style.display='none';
	document.getElementById('overlay-background-alternate').style.display='none';
}