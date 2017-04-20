<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@page import="com.oas.controller.enterprise.images.QuickStatsImageController"%>

<h2><fmt:message key="enterprise.quickStats.header"/></h2>
<p><fmt:message key="enterprise.quickStats.intro"/></p>

<div>
<img src="<c:url value='/html/ent/db/img/qs.png'/>" 
height="<%=QuickStatsImageController.QUICK_STATS_IMAGE_HEIGHT %>"
width="<%=QuickStatsImageController.QUICK_STATS_IMAGE_WIDTH %>"
alt="<fmt:message key="enterprise.quickStats.header"/>"/>
</div>

<table class="manageSurveysTable">
<thead>
<tr>
	<th><fmt:message key="enterprise.quickStats.timePeriod"/></th>
	<th><fmt:message key="enterprise.quickStats.closed"/></th>
	<th><fmt:message key="enterprise.quickStats.partial"/></th>
	<th><fmt:message key="enterprise.quickStats.deleted"/></th>
	<th><fmt:message key="enterprise.quickStats.total"/></th>
</tr>
</thead>
<tbody>
	<tr>
		<td><fmt:message key="enterprise.quickStats.today"/></td>
		<td>${enterpriseQuickStats.closed.today}</td>
		<td>${enterpriseQuickStats.partial.today}</td>
		<td>${enterpriseQuickStats.deleted.today}</td>
		<td>${enterpriseQuickStats.total.today}</td>
	</tr>
	<tr>
		<td><fmt:message key="enterprise.quickStats.sevenDays"/></td>
		<td>${enterpriseQuickStats.closed.lastWeek}</td>
		<td>${enterpriseQuickStats.partial.lastWeek}</td>
		<td>${enterpriseQuickStats.deleted.lastWeek}</td>
		<td>${enterpriseQuickStats.total.lastWeek}</td>
	</tr>
	<tr>
		<td><fmt:message key="enterprise.quickStats.thisMonth"/></td>
		<td>${enterpriseQuickStats.closed.thisMonth}</td>
		<td>${enterpriseQuickStats.partial.thisMonth}</td>
		<td>${enterpriseQuickStats.deleted.thisMonth}</td>
		<td>${enterpriseQuickStats.total.thisMonth}</td>
	</tr>
	<tr>
		<td><fmt:message key="enterprise.quickStats.lastQuarter"/></td>
		<td>${enterpriseQuickStats.closed.lastQuarter}</td>
		<td>${enterpriseQuickStats.partial.lastQuarter}</td>
		<td>${enterpriseQuickStats.deleted.lastQuarter}</td>
		<td>${enterpriseQuickStats.total.lastQuarter}</td>
	</tr>
	<tr>
		<td><fmt:message key="enterprise.quickStats.total"/></td>
		<td>${enterpriseQuickStats.closed.total}</td>
		<td>${enterpriseQuickStats.partial.total}</td>
		<td>${enterpriseQuickStats.deleted.total}</td>
		<td>${enterpriseQuickStats.total.total}</td>
	</tr>
</tbody>
</table>
