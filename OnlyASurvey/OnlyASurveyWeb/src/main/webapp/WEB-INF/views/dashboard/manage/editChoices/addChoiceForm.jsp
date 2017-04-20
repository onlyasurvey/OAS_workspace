<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${question.displayTitle}"/> - <fmt:message key="editChoices.addChoice.pageTitle"/></title>
<h1><c:out value="${question.displayTitle}"/></h1>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<h2><fmt:message key="editChoices.addChoice.pageTitle"/></h2>
<p><fmt:message key="editChoices.addChoice.introText" /></p>


<form:form action="${question.id}.html" method="post">
<fieldset class="twoColumnLayout">
<c:forEach items="${question.survey.supportedLanguages}" var="supportedLanguage">
	<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
	<div>
		<label for='map${supportedLanguage.iso3Lang}'><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /> *</label>
		<form:textarea id='map${supportedLanguage.iso3Lang}' path="map[${supportedLanguage.iso3Lang}]" cols="50" rows="2" />
	</div>
</c:forEach>
</fieldset>

<div class='bottomButtonBar'>
	<input type='submit' class='button' value='<fmt:message key="save"/>' name="_save"/>
	<input type='submit' class='button' value='<fmt:message key="cancel"/>' name="_cancel"/>
	<input type='hidden' name='_a' value=''/>
</div>
</form:form>
