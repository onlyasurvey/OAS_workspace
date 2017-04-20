<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${question.displayTitle}"/> - <fmt:message key="editChoices.editChoice.pageTitle"/></title>
<h1><fmt:message key="editChoices.editChoice.pageTitle"/></h1>
<p><fmt:message key="editChoices.editChoice.introText" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="${choice.id}.html" method="post">
<fieldset class="twoColumnLayout">
<c:forEach items="${question.survey.supportedLanguages}" var="supportedLanguage">
	<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
	<div>
		<label for='map${supportedLanguage.iso3Lang}'><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /><span class="requiredIndicator"> *</span></label>
		<form:textarea id='map${supportedLanguage.iso3Lang}' path="map[${supportedLanguage.iso3Lang}]" cols="50" rows="2" />
	</div>
</c:forEach>

<div>
	<label>&nbsp;</label>
	<input type='hidden' name='_e' value=''/>
	<input type='submit' class='button' value='<fmt:message key="save"/>' name="_save"/>
	<input type='submit' class='button' value='<fmt:message key="cancel"/>' name="_cancel"/>
</div>

</fieldset>
</form:form>
