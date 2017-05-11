<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="loginForm.pageTitle" /> - OnlyASurvey.com</title>
<div class="yui-g"> 
<h1><fmt:message key="loginForm.pageTitle" /></h1>
<p><fmt:message key="loginForm.introText" /></p> 
</div> 
<div class="yui-g">
	<c:if test='${param.fd eq "1"}'><div class='errorMessage'><fmt:message key='login.error'/></div></c:if>
	<form id='login' action="<oas:url value='/lgn/j_acegi_security_check'/>" method="post">
	<fieldset class="twoColumnLayout">
		<div>
			<label for='username'><fmt:message key="loginForm.username" /></label>
			<input name="j_username" id="username" type="text" size="15" maxlength="100" />
		</div>
		<div>
			<label for='password'><fmt:message key="loginForm.password" /></label>
			<input name="j_password" id="password" type="password" size="15" maxlength="100" />
		</div>
	<%-- 
				<div class="rememberPasswordLink">
					<input name="chk_remember" id="chk_remember" type="checkbox"
						checked="checked" value="on" />
					<label for="chk_remember">
						Remember me
					</label>
				</div>				 
		<div class="recoverPasswordLink">
			<label>&nbsp;</label>
			<a href='<oas:url value="/html/rcvrpw.html"/>'><fmt:message key="loginForm.recoverPassword" /></a>
		</div>
--%>
		<div class="signInButton">
			<label>&nbsp;</label>
			<input type='submit' class='button' value="<fmt:message key='loginForm.submit'/>" />
		</div>
		<div>
			<a href='<oas:url value="/html/sgnup.html"/>'><fmt:message key="loginForm.signUpLink" /></a>
		</div>
	</fieldset>
	</form>
</div>
