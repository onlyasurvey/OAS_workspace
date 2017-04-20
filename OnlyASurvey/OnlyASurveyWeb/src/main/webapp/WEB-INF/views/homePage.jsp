<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="homepage.pageTitle" /> - OnlyASurvey.com</title>
<%-- Sign Up banner --%>
<div class="yui-g">
<sec:authorize ifNotGranted="ROLE_USER">
<div id="signupnow">
	<div id="signupnow-text"><h2><fmt:message key='loginForm.startToday' /></h2></div>
	<div id="signupnow-button"><a
		href="<c:url value='/html/sgnup.html'/>"><fmt:message
		key='loginForm.signUpNow' /></a>
	</div>
</div>
</sec:authorize>

<%-- Login Error --%>
<c:if test='${param.failed eq "true"}'>
<div class='errorMessage'><fmt:message key='login.error'/></div>
</c:if>

<%-- If logged in, provide potentially confused users (why am I seeing this page?) with a link to the dashboard --%>
<sec:authorize ifAllGranted="ROLE_USER">
<p><a href="<c:url value='/html/db/db.html'/>"><fmt:message key='homepage.backLink'/></a></p>
</sec:authorize>
 
<%-- Login Box --%>
<sec:authorize ifNotGranted="ROLE_USER"> 
<div class="loginBox">
<form id='login' action="<c:url value='/lgn/j_acegi_security_check'/>" method="post">
	<div class="field">
		<label for='username'><fmt:message key="loginForm.username" /></label>
		<input name="j_username" id="username" type="text" size="15" maxlength="100" />
	</div>
	<div class="field">
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
		<a href='<c:url value="/html/rcvrpw.html"/>'><fmt:message key="loginForm.recoverPassword" /></a>
	</div>
			 --%>
	<div class="signInButton">
		<input type='submit' class='button' value="<fmt:message key='loginForm.submit'/>" />
	</div>
</form>
</div>
</sec:authorize>

<%-- via the model, this is loaded from the PublicSiteContent repo by the controller. --%>
${pageContent}

                  
</div>
