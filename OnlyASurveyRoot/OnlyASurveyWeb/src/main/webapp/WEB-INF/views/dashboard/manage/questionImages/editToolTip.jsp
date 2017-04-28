<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code="editQuestionImageTooltip.pageTitle" arguments="${languageName}"/></title>
<h1><spring:message code="editQuestionImageTooltip.pageTitle" arguments="${languageName}"/></h1>
<p><spring:message code="editQuestionImageTooltip.pageIntro" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="${question.id}.html" method="post">
<fieldset class="twoColumnLayout">
<div>
	<label for='map${param.l}'><spring:message code='editQuestionImageTooltip.label' arguments="${languageTitle}" /><span class="requiredIndicator"> *</span></label>
	<form:textarea id='map${param.l}' path="map[${param.l}]" cols="50" rows="2" />
</div>
<div>
<label>&nbsp;</label>
<input type='submit' class='button' value='<spring:message code="save"/>' name="_save"/>
<input type='submit' class='button' value='<spring:message code="cancel"/>' name="_cancel" />
<input type='hidden' name='l' value='${param.l}' />
</div>
</fieldset>
</form:form>
