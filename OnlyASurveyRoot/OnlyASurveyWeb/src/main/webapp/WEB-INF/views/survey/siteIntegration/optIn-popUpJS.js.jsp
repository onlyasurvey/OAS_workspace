<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ page contentType="text/javascript"%>
// ==========================================================================
//
// Copyright (c) 2008-2010 Only A Survey, Inc.
//
// ==========================================================================

<c:set var="boxDivId">oasSurvey${survey.id}-optin</c:set>
<c:set var="shadowDivId">oasSurvey${survey.id}-shadow</c:set>


OAS = {
		
	COOKIE_NAME : "OASoptin${survey.id}",
	
	// ==========================================================================
	main : function() {
	
		var hasSeenPrompt = OAS.determineIfBrowserHasSeenPrompt();
		var hasResponded = OAS.determineIfBrowserHasResponded();
	
		// only continue if the browser does not have the cookie
		if( ! ( hasSeenPrompt || hasResponded)) {
	
			if( OAS.randomChance( ${percentChance} )) {
				OAS.showLightbox();
			} // else: random chance excluded this request
	
		} // else: excluded this request because the browser has seen prompt
			// or has responded
	}
	,
	// ==========================================================================
	showLightbox : function() {
		
		// dim out the main site
		document.body.innerHTML += OAS.shadowLayerContent;
		
		// add box content
		document.body.innerHTML += OAS.boxContent;
	}
	,
	// ==========================================================================
	hideLightbox : function() {
		document.body.removeChild(document.getElementById("${boxDivId}"));
		document.body.removeChild(document.getElementById("${shadowDivId}"));
	}
	,
	// ==========================================================================
	determineIfBrowserHasSeenPrompt : function() {
		var cookie = OAS.getCookie();
		return (cookie.length > 0);
	}
	,
	// ==========================================================================
	determineIfBrowserHasResponded : function() {
		return false;
	}
	,
	
	// ==========================================================================
	openSurveyWindow : function() {
		var url = "${surveyUrl}";
		var surveyWindow = window.open(url, "oasSurvey${survey.id}", "height=750,width=800,resizable=yes,scrollbars=yes");
	}
	,
	
	// ==========================================================================
	yesClicked : function() {
		OAS.hideLightbox();
		OAS.openSurveyWindow(); 
		OAS.setCookie();
	},
	
	// ==========================================================================
	noClicked : function() {
		OAS.hideLightbox();
		OAS.setCookie();
	},
	
	
	// ==========================================================================
	/**
	 * Return TRUE if a random chance check (1..100) returns a number under this
	 * percent.
	 */
	randomChance : function( percentChance ) {
		
		if(percentChance == 100) {
			return true;
		} else if(percentChance == 0) {
			return false;
		}
		
		var roll = Math.floor(Math.random() * 100)
		
		if(roll <= percentChance) {
			return true;
		}
		
		return false;
	},
	
	// ==========================================================================
	/**
	 * Adds functions to the onLoad event; included here to avoid conflicting
	 * with any customer code.
	 */
	addOnLoadEvent : function()
	{	
		var old = window.onload;
		if (typeof window.onload != 'function') {
			window.onload = OAS.main;
		} else {			
			window.onload = function() {
				old();
				OAS.main();
			}
		}
	},
	
	/**
	 * included here to avoid conflicting with any customer code.
	 */
	setCookie : function()
	{
		var expireDays = ${expireDays};
		var exdate = new Date();
		var value = "skipOptIn";
		
		exdate.setDate(exdate.getDate() + expireDays);
		document.cookie = OAS.COOKIE_NAME + "=" + escape(value) +
			((expireDays==null) ? "" : ";expires=" + exdate.toGMTString());
	},

	/**
	 * included here to avoid conflicting with any customer code.
	 */
	getCookie : function()
	{
		if (document.cookie.length > 0)
		{
			var c_start = document.cookie.indexOf(OAS.COOKIE_NAME + "=");
			if (c_start != -1)
			{
				c_start = c_start + OAS.COOKIE_NAME.length + 1;
				var c_end = document.cookie.indexOf(";", c_start);
				if (c_end == -1) {
					c_end = document.cookie.length;
				}
				return unescape(document.cookie.substring(c_start, c_end));
			}
		}
		return "";
	},
	
	/**
	 * ==========================================================================
	 * Lightbox shadow layer markup.
	 * ==========================================================================
	 */
	shadowLayerContent : "<div id='oasSurvey${survey.id}-shadow' "+
		"style='z-index: 98; display: block; background-color: #666666; opacity: 0.70; filter: alpha(opacity=70); "+
		"position: fixed; text-alignment: left; top: 0px; left: 0px; min-height: 100%; width: 100%; height: 100%;'"+
		">&nbsp;</div>"
	,
	
	/**
	 * ==========================================================================
	 * Lightbox body content.
	 * ==========================================================================
	 */
	boxContent : "<div id='${boxDivId}' class='surveyPopup' " +
		"style='"+
		"z-index: 99; background-color: white; position: absolute; top: 25%; left: 50%; " +
		"width: 33%; width: 460px; margin-left: -230px; height: 250px; "+
		"border: 2px solid #444; " +
		"'>" +
		"<h1><c:out value='${survey.displayTitle}'/></h1>" +
		"<p>Participate in our survey?</p>" +
		"<p>" +
		"<a href='#' class='OASlinkButton' onclick='OAS.yesClicked(); return false;'>Yes</a>" +
		"&nbsp;" +
		"<a href='#' class='OASlinkButton' onclick='OAS.noClicked(); return false;'>No</a>" +
		"&nbsp;" +
		"<a href='#' class='OASlinkButton' onclick='OAS.hideLightbox(); return false;'>Later</a>" +
		"</p>" +
		"</div>"
}

//
OAS.addOnLoadEvent();


// ==========================================================================

