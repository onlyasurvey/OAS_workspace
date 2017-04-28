<%@ include file="commonLayoutHeader.jspf"%>
<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ include file="config/includes.jspf"%>
<c:set var="rTo">/html<%=request.getPathInfo() %><% if(request.getQueryString() != null) { %>?${queryParams}<% } %></c:set>
<c:set var="rTo"><string:encodeUrl>${languageUrlRedirectSource}</string:encodeUrl></c:set>

<div id="bd"> 
	<div id="yui-main"> 
		<div class="yui-b"> 
			<decorator:body/> 
		</div> 
	</div>
	<sec:authorize ifAllGranted="ROLE_USER"> 
	<div class="yui-b">
		<div id="nav">
			<ul>
				<li class="activelink"><a href="<c:url value="/html/db/db.html"/>"><fmt:message key="dashboard"/></a></li>
				<li><a href="<c:url value="/html/db/crt.html"/>?rTo=${rTo}"><fmt:message key="newSurvey"/></a></li>
				<li><a href="<c:url value="/html/db/rpt.html"/>"><fmt:message key="viewReports"/></a></li>
			<%--	<li><a href="<c:url value="/html/db/upgrd.html"/>"><fmt:message key="upgradeAccount"/></a></li>--%>
			</ul>
		</div>

	</div> 
	</sec:authorize>
</div>
 
<%@ include file="commonLayoutFooter.jspf"%>