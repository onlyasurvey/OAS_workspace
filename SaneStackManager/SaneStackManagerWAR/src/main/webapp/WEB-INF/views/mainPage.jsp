<%@ include file="includes.jspf" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>

<h1>
	Welcome
</h1>
<p>
	Welcome to SADMAN, the SANE Stack common management application.
</p>
<h2>
	My Applications
</h2>
<p>
	The following is a list of all applications that you have the "application admin" role
	in. Click into an application to view it's summary and manage it's users.
</p>
<ul>
<c:forEach var="item" items="${applications}">
	<li><a href="viewApplication.html?id=<c:out value="${item.id}"/>"><c:out value="${item.identifier}"/></a> - <c:out value="${item.displayTitle}"/></li>
</c:forEach>
</ul>
