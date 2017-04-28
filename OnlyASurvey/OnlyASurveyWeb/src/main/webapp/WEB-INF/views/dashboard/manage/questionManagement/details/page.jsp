<%@ include file="/WEB-INF/views/includes.jspf"%>

<div> 
<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
<c:set var="languageCode"><c:out value="${supportedLanguage.iso3Lang}"/></c:set>
	<div>
		<label for='pageContent.map${languageCode}'><spring:message code='editQuestion.pageContentLabel' arguments="${languageTitle}" /></label>
		<form:textarea cssClass="wysiwygEditor" id="pageContent.map${languageCode}" path="pageContent.map[${languageCode}]" cols="60" rows="10"/>
	</div>
</c:forEach>
</div>

<div>
	<form:label path="showBack" for="SB">
		<fmt:message key='editQuestion.showBack' />
	</form:label>
	<form:checkbox path="showBack" id="SB" />
</div>
<div>
	<form:label path="showForward" for="SF">
		<fmt:message key='editQuestion.showForward' />
	</form:label>
	<form:checkbox path="showForward" id="SF" />
</div>

<%@ include file="/WEB-INF/views/wysiwygEditor.jsp"%>
