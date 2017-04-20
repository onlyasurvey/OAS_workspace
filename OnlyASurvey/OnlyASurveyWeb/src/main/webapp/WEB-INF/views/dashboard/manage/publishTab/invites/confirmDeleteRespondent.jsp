<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value='${subject.emailAddress}' /> - <fmt:message key="deleteRespondent.pageTitle" /></title>
<h1><fmt:message key="deleteRespondent.pageTitle" /></h1>
<h2><c:out value='${subject.emailAddress}' /></h2>
<p><fmt:message key="deleteRespondent.confirmMsg" /></p>
<form action="<c:url value='/html/db/mgt/pb/inv/dl/${subject.id}/${survey.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
</div>
</form>