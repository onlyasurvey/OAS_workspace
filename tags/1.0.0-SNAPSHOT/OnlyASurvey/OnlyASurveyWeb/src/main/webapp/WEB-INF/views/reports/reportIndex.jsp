<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='reportIndex.title'/> - <c:out value="${survey.displayTitle}"/></title>

<p><a href="<oas:url value='/html/db/db.html'/>"><fmt:message key='reportIndex.backLink'/></a>
<c:if test='${!survey.published}'>
<br/><a href="<oas:url value='/html/db/mgt/${survey.id}.html'/>"><fmt:message key='reportIndex.backToEditSurvey'/></a>
</c:if>
</p>

<h1><c:out value="${survey.displayTitle}"/> - <fmt:message key='reportIndex.title'/></h1>
<p><fmt:message key="reportIndex.introText" /></p>
<h2><fmt:message key='reportIndex.responses' /></h2>
<img src="<oas:url value='/html/db/rpt/rpd/${survey.id}.png'/>" alt="<fmt:message key='reports.rpd.imageAlt'/>" title="<fmt:message key='reports.rpd.imageAlt'/>" />
<img src="<oas:url value='/html/db/rpt/rpl/${survey.id}.png'/>" alt="<fmt:message key='reports.rpl.imageAlt'/>" title="<fmt:message key='reports.rpl.imageAlt'/>" />

<jsp:include page="component/responseRate/monthlyVertical.jsp"/>



<div class="yui-g">
	<div class="yui-u first">
		<h2><fmt:message key="reportIndex.dailyReports" /></h2>
		<ul>
			<li><a href="<oas:url value='/html/db/rpt/dbd/${survey.id}.html'/>"><fmt:message key='reportIndex.report.responsesPerDay'/></a></li>
			<li><a href="<oas:url value='/html/db/ex/xls/dbd/${survey.id}.xls'/>"><fmt:message key='reportIndex.report.responsesPerDay'/>
				(<fmt:message key='reportIndex.download.xls'/>)</a></li>
		</ul>
	</div>
	<div class="yui-u">
		<h2><fmt:message key="reportIndex.monthlyReports" /></h2>
		<ul>
			<li><a href="<oas:url value='/html/db/rpt/mbd/${survey.id}.html'/>"><fmt:message key='reportIndex.report.responsesPerMonth'/></a></li>
			<li><a href="<oas:url value='/html/db/ex/xls/mbd/${survey.id}.xls'/>"><fmt:message key='reportIndex.report.responsesPerMonth'/>
				(<fmt:message key='reportIndex.download.xls'/>)
			</a></li>
		</ul>
	</div>
</div>
