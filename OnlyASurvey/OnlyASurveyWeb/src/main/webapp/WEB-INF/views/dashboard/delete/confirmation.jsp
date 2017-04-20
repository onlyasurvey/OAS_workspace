<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value='${subject.displayTitle}' /> - <fmt:message key="${pageTitleKey}" /></title>
<h1><fmt:message key="${pageTitleKey}" /></h1>
<h2><c:out value='${subject.displayTitle}' /></h2>
<p><fmt:message key="deleteItem.confirmationPrompt" /></p>
<form action="<c:url value='/html/db/mgt/rm/${subject.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
</div>
</form>