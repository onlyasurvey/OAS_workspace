<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code='response.passwordPrompt.pageTitle'/></title>
<h1><spring:message code='response.passwordPrompt.pageTitle'/></h1>
<><spring:message code='response.passwordPrompt.pageIntro'/></p>

<c:if test="${not empty(errors)}">
<div class='errorMessage'>
<spring:message code='response.passwordPrompt.error'/>
</div>
</c:if>

<form action="${response.id}" method="post">
<fieldset class="twoColumnLayout">
	<div>
		<label for='spw'><spring:message code="response.passwordPrompt.label" /></label>
		<input name="spw" id="spw" type="password" size="15" maxlength="100" />
	</div>

	<div>
		<label>&nbsp;</label>
		<input type='submit' class='button' value='<spring:message code="continue"/>'/>
		<c:if test="${not empty(skipWelcomeFlag)}"><input type='hidden' name='sw' value='t'/></c:if>
	</div>
</fieldset>
</form>