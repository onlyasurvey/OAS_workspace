<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='reportIndex.title'/> - ${survey.displayTitle}</title>
<h1><fmt:message key='reportIndex.title'/> - ${survey.displayTitle}</h1>
<p><fmt:message key="reportIndex.introText" /></p>
<!--  Row 2 -->
<div class="yui-g">
	<div class="yui-u first">
		<h2><fmt:message key="reportIndex.builtInReports" /></h2>
		<ul>
			<li><a href="<oas:url value='/html/db/rpt/sum/${survey.id}.html'/>"><fmt:message key='reportIndex.report.summary'/></a></li>
			<li><a href="<oas:url value='/html/db/rpt/rpd/${survey.id}.html'/>"><fmt:message key='reportIndex.report.responsesPerDay'/></a></li>
			<li><a href="<oas:url value='/html/db/rpt/q/${survey.id}.html'/>"><fmt:message key='reportIndex.report.questionDetails'/></a></li>
		</ul>
	</div>
	<div class="yui-u">
		<h2><fmt:message key="reportIndex.dataDownload" /></h2>
		<ul>
			<li><a href="<oas:url value='/html/db/rpt/csv/${survey.id}.html'/>"><fmt:message key='reportIndex.download.csv'/></a></li>
			<li><a href="<oas:url value='/html/db/rpt/xls/${survey.id}.html'/>"><fmt:message key='reportIndex.download.xls'/></a></li>
			<li><a href="<oas:url value='/html/db/rpt/xml/${survey.id}.html'/>"><fmt:message key='reportIndex.download.xml'/></a></li>
		</ul>
	</div>
</div>