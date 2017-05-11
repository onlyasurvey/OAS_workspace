<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${report.survey.displayTitle}"/> <fmt:message key='monthlyBreakdown.pageTitle'/></title>


<div class="yui-g">
<p><a href="<oas:url value='/html/db/rpt/${report.survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>
                  
<h1><c:out value="${report.survey.displayTitle}"/> - <fmt:message key='monthlyBreakdown.pageTitle'/></h1>
<p><fmt:message key="monthlyBreakdown.pageIntro" /></p>

<%-- FOR EACH QUESTION --%>
<c:forEach items="${report.survey.questions}" var="question" varStatus="iter">
	<a name="question_${question.id}"></a> 
	<h2><fmt:message key='question'/> ${iter.count}</h2>
	<p><c:out value="${question.displayTitle}"/></p> 
	<c:if test="${question.choiceQuestion}">
		<%@ include file="monthlyChoiceBreakdown.jsp" %>
	</c:if>	  
	<c:if test="${question.textQuestion}">
		<%@ include file="monthlyTextBreakdown.jsp" %>
	</c:if>	  
	<c:if test="${question.scaleQuestion}">
		<%@ include file="monthlyScaleBreakdown.jsp" %>
	</c:if>	  
</c:forEach>
</div>
