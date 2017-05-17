<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code="branching.pageTitle" /></title>
<p><a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>"><fmt:message key='branching.backLink'/></a></p>
<h1><spring:message code="branching.pageTitle" /> - <c:out value='${question.displayTitle}'/></h1>
<p><spring:message code="branching.pageIntro" /></p>

<%--

		ENTRY RULES

--%>
<h2><spring:message code="branching.entryHeader"/></h2>
<p><spring:message code="branching.entryIntro"/></p>
<div>
	<a href='<c:url value='/html/db/mgt/qbr/aenr/${question.id}.html'/>'><spring:message code='branching.addEntryRule'/></a>
</div>
<c:if test="${not empty(entryRules)}">
<table class="branchingRules" width="100%" border="0">
<tr class="tableHeading">
	<th scope="col"><spring:message code='branching.header.ruleType'/></th>
	<th scope="col"><spring:message code='branching.header.action'/></th>
	<th scope="col"><spring:message code='branching.header.otherObject'/></th>
	<th scope="col">&nbsp;</th>
</tr>
<c:forEach items="${entryRules}" var="rule" varStatus="i">
<tr>
	<td>${rule.ruleType}</td>
	<td>
	<c:choose>
	<c:when test="${rule.otherObject.questionType}">
	<a href="<c:url value='/html/db/mgt/qbr/${rule.otherObject.id}.html'/>"><c:out value='${rule.otherObject.displayTitle}'/></a>
	</c:when>
	<c:when test="${rule.otherObject.choiceType}">
	<a href="<c:url value='/html/db/mgt/qbr/${rule.otherObject.question.id}.html'/>"><c:out value='${rule.otherObject.question.displayTitle}'/></a>
	<br/>
	&rarr;
	<c:out value='${rule.otherObject.displayTitle}'/>
	</c:when>
	</c:choose>
	</td>
	<td>${rule.action}</td>
	<td><a href='<c:url value="/html/db/mgt/qbr/rm/${question.id}.html?_en=${rule.id}"/>'
		><img class="deleteIcon" src="<c:url value='/incl/images/icon-delete.gif'/>"
			alt="<spring:message code='branching.deleteEntryRule' arguments="${i.index + 1}"/>"
			title="<spring:message code='branching.deleteEntryRule' arguments="${i.index + 1}"/>" /></a>
	</td>
</tr>
</c:forEach>
<tr> 
<td class="pagination" colspan="4">
</td>
</tr>
</table>
</c:if>

<%--

		EXIT RULES

--%>
<h2><spring:message code="branching.exitHeader"/></h2>
<p><spring:message code="branching.exitIntro"/></p>
<div>
	<a href="<c:url value='/html/db/mgt/qbr/aexr/${question.id}.html'/>"><spring:message code='branching.addExitRule'/></a>
</div>

<c:if test="${not empty(exitRules)}">
<table class="branchingRules" width="100%" border="0">
<tr class="tableHeading">
	<th scope="col"><spring:message code='branching.header.ruleType'/></th>
	<th scope="col"><spring:message code='branching.header.action'/></th>
	<th scope="col"><spring:message code='branching.header.choice'/></th>
	<th scope="col"><spring:message code='branching.header.jumpToQuestion'/></th>
	<th scope="col">&nbsp;</th>
</tr>
<c:forEach items="${exitRules}" var="rule" varStatus="i">
<tr>
	<td>${rule.ruleType}</td>
	<td>${rule.action}</td>
	<td><c:out value='${rule.choice.displayTitle}'/></td>
	<td>
	<c:choose>
	<c:when test="${rule.jumpToQuestion != null}">
	<a href="<c:url value='/html/db/mgt/qbr/${rule.jumpToQuestion.id}.html'/>"><c:out value='${rule.jumpToQuestion.displayTitle}'/></a>
	</c:when>
	<c:otherwise>&nbsp;</c:otherwise>
	</c:choose>
	</td>
	<td><a href='<c:url value="/html/db/mgt/qbr/rm/${question.id}.html?_ex=${rule.id}"/>'
		><img class="deleteIcon" src="<c:url value='/incl/images/icon-delete.gif'/>"
			alt="<spring:message code='branching.deleteEntryRule' arguments="${i.index + 1}"/>"
			title="<spring:message code='branching.deleteEntryRule' arguments="${i.index + 1}"/>" /></a>
	</td>
</tr>
</c:forEach>
</table>
</c:if>

<%--

		REFERENCES

--%>
<h2><spring:message code="branching.referenceHeader"/></h2>
<p><spring:message code="branching.referenceIntro"/></p>
<c:if test="${not empty(references)}">
<ul>
<c:forEach items="${references}" var="reference" varStatus="i">
<li><a href="<c:url value='/html/db/mgt/qbr/${reference.id}.html'/>"><c:out value="${reference.displayTitle}"/></a></li>
</c:forEach>
</ul>
</c:if>



