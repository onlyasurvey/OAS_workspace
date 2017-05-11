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
		<title>Manage Configuration - <c:out value="${application.displayTitle}" /></title>
	</head>
	<body>
		<h1>
			Manage Configuration - <c:out value="${application.displayTitle}" />
		</h1>
		
		<p>
			Every application can have zero or more Configuration Items, either system-wide values or application specific.
			
		</p>
		
		<p>Configuration Items (<c:out value="${fn:length(subject)}" /> entries)</p>
		<table style='width: 100%;'>
		<thead>
			<tr>
			<th>ID</th>
			<th>Name</th>
			<th>Value</th>
			<th>Type</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach var="item" items="${subject}">
			<tr>
				<td><c:out value="${item.id}"/></td>
				<td><a href="viewConfigurationItem.html?id=<c:out value="${item.id}"/>"><c:out value="${item.displayTitle}"/></a></td> 
				<td><c:out value="${item.value}"/></td>
				<td><c:out value="${item.valueType}"/></td>
			</tr>
		</c:forEach>
		</tbody>
		</table>
	</body>
</html>