<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<html>
	<head>
		<title>View Application - <c:out value="${subject.displayTitle}" /></title>
	</head>
	<body>
		<h1>
			View Application - <c:out value="${subject.displayTitle}" />
		</h1>
		<p>Manage Application</p>
		
		<ul>
			<li><a href="manageProperties.html?id=<c:out value="${subject.id}"/>">Manage Text Properties</a></li>
			<li><a href="manageConfiguration.html?id=<c:out value="${subject.id}"/>">Manage Configuration Items</a></li>
		</ul>
		
		<p>List of Roles (<c:out value="${fn:length(subject.roleDefinitions)}" /> entries)</p>
		<ul>
		<c:forEach var="item" items="${subject.roleDefinitions}">
			<li>
				<a href="viewRole.html?id=<c:out value="${item.id}"/>"><c:out value="${item.identifier}"/></a> - <c:out value="${item.displayTitle}"/>
			</li>
		</c:forEach>
		</ul>
	</body>
</html>