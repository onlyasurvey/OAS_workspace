<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='error'/></title>
<h1>
	<fmt:message key='error'/> - <c:out value="${survey.displayTitle}"/>
</h1>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>