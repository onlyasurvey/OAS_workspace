<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:set var="defaultTitle"><fmt:message key="manageSurveyPage.changeTextPage"/></c:set>
<title><spring:message code="manageSurveyPage.changeTextPage.${command.key}" text="${defaultTitle}" /></title>
<h1><spring:message code="manageSurveyPage.changeTextPage.${command.key}" text="${defaultTitle}" /></h1>

<form:form action="${survey.id}.html?0=${command.key}" method="post" commandName="command">
<p><fmt:message key="manageSurveyPage.changeTextPage.introText" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<fieldset class="twoColumnLayout">
<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
<c:set var="languageCode"><c:out value="${supportedLanguage.iso3Lang}"/></c:set>
	<div>
		<label for='map${languageCode}'><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /><span class="requiredIndicator"> *</span></label>
		<form:textarea id="map${languageCode}" path="map[${languageCode}]" cols="60" rows="10" /> 
	</div>
</c:forEach>
<div class="">
	<label>&nbsp;</label>
	<input type='submit' class='button' value='<fmt:message key="save"/>'/>
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="cancel"/>'/>
	<input type='hidden' name='0' value="${command.key}"/>
	<input type='hidden' name='rTo' value="${param.rTo}"/>
</div>
</fieldset>
</form:form>