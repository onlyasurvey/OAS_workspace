<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="nameObject.pageTitle"/></title>
<h1><fmt:message key="nameObject.pageTitle"/></h1>
<p><fmt:message key="nameObject.introText" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="${subject.id}.html" method="post">
<fieldset class="twoColumnLayout">
<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
<div>
	<label for='map${supportedLanguage.iso3Lang}'><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /><span class="requiredIndicator"> *</span></label>
	<form:textarea id='map${supportedLanguage.iso3Lang}' path="map[${supportedLanguage.iso3Lang}]" cols="50" rows="2" />
</div>
</c:forEach>
<div>
<label>&nbsp;</label>
<input type='submit' class='button' value='<fmt:message key="save"/>' name="_save"/>
<input type='submit' class='button' value='<fmt:message key="cancel"/>' name="_cancel" />
</div>
</fieldset>
</form:form>
