<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <spring:message code='siteIntegrationEditor.pageTitle' /></title>
<p><a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>"><fmt:message key='backLink.publishTab'/></a></p>

<h2><spring:message code='siteIntegrationEditor.pageTitle' /></h2>
<p><spring:message code='siteIntegrationEditor.pageIntro' /></p>


<h3><spring:message code='siteIntegrationEditor.lightbox.likelihood.header' /></h3>
<p><spring:message code='siteIntegrationEditor.lightbox.entropyNotice' /></p>

<form action="<c:url value='/html/db/mgt/pb/wsi/optin/${survey.id}.html'/>" method='post'>
<div>
	<label for='percent'><spring:message code='siteIntegrationEditor.lightbox.likelihood.input' /></label>
	<input type='text' id='percent' name='percent' value='${survey.optinPercentage}' size='5' maxlength="3"/>
	<spring:message code='siteIntegrationEditor.lightbox.likelihood.input.note' />
</div>		

<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="save"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="cancel"/>' />
</div>

</form>


