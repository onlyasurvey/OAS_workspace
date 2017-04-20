<%@ include file="/WEB-INF/views/includes.jspf"%>

<table class="ratingsResults" width="100%" border="0">
	<tr class="tableHeading"> 
	<th scope="col">&nbsp;</th>
		<c:forEach items="${report.dateList}" var="date">
			<c:set var="dateIndex"><datetime:format date="${date}" pattern="yyyy-MM-dd" /></c:set>
			<th scope="col">${dateIndex}</th>
		</c:forEach>
		<th scope="col">Total</th>
	</tr> 
	
	<c:set var="grandTotal" value="0"/>
	<c:forEach items="${question.possibleValues}" var="choice">
	<c:set var="total" value="0"/>
	<tr>
		<th>${choice}</th>
		<c:forEach items="${report.dateList}" var="date">
			<c:set var="count" value="${report.scaleAnswerCounts[question][date][choice]}"/>
			<td>${count}</td>
			<c:set var="total" value="${total + count}"/>
		</c:forEach>
		<c:set var="grandTotal" value="${grandTotal + total}"/>
		<td>${total}</td>
	</tr>
	</c:forEach>
	
<%-- TEXT RESPONSES --%>
<c:if test="${question.allowOtherText}">
<tr> 
	<c:set var="total" value="0"/>
	<th><fmt:message key='question.other'/></th>
	<c:forEach items="${report.dateList}" var="date">
		<c:set var="dateParam"><datetime:format date="${date}" pattern="yyyy-MM" /></c:set>
		<c:set var="countThisDate" value="${report.textAnswerCounts[question][date]}"/>
		<c:choose>
		<c:when test="${empty(countThisDate) || countThisDate == 0}">
			<c:set var="countThisDate" value="0"/>
			<td>${countThisDate}</td>
		</c:when>
		<c:otherwise>
			<td><a href="<c:url value='/html/db/rpt/txt/${question.id}.html?d=${dateParam}' />"
			title="<spring:message code='monthlyBreakdown.view.title' arguments='${questionNumber},${dateParam},${countThisDate}' />"
				><fmt:message key='monthlyBreakdown.view'/> (${countThisDate})</a></td>
		</c:otherwise>
		</c:choose>
		<%-- TOTAL --%>
		<c:set var="total" value="${total + countThisDate}"/>
		<c:set var="grandTotal" value="${grandTotal + countThisDate}"/>
	</c:forEach> 
	<td>${total}</td> 
</tr>
</c:if>
<%-- VIEW CHART
<tr class="byWeekRow"> 
<td>&nbsp;</td>
<c:forEach items="${report.dateList}" var="date">
	<td>
		<a href="#" rel="popup standard 550 300"><fmt:message key='monthlyBreakdown.viewChart'/></a>
	</td>
</c:forEach> 
<td>&nbsp;</td> 
</tr>
--%>
<%-- BY WEEK
<tr class="byWeekRow"> 
<td>&nbsp;</td>
<c:forEach items="${report.dateList}" var="date">
	<td>
		<a href="#" rel="popup standard 550 300"><fmt:message key='monthlyBreakdown.byWeek'/></a>
	</td>
</c:forEach> 
<td>&nbsp;</td> 
</tr>
--%>
<%-- GRAND TOTAL --%>
<tr>
	<c:forEach items="${report.dateList}" var="date"><td></td></c:forEach>
	<td><fmt:message key='report.total'/></td>
	<td>${grandTotal}</td>
</tr>	

</table>
