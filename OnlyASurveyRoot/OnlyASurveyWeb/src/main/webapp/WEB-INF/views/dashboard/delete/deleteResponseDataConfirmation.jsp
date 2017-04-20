<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value='${subject.displayTitle}' /> - <fmt:message key="deleteResponseData.pageTitle" /></title>
<h1><fmt:message key="deleteResponseData.pageTitle" /></h1>
<h2><c:out value='${subject.displayTitle}' /></h2>
<p><fmt:message key="deleteResponseData.confirmationPrompt" /></p>
<form action="<c:url value='/html/db/mgt/rm/rsp/${subject.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
	<input type='hidden' name='rTo' value="<c:out value='${param.rTo}'/>"/>
</div>
</form>