<%@ include file="/WEB-INF/views/includes.jspf" %>
<title><c:out value="${response.survey.displayTitle}"/></title>
<h1>
	<c:out value="${response.survey.displayTitle}"/>
</h1>
<c:if test="${not empty(additionalMessage)}">
<div class="successMessage"><spring:message code="${additionalMessage}"/></div><br/>
</c:if>
<div><oas:html value="${thanksMessage.displayTitle}"/></div>
