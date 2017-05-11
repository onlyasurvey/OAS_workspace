<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
		<h1>
			Login Required
		</h1>
		<c:if test="{param.failure}">
			Login Failed
		</c:if>
		<p>
			<fmt:message key="loginForm.introText"/>
		</p>
		<form id='loginForm' action="<%=basePath %>app/j_acegi_security_check" method="post">
			<label for='username'><fmt:message key="loginForm.username" /></label>
			<input type='text' id='username' name='j_username' length='32' value='test' />
			<br/>
			<label for='password'><fmt:message key="loginForm.password" /></label>
			<input type='password' id='password' name='j_password' length='32' value='test' />
			<br/>
			<input type='submit' value='<fmt:message key="loginForm.submit" />'/>
		</form>
