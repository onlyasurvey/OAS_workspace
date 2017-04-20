<%@ include file="/WEB-INF/views/includes.jspf"%>

<spring:bind path="numRows">
<div>
	<form:label path="numRows">
		<fmt:message key='editQuestion.numRows' />
	</form:label>
	<form:input path="numRows" size="6" maxlength="3" />
</div>
</spring:bind>
<spring:bind path="maximumLength">
<div>
	<form:label path="maximumLength">
		<fmt:message key='editQuestion.maximumLength' />
	</form:label>
	<form:input path="maximumLength" size="6" maxlength="40" />
</div>
</spring:bind>
