<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ include file="requiredIndicatorInclude.jsp" %>


<%@include file="commonQuestionHeader.jspf"%>

<fieldset class="radioButtons">

	<legend><c:out value="${question.displayTitle}"/>${requiredIndicator}</legend>
	<div class="radio">
		<input id='a1' type='radio' name='a' value='1' />
		<label for='a1'>
			<fmt:message key="booleanQuestion.answer.true" />
		</label>
	</div>
	<div class="radio">
		<input id='a2' type='radio' name='a' value='0' />
		<label for='a2'>
			<fmt:message key="booleanQuestion.answer.false" />
		</label>
	</div>
</fieldset>

<%@include file="commonQuestionFooter.jspf"%>