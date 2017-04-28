<%@ include file="/WEB-INF/views/includes.jspf"%>

<%@page import="com.oas.controller.dashboard.editsurvey.BranchingController"%>
<%@page import="com.oas.model.question.rules.ExitRuleType"%>
<%@page import="com.oas.model.question.rules.ExitRuleAction"%><title><spring:message code="branching.addExitRule.pageTitle" /></title>
<p><a href="<c:url value='/html/db/mgt/qbr/${question.id}.html'/>"><spring:message code='backTo'/> <spring:message code='branching.pageTitle'/> </a></p>
<h1><spring:message code="branching.addExitRule.pageTitle" /> - <c:out value='${question.displayTitle}'/></h1>
<p><spring:message code="branching.addExitRule.pageIntro" /></p>
<p><spring:message code="branching.notice.rulesInOrder" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<%--

		STEP 1

--%>
<h2><spring:message code="branching.addExitRule.step1.header"/></h2>
<form action="<c:url value='/html/db/mgt/qbr/aexr/${question.id}.html'/>" method="post">
<fieldset>
	<legend><spring:message code="branching.addExitRule.step1.legend"/></legend>
	<br/>
	<div>
		<input name='type' id='whHA' type='radio' value='<%=ExitRuleType.HAS_ANSWER %>'  checked="checked" />
		<label for='whHA'><spring:message code='branching.addExitRule.answered'/></label>
	</div>
	<div>
		<input name='type' id='whNA' type='radio' value='<%=ExitRuleType.NO_ANSWER %>' />
		<label for='whNA'><spring:message code='branching.addExitRule.noAnswer'/></label>
	</div>
	<c:if test="${question.choiceQuestion}">
	<br/>
	<div>
		<input name='type' id='whCO' type='radio' value='<%=ExitRuleType.CHOICE_ON %>' />
		<label for='whCO'><spring:message code='branching.addExitRule.choiceOn'/></label>
	</div>
	<div>
		<input name='type' id='whCOf' type='radio' value='<%=ExitRuleType.CHOICE_OFF %>' />
		<label for='whCOf'><spring:message code='branching.addExitRule.choiceOff'/></label>
	</div>
	<div>
		<label for='choiceId'><spring:message code='branching.addExitRule.choiceSelect'/></label>
		<select name='choiceId' id='choiceId'>
		<c:forEach items="${question.choices}" var="choice">
			<option value="${choice.id}"><c:out value='${choice.displayTitle}'/></option>
		</c:forEach>
		<c:if test='${subject.allowOtherText}'>
			<option value="-1"><spring:message code='question.other'/></option>
		</c:if>
		</select>
	</div>
	</c:if>
	<br/>
	<div>
		<input name='type' id='whD' type='radio' value='<%=ExitRuleType.DEFAULT %>' />
		<label for='whD'><spring:message code='branching.addExitRule.default'/></label>
	</div>
</fieldset>


<%--

		STEP 2

--%>
<h2><spring:message code="branching.addExitRule.step2.header"/></h2>
<fieldset>
	<legend><spring:message code="branching.addExitRule.step2.legend"/></legend>
	<br/>
	<div>
		<input name='action' id='actionJ' type='radio' value='<%=ExitRuleAction.JUMP_TO_QUESTION%>' checked="checked" />
		<label for='actionJ'><spring:message code='branching.addExitRule.step2.saveAndJumpToQuestion'/></label>
	</div>
	<div>
		<label for='actionJT'><spring:message code='branching.addExitRule.step2.jumpToQuestionId'/></label>
		<select id='actionJT' name="jumpToQuestionId">
			<c:forEach items="${question.survey.questions}" var="subject">
				<option value="${subject.id}"><c:out value='${subject.displayTitle}'/></option>
			</c:forEach>
		</select>
	</div>
	<br/>
	<div>
		<input name='action' id='actionFF' type='radio' value='<%=ExitRuleAction.FORCE_FINISH%>' />
		<label for='actionFF'><spring:message code='branching.addExitRule.step2.forceFinish'/></label>
	</div>
</fieldset>

<div>
	<label>&nbsp;</label>
	<input type='submit' class='button' value='<fmt:message key="save"/>' name="_evas"/>
	<input type='submit' class='button' value='<fmt:message key="cancel"/>' name="_lecnac"/>
</div>

</form>
