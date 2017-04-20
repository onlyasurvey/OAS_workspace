<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='inviteAll.pageTitle' /></title>

<div class="yui-g">
<h1><fmt:message key="inviteAll.pageTitle" /> </h1>
<p><spring:message code="inviteAll.introText" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form id="command" action="${survey.id}.html" method="post">
<fieldset class="twoColumnLayout">
<h2><spring:message code="inviteAll.sectionTitle" /></h2>
<div>
	<label for="subject"><fmt:message key="inviteAll.subjectField" /></label>
	<input type="text" id="subject" name="subject" size="78" maxlength="150" value="<c:out value='${command.subject}'/>" />
	</div>
<div>
	<label for="msgtext"><fmt:message key="inviteAll.messageField" /></label>
	<textarea name="message" cols="75" rows="15" id="msgtext"><c:if test="${empty command.message}"><fmt:message key="inviteAll.defaultMessage" /></c:if><c:if test="${not empty command.message}"><c:out value='${command.message}' /></c:if>
</textarea>
</div>

</fieldset>   
<div class="bottomButtonBar">
	<input  value="<fmt:message key="inviteAll.inviteBtn" />" name="_save"  type="submit" class="button"  />
	<input type='submit' class='button' value="<fmt:message key='cancel'/>" name="_cancel" />	
</div> 
</form> 
</div> 

