<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='reportsPage.pageTitle'/></title>
<h1><fmt:message key='reportsPage.pageTitle'/></h1>
<p><fmt:message key="reportsPage.introText" /></p>
<ul><c:forEach items="${list}" var="item">
<c:if test="${item.published}"><li><a href="<c:url value='/html/db/rpt/${item.id}.html'/>"><c:out value="${item.displayTitle}"/></a></li></c:if>
</c:forEach></ul>