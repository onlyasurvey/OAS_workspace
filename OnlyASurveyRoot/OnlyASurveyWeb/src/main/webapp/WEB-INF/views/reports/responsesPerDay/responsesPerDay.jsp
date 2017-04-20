<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='report.responsesPerDay.title'/> - ${survey.displayTitle}</title>

<div class="yui-g">
<p><a href="<c:url value='/html/db/rpt/${survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>
<h1><fmt:message key='report.responsesPerDay.title'/> - ${survey.displayTitle}</h1>
<p><fmt:message key="report.responsesPerDay.introText" /></p>

<table class="responseResults" width="100%" border="0">
	<tr class="tableHeading"> 
		<th scope="col"><spring:message code='report.responsesPerDay.date'/></th>
		<th scope="col"><spring:message code='report.responsesPerDay.total'/></th>
	</tr> 
	
	<%-- RESPONSE COUNT --%>
	<c:set var="total" value="0"/>
	<c:forEach items="${data}" var="item">
	<c:set var="total" value="${total + item.count}"/>
	<tr> 
		<c:set var="dateParam"><datetime:format date="${item.id.date}" pattern="yyyy-MM-dd" /></c:set>
		<td>${dateParam}</td> 
		<td>${item.count}</td> 
	</tr>
	</c:forEach> 
	<tr>
	<td><spring:message code='report.responsesPerDay.total'/></td>
	<td>${total}</td>
	</tr>
</table>
</div>