<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="publish.error.pageTitle"/></title>
<h1><fmt:message key="publish.error.pageTitle"/></h1>
<p><fmt:message key="publish.error.pageIntro" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form action="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="get">
<div class='buttonBar'>
	<input type='submit' class='button' value='<fmt:message key="back"/>'/>
</div>
</form>