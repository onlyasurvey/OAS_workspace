<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="deleteItem.pageTitle.choice" />
</title>
<h1><fmt:message key="deleteItem.pageTitle.choice" /></h1>
<h2><c:out value='${choice.displayTitle}' /></h2>
<p><fmt:message key="deleteItem.confirmationPrompt" /></p>
<form action="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
	<input type='hidden' name='_d' value='' />
</div>
</form>