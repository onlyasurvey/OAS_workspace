<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/></title>
<%--

	NEEDS TO BE IN SYNC WITH /response/start.jsp EXCEPT FORM ACTION/PARAM

--%>
<h1><c:out value="${survey.displayTitle}"/></h1>
<p><oas:html value="${welcomeMessage.displayTitle}"/></p>
<form action="<c:url value="/html/srvy/resp/${survey.id}.html" />" method="post">
<div>
	<input type='hidden' name='sw' />
	<input type='hidden' name='n' value='1' />
	<input type='submit' value='<fmt:message key="survey.startNewResponseButton" />' />
</div>
</form>
