<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code="surveyLogos.toolTip.pageTitle" arguments="${languageName}"/></title>
<h1><spring:message code="surveyLogos.toolTip.pageTitle" arguments="${languageName}"/></h1>
<p><spring:message code="surveyLogos.toolTip.pageIntro" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="${survey.id}.html" method="post">
<fieldset class="twoColumnLayout">
<div>
	<label for='map${languageCode}'><spring:message code='surveyLogos.toolTip.label' arguments="${languageTitle}" /><span class="requiredIndicator"> *</span></label>
	<form:textarea id='map${languageCode}' path="map[${languageCode}]" cols="50" rows="2" />
</div>
<div>
<label>&nbsp;</label>
<input type='submit' class='button' value='<spring:message code="save"/>' name="_save"/>
<input type='submit' class='button' value='<spring:message code="cancel"/>' name="_cancel" />
</div>
</fieldset>
</form:form>
