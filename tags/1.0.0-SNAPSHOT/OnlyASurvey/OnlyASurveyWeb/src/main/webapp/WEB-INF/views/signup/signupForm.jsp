<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="signup.pageTitle" /> - OnlyASurvey.com</title>
<h1>
	<fmt:message key="signup.pageTitle" />
</h1>

<p>
	<fmt:message key="signup.pageIntro" />
</p>

<form:form id="signUpForm" action="" method="post">

<spring:bind path="command">

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

	<h2><fmt:message key="signup.contactInfo" /></h2>
	<fieldset class="twoColumnLayout"> 
		<div>
			<form:label path="firstname"><fmt:message key='signup.firstname' /></form:label>
			<form:input path="firstname" size="50" />
		</div>

		<div>
			<form:label path="lastname"><fmt:message key='signup.lastname' /></form:label>
			<form:input path="lastname" size="50" />
		</div>

		<div>
			<form:label path="organization"><fmt:message key='signup.organization' /></form:label>
			<form:input path="organization" size="50" />
		</div>

		<div>
			<form:label path="telephone"><fmt:message key='signup.telephone' /></form:label>
			<form:input path="telephone" size="20" />
		</div>

		<div>
			<form:label path="email[0]"><fmt:message key='signup.email' /></form:label>
			<form:input path="email[0]" size="50" />
		</div>

		<div>
			<form:label path="email[1]"><fmt:message key='signup.email.confirm' /></form:label>
			<form:input path="email[1]" size="50" />
		</div>

		<div>
			<form:label path="learnedAbout"><fmt:message key='signup.learnedAbout' /></form:label>
			<form:textarea path="learnedAbout" rows="8" cols="50" />
		</div>

		<h2><fmt:message key='signup.customizeOptions'/></h2>	

		
		<form:checkbox id="government" path="government"/>
		<form:label path="government"><fmt:message key='signup.isGovernment' /></form:label>

		<h2><fmt:message key="signup.usernameAndPassword" /></h2>

		<div>
			<form:label path="username">
				<fmt:message key='username' />
				<br /> <span class="helpText">(<fmt:message key='sixToTwentyLettersAndNumbers' />)</span>
			</form:label>
			<form:input path="username" size="22" />
		</div>

		<div>
			<form:label path="password[0]">
				<fmt:message key='password' />
				<br /> <span class="helpText">(<fmt:message key='sixToTwentyChars' />)</span>
			</form:label>
			<form:password path="password[0]" size="22" />
		</div>

		<div>
			<form:label path="password[1]">
				<fmt:message key='password' />
				<br /> <span class="helpText">(<fmt:message key='sixToTwentyChars' />)</span>
			</form:label>
			<form:password path="password[1]" size="22" />
		</div>

		<h2><spring:message code='signup.newsletterOptIn.header'/></h2>
		<form:checkbox id="news" path="news"/>
		<form:label path="news"><spring:message code='signup.newsletterOptIn.header'/></form:label>
		
		

	</fieldset>
	<div class='bottomButtonBar'>
		<input type='submit' class='button' value="<fmt:message key='signup.submit'/>" />
	</div>
</spring:bind>
</form:form>
