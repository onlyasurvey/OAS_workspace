<%@ include file="/WEB-INF/views/includes.jspf"%>

<%@page import="com.oas.controller.dashboard.report.graph.TimeToCompleteChartController"%>

<title><fmt:message key='report.timeTaken.title'/> - <c:out value="${survey.displayTitle}"/></title>

<p><a href="<c:url value='/html/db/rpt/${survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>

<h1><c:out value="${survey.displayTitle}"/> - <fmt:message key='report.timeTaken.title'/></h1>

<p><spring:message code='report.timeTaken.introText' arguments='<%=TimeToCompleteChartController.FILTER_OUT_PAST_TIME_TAKEN %>'/></p>

<div>
<img
	width="<%=TimeToCompleteChartController.GRAPH_WIDTH %>"
	height="<%=TimeToCompleteChartController.GRAPH_HEIGHT %>"
	src="<c:url value='/html/db/rpt/g/ttc/${survey.id}.png'/>"
	alt="<spring:message code='report.timeTaken.graph.title'/>'"
	title="<spring:message code='report.timeTaken.graph.title'/>"
	/>
</div>

<c:set var="totalRespondents" value="0"/>
<c:forEach items="${data}" var="item">
	<c:set var="totalRespondents" value="${totalRespondents + item.count}"/>
</c:forEach>

<table class="ratingsResults">
<thead>
<tr class="tableHeading">
	<th><fmt:message key='reports.timeTaken.withMinutes'/></th>
	<th><fmt:message key='reports.count'/></th>
	<th><fmt:message key='reports.percentage'/></th>
</tr>
</thead>
<c:forEach items="${data}" var="item">
<tr>
	<td>${item.id.minutes}</td>
	<td>${item.count}</td>
	<td><fmt:formatNumber minFractionDigits="2" maxFractionDigits="2"  value="${item.count / totalRespondents * 100}"/>%</td>
</tr>
</c:forEach>
</table>


