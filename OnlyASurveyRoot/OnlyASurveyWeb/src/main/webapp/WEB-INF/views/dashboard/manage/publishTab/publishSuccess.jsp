<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="publish.success.pageTitle"/></title>
<h1><fmt:message key="publish.success.pageTitle"/></h1>
<p><fmt:message key="publish.success.pageIntro" /></p>

<form action="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="get">
<div>
<input class='button' type='submit' value="<fmt:message key='publish.success.thanksButton'/>"/>
</div>
</form>