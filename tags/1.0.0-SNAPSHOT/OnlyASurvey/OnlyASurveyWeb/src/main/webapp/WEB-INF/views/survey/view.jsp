<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/></title>
<h1><c:out value="${survey.displayTitle}"/></h1>
<p><c:out value="${welcomeMessage.displayTitle}"/></p>
<form action="<oas:url value="/html/srvy/resp/${survey.id}.html" />" method="post"><div>
	<input type='submit' value='<fmt:message key="survey.startNewResponseButton" />' />
</div></form>
