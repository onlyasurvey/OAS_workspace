<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='viewText.pageTitle' /></title>
<p><a href="<oas:url value='/html/db/rpt/mbd/${survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>
<h1><fmt:message key='viewText.pageTitle' /></h1>
<table class="essayResults" width="100%" border="0">
	<c:forEach items="${report}" var="item">
		<tr>
			<th><datetime:format pattern="yyyy-MM-dd"
				date="${item.id.response.created}" /></th>
			<td><c:out value="${item.value}" /></td>
		</tr>
	</c:forEach>
</table>