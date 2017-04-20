<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="deleteItem.pageTitle" /></title>
<h1><fmt:message key="deleteItem.pageTitle" /></h1>
<h2><fmt:message key="warning"/></h2>
<p>
	<fmt:message key="deleteItem.error.surveyHasResponses" />
</p>

<form action="<c:url value='/html/db/db.html'/>" method="get">
<div class='bottomButtonBar'>
	<input type='hidden' name='rTo' value="<c:url value='/html/db/mgt/rm/${survey.id}.html'/>" />
	<input type='submit' class='button' value="<fmt:message key='cancel'/>" name="_cancel" />
</div>
</form>
