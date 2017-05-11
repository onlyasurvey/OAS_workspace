<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <spring:message code='questionsTab.title' /></title>
<p><a href="<oas:url value='/html/db/db.html'/>"><spring:message code='manageSurveyPage.backLink'/></a></p>
<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><spring:message code='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<oas:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><fmt:message key='generalTab'/></a>
		<a href="<oas:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='questionsTab'/></a>
		<a href="<oas:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><fmt:message key='preferencesTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><fmt:message key='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><spring:message code='questionsPage.pageIntro' /></p>
		<h2><spring:message code='questionsPage.pageHeader' /></h2>

		<c:if test="${survey.changeAllowed}">
		<form action="<oas:url value='/html/db/mgt/q/crt/${survey.id}.html'/>" method="get">
		<div>
		<input type="submit" class="button" value="<spring:message code='manageSurveyPage.addQuestionButton'/>"/>
		</div>
		</form>
		</c:if>
		<c:if test="${ ! survey.changeAllowed}">
		<c:set var="publishTabUrl"><oas:url value='/html/db/mgt/pb/${survey.id}.html'/></c:set>
		<p><spring:message code='notice.published.cannotChangeQuestions' arguments="${publishTabUrl}"/></p>
		</c:if>
		<table width='100%' class="listOfQuestions" border="0">
		<tr> 
			<th style="width:5%;" ><spring:message code='option'/></th>
			<th style="width:5%;" >&nbsp;</th>
			<th><spring:message code='question'/></th> 
			<th style="width:5%;" ></th> 
		</tr> 
		<c:forEach items="${survey.questions}" var="question" varStatus="loopStatus">
		<c:set var="questionNumber">${loopStatus.index + 1}</c:set>
		<tr>
			<%-- CHANGE IS ALLOWED --%>
			<c:if test="${survey.changeAllowed}">
			<td align='left'>
			<span>
				<a href="<oas:url value='/html/db/mgt/q/edt/${question.id}.html'/>"><img src="<oas:url value='/incl/images/icon-edit.gif'/>"
					width="25" height="25" class="icon" alt="<spring:message code='editQuestionIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='editQuestionIconAlt' arguments='${questionNumber}'/>" /></a>
				<br/>
				<c:if test="${question.choiceQuestion}">
				<a href="<oas:url value='/html/db/mgt/qchs/${question.id}.html'/>"><img src="<oas:url value='/incl/images/icon-edit-choices.png'/>"
					width="18" height="18" class="icon" alt="<spring:message code='editQuestionChoicesIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='editQuestionChoicesIconAlt' arguments='${questionNumber}'/>" /></a>
				<br/>
				</c:if>
				<a href="<oas:url value='/html/db/mgt/clq/${question.id}.html'/>"><img src="<oas:url value='/incl/images/clone_icon.gif'/>"
					width="18" height="23" class="icon" alt="<spring:message code='cloneQuestionIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='cloneQuestionIconAlt' arguments='${questionNumber}'/>" /></a> 
				<br/>
				<a href="<oas:url value="/html/db/mgt/qbr/${question.id}.html"/>"><img src="<oas:url value='/incl/images/branching.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='branchingIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='branchingIconAlt' arguments='${questionNumber}'/>" /></a>
			</span>
			</td>
			<td align='left'>
				<a href="<oas:url value="/html/db/mgt/${survey.id}.html"/>?_dord&amp;qid=${question.id}&amp;ud=u"><img src="<oas:url value='/incl/images/arrow_up.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveUpQuestionIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='moveUpQuestionIconAlt' arguments='${questionNumber}'/>" /></a>
				<br/>
				<a href="<oas:url value="/html/db/mgt/${survey.id}.html"/>?_dord&amp;qid=${question.id}&amp;ud=d"><img src="<oas:url value='/incl/images/arrow_down.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveDownQuestionIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='moveDownQuestionIconAlt' arguments='${questionNumber}'/>" /></a>
				<br/>
			</td>
			</c:if>
			<%-- CHANGE IS NOT ALLOWED --%>
			<c:if test="${!survey.changeAllowed}">
			<td align='left'>
			<span>
				<img src="<oas:url value='/incl/images/edit_disabled.png'/>"
					width="25" height="25" class="icon" alt="<spring:message code='editQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='editQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
				<br/>
				<c:if test="${question.choiceQuestion}">
				<img src="<oas:url value='/incl/images/icon-edit-choices.png'/>"
					width="18" height="18" class="icon" alt="<spring:message code='editQuestionChoicesIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='editQuestionChoicesIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
				<br/>
				</c:if>
				<img src="<oas:url value='/incl/images/clone_icon_disabled.gif'/>"
					width="18" height="23" class="icon" alt="<spring:message code='cloneQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='cloneQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" /> 
				<br/>
				<img src="<oas:url value='/incl/images/branching.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='branchingIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='branchingIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
			</span>
			</td>
			<td align='left'>
				<img src="<oas:url value='/incl/images/arrow_up.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveUpQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='moveUpQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
				<br/>
				<img src="<oas:url value='/incl/images/arrow_down.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveDownQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='moveDownQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
			</td>
			</c:if>
			<td>
				<%-- wrapped in a form for CSS styles to be consistent --%><form action="#" onsubmit="return false;">
				<c:choose>
				<c:when test="${question.textQuestion}"><%@include file="../../../question/textQuestionDetails.jspf"%></c:when>
				<c:when test="${question.choiceQuestion}"><%@include file="../../../question/choiceQuestionDetails.jspf"%></c:when>
				<c:when test="${question.scaleQuestion}">
					<%@include file="../../../question/scaleQuestionDetails.jsp"%>				
				</c:when>
				<c:otherwise>
				BUG: unknown question type!
				</c:otherwise>
				</c:choose>
				<%-- wrapped in a form for CSS styles to be consistent --%></form>
			</td>
			<td align='right'>
			<c:if test="${survey.changeAllowed}">
				<a href="<oas:url value='/html/db/mgt/rm/${question.id}.html'/>"><img src="<oas:url value='/incl/images/icon-delete.gif'/>"
					alt="<spring:message code='deleteQuestionIconAlt' arguments='${questionNumber}'/>"
					title="<spring:message code='deleteQuestionIconAlt' arguments='${questionNumber}'/>" /></a>
			</c:if>
			<c:if test="${!survey.changeAllowed}">
				<img src="<oas:url value='/incl/images/icon-delete_disabled.gif'/>"
					alt="<spring:message code='deleteQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>"
					title="<spring:message code='deleteQuestionIconAlt' arguments='${questionNumber}'/> <spring:message code='disabledIconAlt'/>" />
			</c:if>
			</td>
		</tr>
		</c:forEach>
		</table>

		<c:if test="${empty(survey.questions)}">
		<p><spring:message code='manageSurveyPage.questions.addQuestionHint'/></p>
		</c:if>

	</div>
</div>
