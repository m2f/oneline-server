function isName(str, elem) {
    var re = /^[A-Za-z](['. -])?([A-Za-z](['. -])?)+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isSpclName(str, elem) {
    var re = /[#@$%\^&\*\(\)\-+=_`~!;:'",.<>\/?\[\]]/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isPhoneNumber(str, elem) {
    var re = /^\d{10}$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}


/**
function isPhoneNumber(str, elem) {
    str = str.replace(" ", "");
    str = str.replace("-", "");
    if (str.length > 1) {
        if (str.charAt(0) == '+') str = str.substr(1);
    }

    if (lengthRestriction(str, elem, 8, 12)) {
        return isNumber(str, elem);
    } else {
        return false;
    }
    
}
*/

function isEmail(str, elem) {
    var re = /^[A-Za-z0-9]+[\w.-]*?[A-Za-z0-9]+@[A-Za-z0-9]+[\w.-]*?\.[A-Za-z0-9]{2,5}$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isStreet(str, elem) {
    var re = /^\d{2,}\s\b[A-Z][\w .]*[^\W]\.?$/i;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isZip(str, elem) {
    var re = /^\d{6}$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isNumber(str, elem) {
    var re = /^\d+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isNotEmpty(str, elem) {
    var re = /\w+/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isEmpty(str, elem) {
    var strLen = (null == str) ? 0 : str.replace(" ", "").length;
    if (strLen == 0 ) {
		elem.focus();
        return true;
    }
    return false;
}

function isAlphabet(str, elem) {
    var re = /^[a-zA-Z]+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isAlphanumeric(str, elem) {
    var re = /^[0-9a-zA-Z]+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isAlphanumericWithSpace(str, elem) {
    var re = /^[0-9a-zA-Z ]+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function isAlphanumericText(str, elem) {
    var re = /^[0-9a-zA-Z \r\n]+$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function lengthRestriction(str, elem, min, max) {
    if (str.length >= min && str.length <= max) {
        return true;
    } else {
        elem.focus();
        return false;
    }
}

// 1 upper case letter
// 1 lower case letter
// 1 special case letter
// Minimum 8 charaters
function isPassword(str, elem) {
    var re = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}

function compareStrings(str1, str2, elem) {

	if (str1 == str2) {
	    elem.focus();
	    return false;
    }
	return true;
}

function isEqual(number1, number2, elem) {

	if (number1 == number1) return true;
	elem.focus();
	return false;
}

function isGreater(number1, number2, elem) {
	
	if( number1 > number2) return true;
	elem.focus();
	return false;
}


function isNumberOrDecimal(str, elem) {
    var re = /^\d*\.?\d*$/;
    if (str.match(re)) {
        return true;
    }
    elem.focus();
    return false;
}
