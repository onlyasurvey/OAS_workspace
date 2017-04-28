<!DOCTYPE 
    html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/views/includes.jspf"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<%@ include file="config/includes.jspf"%>
	<head>
		<title><c:set var="defaultTitle">
				<fmt:message key='jsp.default.decoratorTitle' />
			</c:set> <decorator:title default="${defaultTitle}" /> - <fmt:message
				key="jsp.default.title" /></title>
		<link rel="stylesheet"
			href="<c:url value="/incl/css/reset-fonts-grids.css"/>"
			type="text/css" />
		<link rel="stylesheet" href="<c:url value="/incl/css/styles.css"/>"
			type="text/css" />
		<link rel="stylesheet" href="<c:url value="/incl/css/utility.css"/>"
			type="text/css" />
		<decorator:head />
<c:set var="doExternalJS">
<%=request.getRequestURL().indexOf("www") != -1 ? true : "" %>
</c:set>
	
	<c:if test="${not empty(doExternalJS)}">
	<script type="text/javascript">
	var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
	document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
	</script>
	<script type="text/javascript">
	var pageTracker = _gat._getTracker("UA-5613908-1");
	pageTracker._trackPageview();
	</script>
	</c:if>
	
	</head>
<body> 
<div id="doc" class="yui-t7"> 
<div id="hd"> 
	<div class="yui-gc"> 
		<div class="yui-u first"> 
			<div id="logo">
			<c:choose>
				<c:when test="${leftLogoExists}">
				<c:set var="leftAlt"><c:out value="${leftLogoAlt}"/></c:set>
				<img src="<c:url value='/html/srvy/lg/${survey.id}.${leftLogoExtension}?p=l&amp;l=${templateLanguage}&amp;dt=${leftLogoDate}'/>" alt="${leftAlt}" title="${leftAlt}" />
				</c:when>
				<c:otherwise><c:out value="${survey.owner.organization}"/></c:otherwise>
			</c:choose>
			</div>
		</div>
		<div class="yui-u"> 
			<div id="logoRight">
				<c:choose>
				<c:when test="${rightLogoExists}">
				<c:set var="rightAlt"><c:out value="${rightLogoAlt}"/></c:set>
				<img src="<c:url value='/html/srvy/lg/${survey.id}.${rightLogoExtension}?p=r&amp;l=${templateLanguage}'/>&amp;dt=${rightLogoDate}" alt="${rightAlt}" title="${rightAlt}" />
				</c:when>
				</c:choose>
			</div>
		</div>
	</div>
	<div class="yui-g">
		<div id="changeLang">
		<c:if test="${fn:length(survey.supportedLanguages) > 1}">
		<c:forEach items="${survey.supportedLanguages}" var="language">
			<c:set var="queryParams"><%=request.getQueryString() %></c:set>
			<c:set var="languageUrlRedirectSource">/html<%=request.getPathInfo() %><% if(request.getQueryString() != null) { %>?${queryParams}<% } %></c:set>
			<c:set var="languageUrlRedirect"><string:encodeUrl>${languageUrlRedirectSource}</string:encodeUrl></c:set>
			<a href="<c:url value='/html/${language.iso3Lang}.html?url=${languageUrlRedirect}'/>"><c:out value="${language.displayTitle}"/></a>
		</c:forEach>
		</c:if>
		</div> 
	</div>
	<div class="headerBottomBar">&nbsp;</div> 
</div> 
<div id="bd">
	<div id="yui-main">
		<div class="yui-b">
			<div class="yui-g"><div class="OAS_survey"><decorator:body/></div></div>
		</div>
	</div>
	<div id="ft">
    	<div id="copyRightNotice"><fmt:message key='response.copyrightLine'/></div>
	</div>
</div>
</div>
</body> 
<!-- InstanceEnd --></html> 