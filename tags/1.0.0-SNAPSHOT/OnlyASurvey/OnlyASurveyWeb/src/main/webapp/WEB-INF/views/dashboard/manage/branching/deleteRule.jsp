<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:choose>
<c:when test="${subject.entryRule}">
<c:set var="pageTitle"><spring:message code='branching.deleteEntryRule.pageTitle'/></c:set>
<c:set var="paramName">_en</c:set>
</c:when>
<c:when test="${subject.exitRule}">
<c:set var="pageTitle"><spring:message code='branching.deleteExitRule.pageTitle'/></c:set>
<c:set var="paramName">_ex</c:set>
</c:when>
</c:choose>
<title><fmt:message key="${pageTitle}" /></title>
<h1><fmt:message key="${pageTitle}" /></h1>
<p><fmt:message key="branching.deleteRule.pageIntro" /></p>
<form action="<oas:url value='/html/db/mgt/qbr/rm/${question.id}.html'/>" method="post">
<div class='bottomButtonBar'>
	<input type='submit' class='button' name='_save' value='<fmt:message key="yes"/>' />
	<input type='submit' class='button' name='_cancel' value='<fmt:message key="no"/>' />
	<input type='hidden' name='${paramName}' value='${subject.id}' />
</div>
</form>