<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${response.survey.displayTitle}"/></title>
<c:set var="backUrl"><c:url value='/html/res/q/${response.id}.html?n=${param.n}'/></c:set>

	<a href="${backUrl}"><spring:message code='response.saveAndReturnLater.backLink'/></a>
	
	<h1><spring:message code='response.saveAndReturnLater.pageTitle'/></h1>

	<%-- when a status or error message is displayed then this flag is set --%>	
	<c:choose>
	<c:when test="${not empty(statusMessage)}">
	<div class='successMessage'><spring:message code="${statusMessage}"/></div>
	</c:when>
	<c:when test="${not empty(errorMessage)}">
	<div class='errorMessage'><spring:message code="${errorMessage}"/></div>
	</c:when>
	</c:choose>
	
	<h2><spring:message code='response.saveAndReturnLater.header1'/></h2>
	<p><spring:message code='response.saveAndReturnLater.method1'/></p>
	
	<h2><spring:message code='response.saveAndReturnLater.header2'/></h2>
	<p><spring:message code='response.saveAndReturnLater.method2'/></p>
	<form action="${response.id}.html?n=${param.n}" method="post"> 
	<fieldset class="twoColumnLayout">
	<input type='hidden' name='_knildnes' value=''/>
	<div>
		<label class="width10" for="email"><spring:message code='response.saveAndReturnLater.emailLabel'/></label> 
		<input name="email" id="email" type="text" size="40" maxlength="255" value="<c:out value='${email}'/>"/> 
	</div> 
	<div> 
		<label>&nbsp;</label> 
		<input class="button" type="submit" value="<spring:message code='response.saveAndReturnLater.sendButton'/>"/> 
	</div> 
	</fieldset> 
	</form> 
	<div class="bottomButtonBarLined"><a class="linkButton" href="${backUrl}"><spring:message code='back'/></a></div> 
