<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code="surveyLogos.delete.pageTitle" /></title>
<h1><spring:message code="surveyLogos.delete.pageTitle" /></h1>
<p><spring:message code="surveyLogos.delete.pageIntro" /></p>
<form action="${survey.id}.html" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<spring:message code="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<spring:message code="no"/>' />
</div>
</form>