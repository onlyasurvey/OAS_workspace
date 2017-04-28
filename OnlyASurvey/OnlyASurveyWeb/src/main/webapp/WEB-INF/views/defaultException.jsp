<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>An Error Occurred</title>
	</head>
	<body>
		<h1>
			Error
		</h1>
		<p>
			Please accept our apologies, an error has occurred. 
		</p>
		<p>
			An email has been sent to our staff to investigate this error.
			For support please contact <a href="mailto:info@onlyasurvey.com">info@onlyasurvey.com</a>.
		</p>
	<c:if test="${not empty(exception)}">
<!--

Error:
<c:out value="${exception.class.simpleName}" />

-->
<%
//if all else fails: 
//((Exception)request.getAttribute("exception")).printStackTrace();
%>
	</c:if>
	</body>
</html>
