<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='inviteNew.pageTitle' /></title>

<div class="yui-g">
<h1><fmt:message key="inviteNew.pageTitle" /> </h1>
<p><spring:message code="inviteNew.introText" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form id="command" action="${survey.id}.html" method="POST">
<fieldset class="twoColumnLayout">
<h2><spring:message code="inviteNew.sectionTitle" /></h2>
<div>
	<label for="subject"><fmt:message key="inviteNew.subjectField" /></label>
    <input type="text" id="subject" name="subject" size="78" maxlength="150" value="<c:out value='${command.subject}'/>" />
</div>
<div>
	<label for="message"><fmt:message key="inviteNew.messageField" /></label>
	<textarea name="message" cols="75" rows="15" id="message"><c:if test="${empty command.message}"><fmt:message key="inviteNew.defaultMessage" /></c:if><c:if test="${not empty command.message}"><c:out value='${command.message}' /></c:if>
	</textarea>

</div>

</fieldset>   
<div class="bottomButtonBar">
	<input  value="<fmt:message key="inviteNew.inviteBtn" />" name="_save"  type="submit" class="button"  />
	<input type='submit' class='button' value="<fmt:message key='cancel'/>" name="_cancel" />	
</div> 
</form> 
</div> 

