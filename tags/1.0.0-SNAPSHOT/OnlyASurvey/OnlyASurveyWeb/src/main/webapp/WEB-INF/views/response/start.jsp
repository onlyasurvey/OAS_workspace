<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/></title>
<h1><c:out value="${survey.displayTitle}"/></h1>
<p><fmt:message key='response.requiredIndicatorMessage'/></p>
<p><c:out value="${welcomeMessage.displayTitle}"/></p>
<form action="<oas:url value="/html/res/q/${response.id}.html" />" method="get"><div>
	<input type='hidden' name='qId' value='${firstQuestion.id}'/>
	<input type='submit'
		value='<fmt:message key="survey.startNewResponseButton" />' />
</div></form>
