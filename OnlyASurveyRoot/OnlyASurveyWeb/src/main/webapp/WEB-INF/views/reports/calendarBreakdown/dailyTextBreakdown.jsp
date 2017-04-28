<%@ include file="/WEB-INF/views/includes.jspf"%>

<table class="essayResults" width="100%" border="0">
	<tr class="tableHeading"> 
	<th scope="col">&nbsp;</th>
		<c:forEach items="${report.dateList}" var="date">
			<c:set var="dateIndex"><datetime:format date="${date}" pattern="yyyy-MM-dd" /></c:set>
			<th scope="col">${dateIndex}</th>
		</c:forEach>
		<th scope="col">Total</th>
	</tr> 
	
	<c:set var="grandTotal" value="0"/>
		
	<%-- RESPONSE COUNT --%>
	<tr> 
		<c:set var="total" value="0"/>
		<th><fmt:message key='monthlyBreakdown.responses'/></th>
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
				<c:if test="${countThisDate > 0}"><a href="<c:url value='/html/db/rpt/txt/${question.id}.html?d=${dateParam}' />"
				title="<spring:message code='monthlyBreakdown.view.title' arguments='${questionNumber},${dateParam},${countThisDate}'/>"
				rel="popup standard 550 300"><fmt:message key='monthlyBreakdown.view'/> (${countThisDate})</a></c:if>
				</td>
			</c:otherwise>
			</c:choose>
			<%-- TOTAL --%>
			<c:set var="total" value="${total + countThisDate}"/>
		</c:forEach> 
		<td>${total}</td> 
	</tr>
	 
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



</table>
