<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${subject.displayTitle}"/> - <fmt:message key="viewAccountOwner.pageTitle" /></title>

<%@ include file="/WEB-INF/views/enterprise/enterpriseDashboard.backLink.jsp" %>

<h1><fmt:message key="viewAccountOwner.pageTitle" /> - <c:out value="${subject.displayTitle}"/></h1>

<h2><spring:message code="viewAccountOwner.surveyList"/></h2>
<display:table
	name="surveyList"
	id="srvLst" 
	uid="srvLst"
	class="enterpriseDashboardSummary"
	>
	<display:column property="created" format="{0,date,short}" titleKey="viewAccountOwner.surveyCreated"/>
	<display:column titleKey="viewAccountOwner.surveyName">
	<c:choose>
		<c:when test="${! srvLst.deleted}">
		<a href="<oas:url value='/html/db/mgt/${srvLst.id}.html'/>"><c:out value="${srvLst.displayTitle}"/></a>
		</c:when>
		<c:otherwise><c:out value="${srvLst.displayTitle}"/></c:otherwise>
	</c:choose>
	</display:column>
	<display:column titleKey="viewAccountOwner.surveyDeleted">
		<c:if test="${srvLst.deleted}"><spring:message code='yes'/></c:if>
	</display:column>
</display:table>
