<%@ include file="/WEB-INF/views/includes.jspf"%>

<table class="ratingsResults" width="100%" border="0">
	<tr class="tableHeading"> 
	<th scope="col">&nbsp;</th>
		<c:forEach items="${report.dateList}" var="date">
			<c:set var="dateIndex"><datetime:format date="${date}" pattern="MM-dd-yyyy" /></c:set>
			<th scope="col">${dateIndex}</th>
		</c:forEach>
		<th scope="col">Total</th>
	</tr> 
	
	<c:set var="grandTotal" value="0"/>
	
	<c:forEach items="${question.choices}" var="choice">
	<c:set var="total" value="0"/>
	<tr>
		<th><c:out value="${choice.displayTitle}"/></th>
		<c:forEach items="${report.dateList}" var="date">
			<c:set var="breakdown" value="${report.choiceAnswers[choice][date]}"/>
			<c:choose>
				<c:when test="${empty(breakdown)}">
				<c:set var="count" value="0"/>
				</c:when>
				<c:otherwise><c:set var="count" value="${report.choiceAnswers[choice][date].count}"/></c:otherwise>
			</c:choose>		
			<td	>${count}</td>
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
		<c:set var="dateParam"><datetime:format date="${date}" pattern="yyyy-MM-dd" /></c:set>
		<c:set var="countThisDate" value="${report.textAnswerCounts[question][date]}"/>
		<c:choose>
		<c:when test="${empty(countThisDate)}">
			<c:set var="countThisDate" value="0"/>
			<td>${countThisDate}</td>
		</c:when>
		<c:otherwise>
				<td>
				<c:if test="${countThisDate == 0}">0</c:if>
				<c:if test="${countThisDate > 0}"><a href="<oas:url value='/html/db/rpt/txt/${question.id}.html?d=${dateParam}' />" rel="popup standard 550 300"><fmt:message key='dailyBreakdown.view'/> (${countThisDate})</a></c:if>
				</td>
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
		<a href="#" rel="popup standard 550 300"><fmt:message key='dailyBreakdown.viewChart'/></a>
	</td>
</c:forEach> 
<td>&nbsp;</td> 
</tr>
 --%>
<tr>
	<c:forEach items="${report.dateList}" var="date"><td></td></c:forEach>
	<td><fmt:message key='report.total'/></td>
	<td>${grandTotal}</td>
</tr>
</table>
