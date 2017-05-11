<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='contactUs.pageTitle' /> - OnlyASurvey.com</title>
<h1><fmt:message key='contactUs.pageTitle' /></h1>
<p><fmt:message key="contactUs.pageIntro" /></p>

<h2><fmt:message key="contact.main.header" /></h2>
<p>
	<fmt:message key='contact.main.phone'/>
	<fmt:message key='contact.main.phone.number'/>
</p>
<p>
	<fmt:message key='contact.main.email'/>
	<a href='mailto:<fmt:message key='contact.main.email.address'/>'><fmt:message key='contact.main.email.address'/></a>
</p>

<h2><fmt:message key='contactUs.header'/></h2>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="frm.html" method="post">
<div>
	<form:label path="email"><fmt:message key="contactUs.emailLabel"/></form:label>
	<form:input path="email"/>
</div>

<div>
	<form:label for='messageTextarea' path="message"><fmt:message key="contactUs.messageLabel"/></form:label>
	<br/>
	<form:textarea id="messageTextarea" path="message" cols="50" rows="9"/>
</div>
<div>
	<input type='submit' class='button' value='<fmt:message key="contactUs.submit"/>'/>
</div>
</form:form>