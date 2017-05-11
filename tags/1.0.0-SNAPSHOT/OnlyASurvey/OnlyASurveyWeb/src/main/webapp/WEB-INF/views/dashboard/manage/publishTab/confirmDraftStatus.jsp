<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="publishTab.confirmDraftStatus.pageTitle" /></title>
<h1><fmt:message key='publishTab.confirmDraftStatus.pageTitle' /></h1>
<p><fmt:message key="publishTab.confirmDraftStatus.pageIntro" /></p>
<p><fmt:message key="publishTab.confirmDraftStatus.confirm" /></p>
<form action="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
	<input type='hidden' name='_dr'  /><%-- flags as request for Draft Status --%>
</div>
</form>