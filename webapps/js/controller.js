var separator = "~~~";
var lineseparator = "~-~";

function encode_utf8(s) 
{
	return unescape(encodeURIComponent(s));
}

function decode_utf8(s) 
{
	return decodeURIComponent(escape(s));
}

function WriteCookie(poolname, value, expires)
{
	var expires = new Date();
	expires.setTime(expires.getTime() + (1000 * 60 * 60 * 24 * 5));
	cookievalue= escape(document.getElementById('pooldbEl').value) + ";";
	document.cookie= poolname + "=" + cookievalue + "; path=/" + ((expires == null) ? "" : "; expires=" + expires.toGMTString());
}

function getCookie(poolname) 
{
	var name = poolname + "=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1);
		if (c.indexOf(name) == 0) {
			return c.substring(name.length, c.length);
		}
	}
	return "configpool";
}		

function eraseCookie(poolname) 
{
	WriteCookie(poolname,"",-1);
}

function LoadFunction(page){
	var name = page;
	var url = window.location.href;
	var size = url.length;
	
	if("#" == url.charAt(size -1))
	{
	url = url.substring(0, size - 1);
	window.location.href=url;
	}
		
	var newvalname = Cookies["pagename"];
	var newval = Cookies["pagevalue"];
	Cookies.eraseCookie("pagename");
	Cookies.eraseCookie("pagevalue");
    var pageVal = decodeURIComponent(newval);
	
	if(name == newvalname)
	{
		 var size = pageVal.length;
		 if("#" == pageVal.charAt(size - 1))
		 {
			 pageVal = pageVal.substring(0,pageVal.charAt(size - 2));
						 
		 }
		 pageValParts = pageVal.split("qfilter=");
		 var firstPart = pageValParts[0];
		 var secondPart = encodeURIComponent(pageValParts[1]);
		 window.location.href = firstPart + "qfilter=" + secondPart;
	}
	
}	



function serverCallStart()
{
	document.body.style.cursor = "progress";
	document.getElementById("divOverlay").style.display = "block";
}

function serverCallEnd()
{
	document.body.style.cursor = "auto";
	document.getElementById("divOverlay").style.display = "none";
}


function paintHeader(which) {

	var elemImage = document.createElement("img");
	elemImage.src = "images/ajax-loader.gif";
	elemImage.className = "overlayimage";
	var elemDiv = document.createElement("div");
	elemDiv.id = "divOverlay";
	elemDiv.className = "overlay";
	elemDiv.appendChild(elemImage);
	document.body.appendChild(elemDiv);
	
	var whichHeader =  "header_admin.html";
	
	try
	{
	     serverCallStart();
	     $.ajax({
			    type: "GET",
			    async: true,
			    url: whichHeader,
			  	cache:true,
			  	dataType: "text",
			    success: function onSuccess(data) {
				    try
				    {
					    document.getElementById("divHeader").innerHTML = data;
						var displayName = "Admin";
						$('.loggedin-as').html('You are logged in as "' + displayName +'"' );
					}finally{ 
						serverCallEnd();
				    }
			    },
			   error: function onError(data){
			   serverCallEnd();
			   document.getElementById("spanError").innerHTML = data.statusText;
	                
			   }
		   });
	}
	catch(error){  
	   serverCallEnd();
	};

}


function paintFooter() {

	try
	{
	     serverCallStart();
	     $.ajax({
			    type: "GET",
		  	    async: true,
		  	    url: "footer.html",
		  	    cache:true,
		  	    dataType: "text",
			    success: function onSuccess(data) {
				    try
				    {
				    	document.getElementById("divFooter").innerHTML = data;
				    }finally{ 
				    	serverCallEnd();
				    }
			    },
			   error: function onError(data){
			   serverCallEnd();
			   document.getElementById("spanError").innerHTML = data;
			   }
		   });
	}
	catch(error){  
	   serverCallEnd();
	};

}



function gotoDataAPI()
{
	window.location.href = "index.html";
}

function gotoFunction()
{
	window.location.href = "function.html";
}

function gotoSP()
{
	window.location.href = "sp.html";
}

function gotoHelp()
{
	window.location.href = "help.html";
}

function gotoSequence()
{
	window.location.href = "sequence.html";
}

function gotoFaq()
{
	window.location.href = "FAQs.html";
}

function gotoInfraSetup()
{
	window.location.href = "infra_setup.html";
}


function populateSelectBox(selectionElemId, optionData)
{
	var options = [];
	options.push('<option value="Select">Select</option>');
	for( var label in optionData)
	{
		options.push('<option value="' + optionData[label] + '">' + label + '</option>');
	}
	$("#" + selectionElemId ).html(options.join(''));
	$("#" + selectionElemId ).addClass('input--selectbox');
}

function populateSelectBoxValLabel(selectionElemId, optionData)
{
	var options = [];
	options.push('<option value="Select">Select</option>');
	for( var label in optionData)
	{
		options.push('<option value="' + label + '">' + optionData[label] + '</option>');
	}
	$("#" + selectionElemId ).html(options.join(''));
	$("#" + selectionElemId ).addClass('input--selectbox');
}

function showMessage(message) {
    showPermanentMessage(message);
    setTimeout( 'removeMessage();', 5000);
}

function showPermanentMessage(message) {
	
	if ( ! message  ) message = "";
	
	var divMessageEl = document.getElementById("spanError");
	
	message = message.trim();
	var messgeLen = message.length;
	if ( messgeLen > 2 ) {
		if ( message.charAt(messgeLen-1) == '.') 
			message = message.substring(0, messgeLen-1);
	}
	if ( message.indexOf('.') > 0 ) {
		message = message.replace(/\./g,'.</li><li>')
		message = "<ul><li>" + message + "</li></ul>"; 
	} 
	
    divMessageEl.innerHTML = message;
    divMessageEl.style.display = "block";
    $('.form-message').css( { "display" : "block" } );
    
}

function removeMessage() {
	var divMessageEl = document.getElementById("spanError");
    divMessageEl.style.display = "none";
}

function goToHome()
{
	window.location.href = "home_admin.html";
}

function paintHeaderAndFooter()
{
 	paintHeader("root");
    paintFooter();
}

function getUrlVars() {
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}

function initdatepickercontrols(elems, format)
{
	// Initialize all datepicker controls and make them readonly
	for(var el in elems)
	{
		$('#' + elems[el]).datepicker( "option", "dateFormat", format);
		$('#' + elems[el]).attr( { readonly : "readonly" } );
	}
}

function getDecodedUrlVars(url) {
    var vars = [], hash;
    var hashes =url.slice(url.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++) {
        hash = hashes[i].split('=');
        vars.push(hash[0]);
        vars[hash[0]] = hash[1];
    }
    return vars;
}


function trimTimeFrom (dateTimeField) {

	if (null == dateTimeField || dateTimeField == "") return dateTimeField;
	dateTimeField = dateTimeField.toString();
	return dateTimeField.substring(0,10);
}

function removeTrailingZeroFrom (mysqlTimeStamp) {

	if (null == mysqlTimeStamp || mysqlTimeStamp == "") return mysqlTimeStamp;
	mysqlTimeStamp = mysqlTimeStamp.toString();
	return mysqlTimeStamp.substring(0,mysqlTimeStamp.indexOf('.'));
}

function removeDecimalFrom (decimalNumber) {

	if (null == decimalNumber || decimalNumber == "") return decimalNumber;
	return decimalNumber.substring(0,decimalNumber.indexOf('.'));
}

function removeZeroFromTime(val){
	var stringVal=val.toString();
	return stringVal.substring(0,10);
}

function gotoLoginPage()
{
}


function refreshAuth()
{
	var xmldata = {
			service: "sql",
			action: "refresh",
			format: "jsonp"
	}
	try
	{
	     serverCallStart();
	     $.ajax({
			  type: "GET",
			  async: true,
			  url: "dataservice.html",
			  data: xmldata,
			  cache: true,
			  dataType: "text",
	          success: function onSuccess(data) {
			     try
			     {
			    	 showPermanentMessage("API authorization access refreshed.");
			    }finally{ 
						serverCallEnd();
		         }
	           },
			   error: function onError(data){
					serverCallEnd();
					var errorJson = $.parseJSON(data.responseText);
					errorJson = errorJson[0];
					errorJson = errorJson['values'][0];
					var errorMsg = errorJson["code"] + " : " + errorJson["message"];
					showPermanentMessage(errorMsg);
			   }
		   });
	}
	catch(error){  
	   serverCallEnd();
	}			
}

function refreshFuncAuth()
{
	var xmldata = {
			service: "sql",
			action: "refreshFunc",
			format: "jsonp"
	}
	try
	{
	     serverCallStart();
	     $.ajax({
			  type: "GET",
			  async: true,
			  url: "dataservice.html",
			  data: xmldata,
			  cache: true,
			  dataType: "text",
	          success: function onSuccess(data) {
					try {
						var jsonData  = $.parseJSON(data);
						jsonData = jsonData[0];
						if ( jsonData['response'] == 'resultCode' )
						{
							if ( jsonData['values'][0]['code'] != "0037" )
							{
								isSuccess = false;
								showMessage(jsonData['values'][0]['message']);
							} else {
								showMessage("Funtion refreshed successfully.");
							}
						}
					} finally {
						serverCallEnd();
					}
	           },
			   error: function onError(data){
					serverCallEnd();
					var errorJson = $.parseJSON(data.responseText);
					errorJson = errorJson[0];
					errorJson = errorJson['values'][0];
					var errorMsg = errorJson["code"] + " : " + errorJson["message"];
					showPermanentMessage(errorMsg);
			   }
		   });
	}
	catch(error){  
	   serverCallEnd();
	}			
}
function testApi(configId)
{
	window.location.href = "sqlexecute.html?configid=" + configId;
}

function testFunction(funcId)
{
	window.location.href = "functionexecute.html?funcId=" + funcId;
}

function testSp(sp_title)
{
	window.location.href = "spexecute.html?sp_title=" + sp_title;
}

function gotoResetPassword()
{
	window.location.href = "reset.html";
}
