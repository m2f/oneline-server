var queryId;
function getFirstKeyObj(data)
{
	var obj;
	for ( var e in data)
	{
		obj = data[e];
	}
	return obj;
}

function getApi(configId) {

	var queryJson = '{ queries : [ {queryid : "GET_APP_CONFIG_BY_ID", params : [' + configId + '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool",  format : "jsonp"
	};

	try {
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : true,
			dataType : "text",
			success : function onSuccess(data) {
				var jsonData = $.parseJSON(data);
				var json = jsonData[0];
				jsonData = json["values"];
				if (jsonData == "" || jsonData == null || jsonData == undefined) return;
				$('#bodyEl').val(jsonData[0]["body"]);
				queryId = jsonData[0]["title"];
			},
			error : function onError(data) {
				$('#spanError').html(data.responseText);
			}
		});
	} catch(error) {};
}

function getFunction(funcId) {

	var queryJson = '{ queries : [ {queryid : "GET_FUNCTION_BY_ID", params : [' + funcId + '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool",  format : "jsonp"
	};

	try {
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : true,
			dataType : "text",
			success : function onSuccess(data) {
				var jsonData = $.parseJSON(data);
				var json = jsonData[0];
				jsonData = json["values"];
				if (jsonData == "" || jsonData == null || jsonData == undefined) return;
				$('#funcBodyEl').val(jsonData[0]["funcBody"]);
				$('#funcIdEl').val(jsonData[0]["funcId"]);
				//queryId = jsonData[0]["funcId"];
			},
			error : function onError(data) {
				$('#spanError').html(data.responseText);
			}
		});
	} catch(error) {};
}

function getSp(sp_title) {

	var queryJson = '{ queries : [ {queryid : "GET_SP_BY_TITLE", params : [' + sp_title + '] } ] }';
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool",  format : "jsonp"
	};

	try {
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : true,
			dataType : "text",
			success : function onSuccess(data) {
				var jsonData = $.parseJSON(data);
				var json = jsonData[0];
				jsonData = json["values"];
				if (jsonData == "" || jsonData == null || jsonData == undefined) return;
				$('#sp_poolnameEl').val(jsonData[0]["sp_poolname"]);
				$('#sp_titleEl').val(jsonData[0]["sp_title"]);
				$('#sp_bodyEl').val(atob(jsonData[0]["sp_body"]));
				$('#sp_call_syntaxEl').val(jsonData[0]["sp_call_syntax"]);
				$('#sp_out_varEl').val(jsonData[0]["sp_out_var"]);
			},
			error : function onError(data) {
				$('#spanError').html(data.responseText);
			}
		});
	} catch(error) {};
}

function populateParameterCountBox() {

	var paramsCountElem = document.getElementById('paramsTEl');
	for (var i = 1 ; i <= 15 ; i++) {
		paramsCountElem.options[paramsCountElem.options.length] = new Option(i,i);
	}
}

function showParamDivs() {

	var paramsT = $('#paramsTEl').val();

	for (var i = 1 ; i <= paramsT ; i++) {
		$('#params' + i + 'Div').show();
	}
	for (var j = 15 ; j > paramsT ; j--) {
		$('#params' + j + 'Div').hide();
	}
}

function runApi() {

	var poolName = document.getElementById('pooldbEl').value;
	if ( ! poolName) poolName = "configpool";
	
	WriteCookie("poolname", poolName, 1);
	var postData, paramCount;
	paramCount = $('#paramsTEl').val();
	paramCount = parseInt(paramCount);

	var queryJson = '{queries:[{queryid:' + queryId + ',params:[';
	var isFirst = true;
	for (var i = 1 ; i <= paramCount ; i++) {
		if (isFirst) {
			queryJson = queryJson + "'" + $('#params' + i + 'El').val() + "'";
			isFirst = false;
			continue;
		}
		queryJson = queryJson + ",'" + $('#params' + i + 'El').val() + "'";
	}

	queryJson = queryJson + ']}]}';

	//Comment to show at a single window.
	window.open( "http://" + location.host + location.pathname.replace("sqlexecute.html","dataservice.html") +  
			"?service=sql&action=execute&format=jsonp&pool=" + poolName + "&query=" + 
			queryJson , "_self");	
	
	/**
	var postData = {
		service : "sql", action : "execute", query : queryJson,  pool: "configpool",  format : "jsonp"
	};

	try {
		$.ajax ({
			type : "GET",
			async : false,
			url : "dataservice.html",
			data : postData,
			cache : true,
			dataType : "text",
			success : function onSuccess(data) {
				var jsonData = $.parseJSON(data)["values"];
				if (jsonData == "" || jsonData == null || jsonData == undefined) return;
				$('#testThis').hide();
				$('#testThis2').show();
				$('#testThis2').html(data);
			},
			error : function onError(data) {
				$('#spanError').html(data.responseText);
			}
		});
	} catch(error) {};
	*/
}

function runFunction(funcId) {

	var poolName = document.getElementById('pooldbEl').value;
	if ( ! poolName) poolName = "configpool";
	
	WriteCookie("poolname", poolName, 1);
	var postData, paramCount;
	
	var queryJson = "{queries:[{functionid:" + 
	funcId + " }]}";
	

	//Comment to show at a single window.
	window.open( "http://" + location.host + location.pathname.replace("functionexecute.html","dataservice.html") +  
			"?service=sql&action=execute&format=jsonp&pool=" + poolName + "&query=" + 
			queryJson , "_self");		
}

function runSp(sp_title) {

	var poolName = document.getElementById('sp_poolnameEl').value;
	if ( ! poolName) poolName = "configpool";
	
	var variables = document.getElementById("sp_out_varEl").value;

	//WriteCookie("poolname", poolName, 1);
	var postData, paramCount;
	
	//var queryJson = "{queries:[{functionid:" + 
	//funcId + " }]}";
	

	//Comment to show at a single window.
	window.open( "http://" + location.host + location.pathname.replace("spexecute.html","dataservice.html") +  
			"?service=sql&action=executesp&format=jsonp&pool=" + poolName + "&storedprocid=" + 
			sp_title + "&variables={\"variables\":[" + variables + "]}" , "_self");		
}
