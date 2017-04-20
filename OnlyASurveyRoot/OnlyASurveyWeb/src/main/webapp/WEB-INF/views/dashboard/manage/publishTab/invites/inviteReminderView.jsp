<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='invReminder.pageTitle' /></title>

<div class="yui-g">
<h1><fmt:message key="invReminder.pageTitle" /> </h1>
<p><spring:message code="invReminder.introNote" /></p>
<p><spring:message code="invReminder.introText" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form id="command" action="${survey.id}.html" method="post">
<fieldset class="twoColumnLayout">
<h2><spring:message code="invReminder.sectionTitle" /></h2>
<div>
	<label for="subject"><fmt:message key="invReminder.subjectField" /></label>
    <input type="text" id="subject" name="subject" size="78" maxlength="150" value="<c:out value='${command.subject}'/>" />
</div>
<div>
	<label for="msgtext"><fmt:message key="invReminder.messageField" /></label>
	<textarea name="message" cols="75" rows="15" id="msgtext"><c:if test="${empty command.message}"><fmt:message key="invReminder.defaultMessage" /></c:if><c:if test="${not empty command.message}"><c:out value='${command.message}' /></c:if>
</textarea>
</div>

</fieldset>   
<div class="bottomButtonBar">
	<input  value="<fmt:message key="invReminder.remindBtn" />" name="_save"  type="submit" class="button"  />
	<input type='submit' class='button' value="<fmt:message key='cancel'/>" name="_cancel" />	
</div> 
</form> 
</div> 

