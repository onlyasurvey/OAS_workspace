<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${report.survey.displayTitle}"/> <fmt:message key='dailyBreakdown.pageTitle'/></title>


<div class="yui-g">
<p><a href="<c:url value='/html/db/rpt/${report.survey.id}.html'/>"><fmt:message key='backToReports'/></a></p>
                  
<h1><c:out value="${report.survey.displayTitle}"/> - <fmt:message key='dailyBreakdown.pageTitle'/></h1>
<p><fmt:message key="dailyBreakdown.pageIntro" /></p>

<%-- FOR EACH QUESTION --%>
<c:forEach items="${report.survey.questions}" var="question" varStatus="iter">
<c:set var="questionNumber" value="${iter.count}"/>
	<a name="question_${question.id}"></a> 
	<h2><fmt:message key='question'/> ${questionNumber}</h2>
	<p><c:out value="${question.displayTitle}"/></p> 
	<c:if test="${question.choiceQuestion}">
		<%@ include file="dailyChoiceBreakdown.jsp" %>
	</c:if>	  

	<c:if test="${question.textQuestion}">
		<%@ include file="dailyTextBreakdown.jsp" %>
	</c:if>	  
	<c:if test="${question.scaleQuestion}">
		<%@ include file="dailyScaleBreakdown.jsp" %>
	</c:if>	 
</c:forEach>
</div>
