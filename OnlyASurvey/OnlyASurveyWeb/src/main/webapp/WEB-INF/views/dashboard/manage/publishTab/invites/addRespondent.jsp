<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='invitesPage.pageTitle' /></title>

<div class="yui-g">
<h1><fmt:message key="addRespondents.pageTitle" /> </h1>
<p><spring:message code="addRespondents.introText" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form id="command" action="${survey.id}.html" method="post">
<fieldset class="twoColumnLayout">
<h2><spring:message code="addRespondents.sectionTitle" /></h2>
<div>
	<label for="userEmailData"><fmt:message key="addRespondents.emailField" /></label>
	<textarea name="userEmailData" cols="75" rows="15" id="userEmailData"></textarea>
</div>
</fieldset>   
<div class="bottomButtonBar">
	<input  value="Save" name="_save"  type="submit" class="button"  />
	<input type='submit' class='button' value="<fmt:message key='cancel'/>" name="_cancel" />	
</div> 
</form> 
</div> 
