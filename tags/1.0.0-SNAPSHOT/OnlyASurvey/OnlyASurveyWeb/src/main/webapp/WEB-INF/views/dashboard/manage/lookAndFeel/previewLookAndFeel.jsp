<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <fmt:message key='previewLookAndFeel.pageTitle' /></title>
<h1><fmt:message key='previewLookAndFeel.pageTitle' /></h1>
<p><fmt:message key='previewLookAndFeel.pageIntro'/></p>

<form action="<oas:url value='/html/db/mgt/lnf/${survey.id}.html'/>" method="get">
<div class='bottomButtonBar'>
	<input class='button' type='submit' value="<fmt:message key='finished'/>" />
</div>
</form>