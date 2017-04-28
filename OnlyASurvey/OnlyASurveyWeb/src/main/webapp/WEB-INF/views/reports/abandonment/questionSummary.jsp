<%@ include file="/WEB-INF/views/includes.jspf"%>

<%@page import="com.oas.controller.dashboard.report.AbandonmentReportController"%>
<%@page import="com.oas.controller.dashboard.report.graph.CompletionRatioPieImageController"%><title><fmt:message key='report.abandonment.questionSummary.title'/> - <c:out value="${survey.displayTitle}"/></title>

<p><a href="<c:url value='/html/db/rpt/${survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>

<h1><c:out value="${survey.displayTitle}"/> - <fmt:message key='report.abandonment.questionSummary.title'/></h1>

<div class="yui-g">
	<div class="yui-u first">
		<fmt:message key="report.abandonment.questionSummary.introText" />
	</div>
	<div class="yui-u">
		<img
			width="<%=CompletionRatioPieImageController.COMPLETION_RATIO_GRAPH_WIDTH %>"
			height="<%=CompletionRatioPieImageController.COMPLETION_RATIO_GRAPH_HEIGHT %>"
			src="<c:url value='/html/db/rpt/abnd/cp_rto/${survey.id}.png'/>"
			alt="<fmt:message key="report.abandonment.rateGraph.title" />"
			title="<fmt:message key="report.abandonment.rateGraph.title" />"
			/>
	</div>
</div>

<h2><fmt:message key='report.abandonment.questionSummary.byQuestion' /></h2>

<c:set var="total" value="0"/>
<c:forEach items="${data}" var="item">
	<c:set var="total" value="${total + item.count}"/>
</c:forEach>

<table  class="ratingsResults">
<thead>
<tr class="tableHeading">
	<th><fmt:message key='reports.question'/></th>
	<th><fmt:message key='reports.title'/></th>
	<th><fmt:message key='reports.count'/></th>
	<th><fmt:message key='reports.percentage'/></th>
</tr>
</thead>
<c:forEach items="${data}" var="item">
<tr>
	<c:set var="question" value="${survey.questions[item.id.questionIndex]}"/>
	<td># ${item.id.questionIndex+1}</td>
	<td><oas:html value="${question.displayTitle}"/></td>
	<td>${item.count}</td>
	<td><fmt:formatNumber minFractionDigits="2" maxFractionDigits="2"  value="${item.count / total * 100}"/>%</td>
</tr>
</c:forEach>
</table>


