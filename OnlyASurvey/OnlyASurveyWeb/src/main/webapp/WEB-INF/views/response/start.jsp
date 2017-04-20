<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/></title>
<%--

	NEEDS TO BE IN SYNC WITH /survey/view.jsp EXCEPT FORM ACTION/PARAM

--%>
<h1><c:out value="${survey.displayTitle}"/></h1>
<div><oas:html value="${welcomeMessage.displayTitle}"/></div>
<form action="<c:url value="/html/res/q/${response.id}.html" />" method="get"><div>
<%--	<input type='hidden' name='qId' value='${firstQuestion.id}'/>--%>
	<input type='hidden' name='n' value='1' />
	<input type='submit'
		value='<fmt:message key="survey.startNewResponseButton" />' />
</div></form>
