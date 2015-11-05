////////////////////////////////////////////////////////
////////////// QUERY FORMING //////////////////////
////////////////////////////////////////////////////////

var OnelineTable = function (name, filterHtml) {

    this.name = name;

    if (filterHtml) this.filterHtml = filterHtml;
    this.filterHtml = "";

    this.offset = 0;
    this.filterVals = [];
    this.pageSize = 5;
    this.limit = this.pageSize;
    this.PAGENO_PREV = -2;
    this.PAGENO_NEXT = -1;
    this.PAGENO_START = -102;
    this.PAGENO_END = -101;
    this.SEGMENT_SIZE = 4;
    this.resizableTables = new Array();
   
    this.tableListFilter;
    this.tableListSortFld;
    this.tableListSortAsc  = true;
    this.tableListSortPart;
    this.tableDiv;

	this.selectFilterValCodes = [];
	this.selectFilterValDescCodes = [];
	this.filterFldDataType = [];
    
    this.rowLoadCounter= -1;
    this.loadJSonF;

    this.records ;
    this.listPaginateDiv = this.name + "paginateDiv";
    this.getTotalRowsForPagination;
};

OnelineTable.prototype.registerLoadJsonFunction = function (loadJSonF) {
    this.loadJSonF = loadJSonF;
}

OnelineTable.prototype.makeSortQuery = function (sortFld, sortVal) {
    if (!sortFld) return "";
    if (sortVal) return (sortFld + " asc ");
    else return (sortFld + " desc ");
};

OnelineTable.prototype.makeFilterQuery = function (filterVals, filterFldDataType) {
    return this.makeFilterQueryCallback(filterVals, filterFldDataType, null);
};


OnelineTable.prototype.openAPage = function (pageNo)  {
	this.offset = pageNo * this.pageSize - this.pageSize;
	this.loadJSonF(this.tableListSortPart, this.tableListFilter);
};

OnelineTable.prototype.makeFilterQueryCallback = function (filterVals, filterFldDataType, callBack) {

    var isFirst = true; var query = "";

    if (!filterFldDataType) filterFldDataType = [];

    var queryPart = "";
    if (filterVals) {
        for (var fld in filterVals) {

            if (filterVals[fld].trim().length == 0) continue;

            queryPart = "";
            var type = (!filterFldDataType[fld] || filterFldDataType[fld].length == 0) ? 's' : filterFldDataType[fld];
            switch (type) {
                case 's':
                    {
                        var moreFilter = (callBack) ? callBack(fld, filterVals[fld]) : "";
                        if (moreFilter.trim().length == 0)
                            queryPart = queryPart + fld + " LIKE '" + filterVals[fld] + "%'";
                        else
                            queryPart = queryPart + " ( " + fld + " LIKE '" + filterVals[fld] + "%' " + moreFilter;
                    }
                    break;

                case 'i':
                    {
                        var moreFilter = (callBack) ? callBack(fld, filterVals[fld]) : "";
                        if (moreFilter.trim().length == 0)
                            queryPart = queryPart + fld + " = '" + filterVals[fld] + "'";
                        else
                            queryPart = queryPart + " ( " + fld + " = '" + filterVals[fld] + "'" + +moreFilter;
                    }
                    break;

                case 'd':
                    {
                        var val = this.filterVals[fld];
                        if (val.length < 10) {
                            this.filterVals[fld] = '';
                            break;
                        }

                        if (val.indexOf('..') != -1) // Date Range ( D1 <= fld and D1 >= fld )
                        {
                            var parts = val.split('..');
                            queryPart = queryPart + " ( '" + parts[0] + "' <= " + fld + " AND '" + parts[1] + "' >= " + fld + " ) ";
                            break;
                        }

                        var firstChar = val.charAt(0);
                        if (firstChar == '>' || firstChar == '<') {
                            var secondChar = val.charAt(1);
                            if (secondChar == '=') {
                                val = val.substring(2);
                                queryPart = queryPart + fld + " " + firstChar + "= '" + val + "'";
                            }
                            else {
                                val = val.substring(1);
                                queryPart = queryPart + fld + " " + firstChar + " '" + val + "'";
                            }
                        }
                        else {
                            queryPart = queryPart + fld + " = '" + filterVals[fld] + "'";
                        }
                    }
                    break;
            }

            if (queryPart.trim().length > 0) {
                if (isFirst) isFirst = false;
                else query = query + " AND ";

                query = query + queryPart;
            }
        }
    }
    return (query.trim());
};


OnelineTable.prototype.listTableOnSort = function (sortField) {
	this.tableListSortFld = sortField;
	if (this.tableListSortFld) {
		if (this.tableListSortFld == sortField)
			this.tableListSortAsc = !this.tableListSortAsc;
		else
			this.tableListSortAsc = true;
	} else {
		this.tableListSortAsc = true;
	}
	this.tableListSortPart = this.makeSortQuery(this.tableListSortFld, this.tableListSortAsc);
	this.loadJSonF(this.tableListSortPart, this.tableListFilter);
	this.tableListSortFld = sortField;
	return this.tableListSortAsc;
}

OnelineTable.prototype.listTableOnFilter = function (val) {
	this.tableListFilter = val;
	this.loadJSonF(this.tableListSortPart, this.tableListFilter);
}


OnelineTable.prototype.loadJSonData = function (data,  headers, cols, filterFields, sortFields, docTag, intFilter, dateFilter, onRowCallback, listsfilterDecoratorF) {

	try {
		
		var dataRecord = $.parseJSON(data);
		var firstDataObj   = dataRecord[0];
		var firstQueryIdData;

		this.records = firstDataObj["values"];
		//alert(">>" + this.records.length);
		if (this.records.length == 0) {
			if (this.offset > 0) {
				this.offset = this.offset - this.limit;
				this.loadJSonF(this.tableListSortPart, this.tableListFilter);
				return;
			}
		}
	} catch (error) {
		document.getElementById("spanError").innerHTML = "Rendering Failure : " + data.responseText;
		return ;
	}


	this.pushToIntFilter(this.filterFldDataType, intFilter);
	this.pushToDateFilter(this.filterFldDataType, dateFilter);
				

	for (var fld in filterFields) {
		if (!this.filterVals[filterFields[fld]])
			this.filterVals[filterFields[fld]] = '';
	}

    var filterDecorator = ( null == listsfilterDecoratorF) ? this.dummyFilterDecorator : listsfilterDecoratorF;

	this.renderBoxTableCallback(data, docTag, headers, cols, filterFields,
			filterDecorator, sortFields, (this.name + ".listTableOnSort"),
            this.listPaginateDiv, "100", "table-plain resizable", this.tableDiv,
            onRowCallback, this.tableListSortFld, this.tableListSortAsc);

	resizableColumns();
	if ( this.offset == 0  ) {
        this.checkPagination(this.pageSize, this.listPaginateDiv, this.getTotalRowsForPagination);
    }
}

OnelineTable.prototype.dummyFilterDecorator = function (filterField) {
    return "";
};

////////////////////////////////////////////////////////
//////////////  DIFFERENT STYLE TABLE   //////////////////////
////////////////////////////////////////////////////////
OnelineTable.prototype.renderBoxTableCallback = function (data, docName, headers, cols,
		filterFields, filterDecoratorFunc, sortFields, onSort, onPaginate,
		width, renderstyle, tableElement, rowCallback, sortFld, sortVal, sumCallback) {

    var htmlSnippets = [];
    var snippetSq = 0;
    var found = 0;

    try {
        htmlSnippets[snippetSq++] = this.filterHtml;
        htmlSnippets[snippetSq++] = (width == -1) ?
            ("<table class=\"" + renderstyle + "\" >") :
            ("<table class=\"" + renderstyle + "\" width=\"" + width + "%\" data-component=\"Resizer\">");

        htmlSnippets[snippetSq++] = "<thead><tr>";
        var headersT = headers.length;
        var sortsT = (sortFields) ? sortFields.length : 0;
        for (var i = 0; i < headersT; i++) {
            var aHeaderCol = "<th scope=\"col\" >" + headers[i] + "</th>";
            for (var s = 0; s < sortsT; s++) {
                if (sortFields[s] == cols[i]) {

                    var imgElem = '<img class="sortimg" src="table-images/no-sort.png" />';
                    if (sortFld) {
                        if (sortFields[s] == sortFld) {
                            if (sortVal) {
                                imgElem = '<img class="sortimg" src="table-images/up.png" />';
                            } else {
                                imgElem = '<img class="sortimg" src="table-images/down.png" />';
                            }
                        }
                    }

                    //                    aHeaderCol = '<th scope=\"col\"><a href=# onClick=\"' + onSort +  '(\'' + sortFields[s] + '\');\">' + imgElem + '</a>'+  headers[i] +
                    aHeaderCol = '<th scope=\"col\"  onClick=\"' + onSort + '(\'' + sortFields[s] + '\');\">' + imgElem + headers[i] +
							'</th>';
                }
            }
            htmlSnippets[snippetSq++] = aHeaderCol;
        }

        // Filter Section

        var filtersT = (filterFields) ? filterFields.length : 0;

        if (filtersT > 0) {

            htmlSnippets[snippetSq++] = "</tr></thead><thead>";
            htmlSnippets[snippetSq++] = "<thead><tr>";

            for (var i = 0; i < headersT; i++) {
                var aFilterCol = "<th scope=\"col\" ></th>";
                for (var f = 0; f < filtersT; f++) {
                    if (filterFields[f] == cols[i]) {
                        aFilterCol = (filterDecoratorFunc(filterFields[f])) ? "<th scope=\"col\" >" + filterDecoratorFunc(filterFields[f]) + "</th>" :  "<th scope=\"col\" ></th>" ;
                    }
                }
                htmlSnippets[snippetSq++] = aFilterCol;
            }
        }

        htmlSnippets[snippetSq++] = "</tr></thead><tbody>";


        var rowHtml = "";
        var jsonData = $.parseJSON(data);
        jsonData = jsonData[0];
        var record = jsonData[docName];
        //alert("d>" + record);
        
        this.rowLoadCounter  = -1;
        for (var r in record) {
            this.rowLoadCounter++;
            rowHtml = "<tr>";
            for (var c = 0; c < headersT; c++) {
                var colText = record[r][cols[c]];
                if ( ! colText ) colText = "";
                if (rowCallback) {
                    rowHtml = rowHtml + '<td>' + rowCallback(this.rowLoadCounter, c, colText) + "</td>";
                } else {
                    rowHtml = rowHtml + '<td>' + colText + "</td>";
                }
            }
            rowHtml = rowHtml + "</tr>";
            htmlSnippets[snippetSq++] = rowHtml;
            found++;
        }

        if (sumCallback) {
            rowHtml = "<tr>";
            for (var c = 0; c < headersT; c++) {
                rowHtml = rowHtml + '<td><b style="font-size: 150%;">' + sumCallback(c) + '</b></td>';
            }
            rowHtml = rowHtml + "</tr>";
            htmlSnippets[snippetSq++] = rowHtml;
        }

        var paginateDivExisting = document.getElementById(onPaginate);
        var paginateDivExistingHtml = (!paginateDivExisting || null == paginateDivExisting) ? "" : paginateDivExisting.innerHTML;

        htmlSnippets[snippetSq++] = '<tr><td class="nextprev" colspan="' + headersT + '">' + '<div id="' + onPaginate + '" ></div>' + '</td></tr>';
        htmlSnippets[snippetSq++] = "</tbody></table>";
        tableElement.innerHTML = htmlSnippets.join('');

        document.getElementById(onPaginate).innerHTML = paginateDivExistingHtml;

        return found;

    } catch (error) {
        document.write("<h2>Error during processing box table rendering</h2><h3>" + error + "</h3>" + data);
    }
};

////////////////////////////////////////////////////////
//////////////  PAGINATION   //////////////////////
////////////////////////////////////////////////////////


// START -10  PREV  1 2 3 4 5 6 7 8 9 10  NEXT +10 END


OnelineTable.prototype.checkPagination  = function (firstPageDataCount, paginateDivId, totalRecordFunction) {
    if (firstPageDataCount < this.pageSize) {
        paginateDiv = document.getElementById(paginateDivId);
        paginateDiv.style.display = "none";
        return;
    }
    totalRecordFunction();
}

OnelineTable.prototype.getPaginationIds  = function (queryId) {
    var startId = queryId + "PaginationStart";
    var prevSegmentId = queryId + "PaginationPrevSegment";
    var prevId = queryId + "PaginationPrev";
    var nextId = queryId + "PaginationNext";
    var nextSegmentId = queryId + "PaginationNextSegment";
    var endId = queryId + "PaginationEnd";

    var currentId = queryId + "count";
    var segmentId = queryId + "segment";

    return [startId, prevSegmentId, prevId, nextId, nextSegmentId, endId, currentId, segmentId ];
}

OnelineTable.prototype.createAPageLink  = function (snippets, queryId, pageNo, noOfpages, onPageClickFuncName) {
    snippets.push(queryId); snippets.push('", '); snippets.push(pageNo); snippets.push(',');
    snippets.push(noOfpages);
    if (onPageClickFuncName) {
        snippets.push(', '); snippets.push(onPageClickFuncName);
    }
}

OnelineTable.prototype.fillFilterVal  = function (filterField, value) {
    this.filterVals[ filterField ] = value;
}

OnelineTable.prototype.buildPageElems = function (snippets, startPage, endPage, startSegment, queryId, noOfpages, onPageClickFuncName) {

    var segmentPageCutoff = startSegment * this.SEGMENT_SIZE;
    for (var i = startPage; i <= endPage; i++) {

        snippets.push('<a href="#" class="pageNo" style="display:');
        if (i > segmentPageCutoff) snippets.push('none"');
        else snippets.push('block inline"');

        snippets.push(' id="'); snippets.push(queryId); snippets.push("page"); snippets.push(i);
        snippets.push('" onclick=\'' + this.name + '.onPageClick("');
        this.createAPageLink(snippets, queryId, i, noOfpages, onPageClickFuncName);
        snippets.push(')\'>');
        snippets.push(i);
        snippets.push('</a>');
    }
}


OnelineTable.prototype.buildPaginationElement = function (queryId, onPageClickFuncName, foundTotals) {

    var paginateDivId = this.listPaginateDiv;
    if (foundTotals <= this.pageSize) {
        paginateDiv = document.getElementById(paginateDivId);
        paginateDiv.parentNode.style.display = "none";
        paginateDiv.innerHTML = "";
        return;
    }

    var paginationIds = this.getPaginationIds(queryId);
    var startId = paginationIds[0],  prevSegmentId = paginationIds[1], prevId = paginationIds[2], nextId = paginationIds[3];
    var nextSegmentId = paginationIds[4], endId = paginationIds[5], currentId = paginationIds[6], segmentId = paginationIds[7];

    var noOfpages = Math.ceil(foundTotals / this.pageSize);

    var snippets = [];

    //Begin Click
    snippets.push('<a href="#" class="pagePrevious" id="');
    snippets.push(startId); snippets.push('" onclick=\'' + this.name + '.onPageClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_START, noOfpages, onPageClickFuncName);
    snippets.push(')\'>Start</a>');

    //Segment Previous Click
    snippets.push('<a href="#" class="pagePrevious" id="');
    snippets.push(prevSegmentId); snippets.push('" onclick=\'' + this.name + '.onSegmentClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_PREV, noOfpages, null);
    snippets.push(')\'><<</a>');

    //Previous Click
    snippets.push('<a href="#" id="'); snippets.push(prevId);
    snippets.push('" class="pagePrevious" onclick=\'' + this.name + '.onPageClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_PREV, noOfpages, onPageClickFuncName);
    snippets.push(')\'>Previous</a>');

    //Build pages
    this.buildPageElems(snippets, 1, noOfpages, 1, queryId, noOfpages, onPageClickFuncName)

    //Next Click
    snippets.push('<a href="#" id="');
    snippets.push(nextId);
    snippets.push('" class="pageNext" onclick=\'' + this.name + '.onPageClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_NEXT, noOfpages, onPageClickFuncName);
    snippets.push(')\'>Next</a>');

    //Segment Next click
    snippets.push('<a href="#"  class="pageNext"  id="');
    snippets.push(nextSegmentId); snippets.push('" onclick=\'' + this.name + '.onSegmentClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_NEXT, noOfpages, null);
    snippets.push(')\'>>></a>');

    //End click
    snippets.push('<a href="#" class="pageNext"  id="');
    snippets.push(endId); snippets.push('"  onclick=\'' + this.name + '.onPageClick("');
    this.createAPageLink(snippets, queryId, this.PAGENO_END, noOfpages, onPageClickFuncName);
    snippets.push(')\'>End</a>');

    //Keep the current displayed Id
    snippets.push('<span id="');
    snippets.push(currentId);
    snippets.push('" style="display:none">1</span> ');

    //Keep the current Segment Id
    snippets.push('<span id="');
    snippets.push(segmentId);
    snippets.push('" style="display:none">1</span> ');

    paginateDiv = document.getElementById(paginateDivId);
    paginateDiv.innerHTML = snippets.join('');
    paginateDiv.style.display = "block";
}

OnelineTable.prototype.onSegmentClick = function (queryId, askedSegment, totalPage, askedPageNo) {

    var paginationIds = this.getPaginationIds(queryId);
    var startId = paginationIds[0], prevSegmentId = paginationIds[1], prevId = paginationIds[2], nextId = paginationIds[3];
    var nextSegmentId = paginationIds[4], endId = paginationIds[5], currentId = paginationIds[6], segmentId = paginationIds[7];

    var segmentElem = document.getElementById(segmentId);
    var segmentNo = parseInt(segmentElem.innerHTML);
    var currentSegment = segmentNo;

    if (askedSegment == this.PAGENO_PREV) {
        segmentNo = segmentNo - 1;
    } else if (askedSegment == this.PAGENO_NEXT) {
        segmentNo = segmentNo + 1;
    } else {
        segmentNo = askedSegment;
    }

    if (segmentNo < 1) segmentNo = 1;
    var totalSegments = Math.ceil(totalPage / this.SEGMENT_SIZE);
    if (segmentNo > totalSegments) segmentNo = totalSegments;

    var segmentPageStart = ((segmentNo - 1) * this.SEGMENT_SIZE) + 1;
    var segmentPageEnd = ( totalSegments > 1 ) ? (segmentNo * this.SEGMENT_SIZE) : totalPage;

    if (segmentPageStart < 1) segmentPageStart = 1;
    if (segmentPageEnd < 1 || segmentPageEnd > totalPage) segmentPageStart = totalPage;

    var lastPageBegin = (totalPage - this.SEGMENT_SIZE + 1);
    if (segmentPageStart > lastPageBegin && segmentPageEnd > totalPage) segmentPageStart = lastPageBegin;


    var pageNoIdPrefix = queryId + "page";

    for (var i = 1; i <= totalPage; i++) {
        var pageNoElem = document.getElementById(pageNoIdPrefix + i);
        if (i == segmentPageStart) {
            pageNoElem.style.display = "inline-block";
            if (askedPageNo) {
                document.getElementById(pageNoIdPrefix + askedPageNo).click();
            } else pageNoElem.click();
        } else if (i > segmentPageStart && i <= segmentPageEnd) {
            pageNoElem.style.display = "inline-block";
        } else {
            pageNoElem.style.display = "none";
        }
    }

    var prevSegmentIdElem = document.getElementById(prevSegmentId);
    prevSegmentIdElem.style.display = (segmentPageStart > 1) ? "inline-block" : "none";
    var startIdElem = document.getElementById(startId);
    startIdElem.style.display = (segmentPageStart > 1) ? "inline-block" : "none";

    var nextSegmentIdElem = document.getElementById(nextSegmentId);
    nextSegmentIdElem.style.display = (segmentPageEnd >= totalPage) ? "none" : "inline-block";
    var endIdElem = document.getElementById(endId);
    endIdElem.style.display = (segmentPageEnd >= totalPage) ? "none" : "inline-block";

    segmentElem.innerHTML = segmentNo;
    //alert("segmentElem:" + segmentElem.innerHTML);
}


OnelineTable.prototype.onPageClick = function (queryId, pageNo, totalPage, callbackF) {

    var paginationIds = this.getPaginationIds(queryId);
    var startId = paginationIds[0], prevSegmentId = paginationIds[1], prevId = paginationIds[2], nextId = paginationIds[3];
    var nextSegmentId = paginationIds[4], endId = paginationIds[5], currentId = paginationIds[6], segmentId = paginationIds[7];

    var curPageElem = document.getElementById(currentId);
    var prevElem = document.getElementById(prevId);
    var nextElem = document.getElementById(nextId);

    if (pageNo == this.PAGENO_PREV) {
        pageNo = parseInt(curPageElem.innerHTML); //Means previous
        pageNo = pageNo - 1;
        var askedSegment = Math.ceil(pageNo / this.SEGMENT_SIZE);
        this.onSegmentClick(queryId, askedSegment, totalPage, pageNo);
        return;

    } else if (pageNo == this.PAGENO_NEXT) {
        pageNo = parseInt(curPageElem.innerHTML); //Means next
        pageNo = pageNo + 1;
        var askedSegment = Math.ceil(pageNo / this.SEGMENT_SIZE);
        this.onSegmentClick(queryId, askedSegment, totalPage, pageNo);
        return;

    } else if (pageNo == this.PAGENO_START) {

        this.onSegmentClick(queryId, 1, totalPage, 1);
        return;
    } else if (pageNo == this.PAGENO_END) {
        var totalSegments = Math.ceil(totalPage / this.SEGMENT_SIZE);
        this.onSegmentClick(queryId, totalSegments, totalPage, totalPage);
        return;
    }

    if (pageNo < 1) pageNo = 1;
    if (pageNo > totalPage) pageNo = totalPage;

    isNext = pageNo < totalPage;
    isPrevious = pageNo > 1;

    prevElem.style.display = (isPrevious) ? "inline-block" : "none";
    nextElem.style.display = (isNext) ? "inline-block" : "none";
    curPageElem.innerHTML = pageNo;

    for (var i = 1; i <= totalPage; i++) {
        var curPageId = queryId + "page" + i;
        if (i == pageNo) document.getElementById(curPageId).className = "pageNo pageNoSelected";
        else document.getElementById(curPageId).className = "pageNo";
    }
    callbackF(pageNo);
}


OnelineTable.prototype.getApplyFilterElem = function()
{
	return "<a href='#' onclick='" + this.name + ".applyFilter()' ><img class='table-filter' src='table-images/filter.png'></a> <a href='#' onclick='" + this.name + ".resetFilter()' ><img class='table-filter' src='table-images/filter-clear.png'></a>";
}

OnelineTable.prototype.resetFilter = function() {
	for (var fld in this.filterVals) {
		this.filterVals[fld] = "";
	}
	this.listTableOnFilter("");
}


OnelineTable.prototype.applyFilter = function () {
	var query = this.makeFilterQuery(this.filterVals, this.filterFldDataType);
	this.listTableOnFilter(query.trim());
}

OnelineTable.prototype.fetchResults = function (event) {
	if (event) {
		if (this.shouldApplyFilter(event))
			this.applyFilter();
	} else
		this.applyFilter();
}

OnelineTable.prototype.refreshList = function()
{
	this.offset = 0;
	this.applyFilter();
}



OnelineTable.prototype.getTextFilterElem = function (filterFld, filterVal, fillFilterValFunc, fetchResultFunc)
{
	return '<input class="filter-text-input" type="text" onchange=\'' + fillFilterValFunc + '("' +
				filterFld + '", this.value)\' value="' + filterVal +
				'" onkeydown=\'' + fetchResultFunc + '(window.event)\'/>';
}

OnelineTable.prototype.getSelectFilterElem = function (filterFld, filterValSelected, filterValCodes, filterValDescs, fillFilterValFunc, fetchResultFunc)
{
	var options = "";
	for( var i = 0; i<filterValCodes.length; i++)
	{
		if( filterValSelected == filterValCodes[i] )
			options = options + '<option value="' + filterValCodes[i] + '" selected="true">' + filterValDescs[i] + '</option>';
		else
			options = options + '<option value="' + filterValCodes[i] + '">' + filterValDescs[i] + '</option>';
	}
	return '<select class="filter-select-input" onchange=\'' + fillFilterValFunc + '("' +
				filterFld + '", this.value);' + fetchResultFunc + '();\'/>' + options + ' </select>';
}


OnelineTable.prototype.shouldApplyFilter = function (event) {
	var pressedKey = event.keyCode || event.charCode;
	if( pressedKey == 13) return true;
	return false;
}

OnelineTable.prototype.downloadLink = function () {
	return '<img id="downloadImg" name="downloadImg" class="table-filter" src="table-images/excel.png">';
}

OnelineTable.prototype.pushToIntFilter = function (filterFldDataType, flds)
{
	for ( var fld in flds)
	{
		filterFldDataType[ flds[fld] ]= 'i'; 
	}
}

OnelineTable.prototype.pushToDateFilter = function (filterFldDataType, flds)
{
	for ( var fld in flds)
	{
		filterFldDataType[ flds[fld] ]= 'd';
	}
	
}



////////////////////////////////////////////////////////
//////////////  RESIZABLE TABLE   //////////////////////
////////////////////////////////////////////////////////

var resizableTables = new Array();
function resizableColumns() {
    resizableTables.splice(0);
    var tables = document.getElementsByTagName('table');
    for (var i = 0; tables.item(i) ; i++) {
        if (tables[i].className.match(/resizable/)) {
            if (!tables[i].id) tables[i].id = 'table' + (i + 1);
            resizableTables[resizableTables.length] = new ColumnResize(tables[i]);
        }
    }
}

function preventEvent(e) {
    var ev = e || window.event;
    if (ev.preventDefault) ev.preventDefault();
    else ev.returnValue = false;
    if (ev.stopPropagation)
        ev.stopPropagation();
    return false;
}


function getWidth(x) {
    if (x.currentStyle)
        var y = x.clientWidth - parseInt(x.currentStyle["paddingLeft"]) - parseInt(x.currentStyle["paddingRight"]);
    else if (window.getComputedStyle)
        var y = document.defaultView.getComputedStyle(x, null).getPropertyValue("width");
    return y || 0;
}

// main class prototype
function ColumnResize(table) {
    if (table.tagName != 'TABLE') return;
    this.id = table.id;
    var self = this;
    var dragColumns = table.rows[0].cells; // first row columns, used for changing of width
    if (!dragColumns) return; // return if no table exists or no one row exists

    var dragColumnNo;
    var dragX;
    var saveOnmouseup;
    var saveOnmousemove;
    var saveBodyCursor;

    this.changeColumnWidth = function (no, w) {
        if (!dragColumns) return false;

        if (no < 0) return false;
        if (dragColumns.length < no) return false;

        if (parseInt(dragColumns[no].style.width) <= -w) return false;
        if (dragColumns[no + 1] && parseInt(dragColumns[no + 1].style.width) <= w) return false;

        dragColumns[no].style.width = parseInt(dragColumns[no].style.width) + w + 'px';
        if (dragColumns[no + 1])
            dragColumns[no + 1].style.width = parseInt(dragColumns[no + 1].style.width) - w + 'px';

        return true;
    }

    this.columnDrag = function (e) {
        var e = e || window.event;
        var X = e.clientX || e.pageX;
        if (!self.changeColumnWidth(dragColumnNo, X - dragX)) {
            self.stopColumnDrag(e);
        }

        dragX = X;
        preventEvent(e);
        return false;
    }

    this.stopColumnDrag = function (e) {
        var e = e || window.event;
        if (!dragColumns) return;

        document.onmouseup = saveOnmouseup;
        document.onmousemove = saveOnmousemove;
        document.body.style.cursor = saveBodyCursor;

        var colWidth = '';
        var separator = '';
        for (var i = 0; i < dragColumns.length; i++) {
            colWidth += separator + parseInt(getWidth(dragColumns[i]));
            separator = '+';
        }
        var expire = new Date();
        expire.setDate(expire.getDate() + 365); // year
        document.cookie = self.id + '-width=' + colWidth +
            '; expires=' + expire.toGMTString();

        preventEvent(e);
    }

    this.startColumnDrag = function (e) {
        var e = e || window.event;
        dragColumnNo = (e.target || e.srcElement).parentNode.parentNode.cellIndex;
        dragX = e.clientX || e.pageX;
        var colWidth = new Array();
        for (var i = 0; i < dragColumns.length; i++)
            colWidth[i] = parseInt(getWidth(dragColumns[i]));
        for (var i = 0; i < dragColumns.length; i++) {
            dragColumns[i].width = ""; // for sure
            dragColumns[i].style.width = colWidth[i] + "px";
        }

        saveOnmouseup = document.onmouseup;
        document.onmouseup = self.stopColumnDrag;

        saveBodyCursor = document.body.style.cursor;
        document.body.style.cursor = 'w-resize';

        // fire!
        saveOnmousemove = document.onmousemove;
        document.onmousemove = self.columnDrag;

        preventEvent(e);
    }

    for (var i = 0; i < dragColumns.length; i++) {
        dragColumns[i].innerHTML = "<div style='position:relative;height:100%;width:100%'>" +
            "<div style='" +
            "position:absolute;height:100%;width:5px;margin-right:-5px;" +
            "left:100%;top:0px;cursor:w-resize;z-index:10;'>" +
            "</div>" +
            dragColumns[i].innerHTML +
            "</div>";
        dragColumns[i].firstChild.firstChild.onmousedown = this.startColumnDrag;
    }
}


function getFirstKeyObj(data)
{
	var obj;
	for ( var e in data)
	{
		obj = data[e];
	}
	return obj;
}
