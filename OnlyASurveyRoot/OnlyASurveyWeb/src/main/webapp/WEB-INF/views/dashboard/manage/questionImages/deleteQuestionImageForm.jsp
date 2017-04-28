<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="deleteQuestionImage.pageTitle" />
</title>
<h1><fmt:message key="deleteQuestionImage.pageTitle" /></h1>
<h2><c:out value='${question.displayTitle}' /></h2>
<p><fmt:message key="deleteQuestionImage.pageIntro" /></p>
<form action="<c:url value='/html/db/mgt/qatt/rm/${question.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
	<input type='hidden' name='l' value='${param.l}' />
</div>
</form>