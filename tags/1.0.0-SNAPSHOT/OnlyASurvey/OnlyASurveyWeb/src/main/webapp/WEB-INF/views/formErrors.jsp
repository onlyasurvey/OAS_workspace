<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<c:if test="${errors.errorCount > 0}">
<div class="errorMessage">
<ul>
<c:forEach items="${errors.allErrors}" var="error">
	<%-- exclude some built-in errors --%>
<c:choose>
<c:when test="${error.code != 'typeMismatch'}">
	<li><spring:message code="${error.code}" arguments="${error.arguments}"/></li>
</c:when>
</c:choose>
</c:forEach>
</ul>
</div>
</c:if>