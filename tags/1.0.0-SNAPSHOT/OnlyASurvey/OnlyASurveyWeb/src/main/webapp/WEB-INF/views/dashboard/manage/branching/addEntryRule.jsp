<%@ include file="/WEB-INF/views/includes.jspf"%>

<%@page import="com.oas.controller.dashboard.editsurvey.BranchingController"%><title><spring:message code="branching.addEntryRule.pageTitle" /></title>
<p><a href="<oas:url value='/html/db/mgt/qbr/${question.id}.html'/>"><spring:message code='backTo'/> <spring:message code='branching.pageTitle'/> </a></p>
<h1><spring:message code="branching.addEntryRule.pageTitle" /> - <c:out value='${question.displayTitle}'/></h1>
<p><spring:message code="branching.addEntryRule.pageIntro" /></p>
<p><spring:message code="branching.notice.rulesInOrder" /></p>

<c:set var="choiceSelectOptions">
	<c:forEach items="${question.survey.questions}" var="subject">
	<c:if test="${subject.choiceQuestion}">
		<optgroup label="<c:out value='${subject.displayTitle}'/>">
		<c:forEach items="${subject.choices}" var="choice">
			<option value="${choice.id}"><c:out value='${choice.displayTitle}'/></option>
		</c:forEach>
		<c:if test='${subject.allowOtherText}'>
			<option value="-1"><spring:message code='question.other'/></option>
		</c:if>
		</optgroup>
	</c:if>
	</c:forEach>
</c:set>
<c:set var="questionSelectOptions">
	<c:forEach items="${question.survey.questions}" var="subject">
		<option value="${subject.id}"><c:out value='${subject.displayTitle}'/></option>
	</c:forEach>
</c:set>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form action="<oas:url value='/html/db/mgt/qbr/aenr/${question.id}.html'/>" method="post">
<%--

		STEP 1

--%>
<h2><spring:message code="branching.addEntryRule.step1.header"/></h2>
<fieldset>
	<legend><spring:message code="branching.addEntryRule.step1.legend"/></legend>
	<br/>
	<div>
		<input name='wn' id='whOt' type='radio' value='<%=BranchingController.WHEN.OTHER_ANSWER %>' checked="checked" />
		<label for='whOt'><spring:message code='branching.addEntryRule.step1.otherAnswer'/></label>
	</div>
	<div>
		<input name='wn' id='whNt' type='radio' value='<%=BranchingController.WHEN.OTHER_EMPTY %>' />
		<label for='whNt'><spring:message code='branching.addEntryRule.step1.otherEmpty'/></label>
	</div>
	<div>
		<label for='wnOp'><spring:message code='branching.addEntryRule.questionSelect'/></label>
		<select name='wnOp' id='wnOp'>
		${questionSelectOptions}
		</select>
	</div>
	<br/>
	<div>
		<input name='wn' id='whCh' type='radio' value='<%=BranchingController.WHEN.CHOICE_ON %>' />
		<label for='whCh'><spring:message code='branching.addEntryRule.step1.choiceOn'/></label>
		<br/>
	</div>
	<div>
		<input name='wn' id='whNCh' type='radio' value='<%=BranchingController.WHEN.CHOICE_OFF %>' />
		<label for='whNCh'><spring:message code='branching.addEntryRule.step1.choiceOff'/></label>
	</div>
	<div>
		<label for='wnChOp'><spring:message code='branching.addEntryRule.choiceSelect'/></label>
		<select name='wnChOp' id='wnChOp'>
		${choiceSelectOptions}
		</select>
	</div>
	<br/>
	<div>
		<input name='wn' id='whD' type='radio' value='<%=BranchingController.WHEN.DEFAULT %>' />
		<label for='whD'><spring:message code='branching.addEntryRule.step1.default'/></label>
	</div>
</fieldset>


<%--

		STEP 2

--%>
<h2><spring:message code="branching.addEntryRule.step1.header"/></h2>
<fieldset>
	<legend><spring:message code="branching.addEntryRule.step2.legend"/></legend>
	<div>
		<input name='wt' id='wtS' type='radio' value='<%=BranchingController.WHAT.SKIP_QUESTION%>' checked="checked" />
		<label for='wtS'><spring:message code='branching.addEntryRule.step2.skip'/></label>
	</div>
	<div>
		<input name='wt' id='wtE' type='radio' value='<%=BranchingController.WHAT.SHOW_QUESTION%>' />
		<label for='wtE'><spring:message code='branching.addEntryRule.step2.show'/></label>
	</div>
</fieldset>

<div>
	<label>&nbsp;</label>
	<input type='submit' class='button' value='<fmt:message key="save"/>' name="_evas"/>
	<input type='submit' class='button' value='<fmt:message key="cancel"/>' name="_lecnac"/>
</div>

</form>
