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
			An Error Occurred
		</h1>
		<p>
			Please accept our apologies, an unexpected error has occured. 
		</p>
		<p>
			Error:
			<b><c:out value="${exception.class}" /></b>
		</p>
	</body>
</html>
