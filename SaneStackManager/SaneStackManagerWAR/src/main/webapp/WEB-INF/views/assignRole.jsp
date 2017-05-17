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
<title>Assign Role - <c:out value="${subject.displayTitle}" />
	(<c:out value="${application.displayTitle}" />)</title>
<h1>
	Assign Role
</h1>
<p>
	<c:out value="${subject.displayTitle}" />
	(<a href="viewApplication.html?id=<c:out value="${subject.application.id}"/>"><c:out value="${subject.application.displayTitle}" /></a>)
</p>
<p>
<form action='assignRole.html' method='get'>
	<input type='hidden' name='roleId' value='<c:out value="${subject.id}"/>'/>
	<label for='searchQ'>Assign to Person</label>
	<input type='text' name='q' id='searchQ' size='20'/>
	<input type='submit' value='Search and Add'/>
</form>
</p>
<p>
	Found <c:out value="${fn:length(list)}"/> matches to your query.
</p>

<table>
<thead>
	<th>Actor</th>
	<th>Actions</th>
</thead>
<tbody>
	<c:forEach var="item" items="${list}">
	<tr>
		<td><c:out value="${item.displayTitle}" /></td>
		<td><a href="assignRole.html?actorId=<c:out value="${item.id}"/>&amp;roleId=<c:out value="${subject.id}"/>">Assign Role</td>
	</tr>
	</c:forEach>
</tbody>
</table>
