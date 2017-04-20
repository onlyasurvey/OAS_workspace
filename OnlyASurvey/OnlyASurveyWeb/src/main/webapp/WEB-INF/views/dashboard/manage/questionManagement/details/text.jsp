<%@ include file="/WEB-INF/views/includes.jspf"%>

<div>
	<form:label path="fieldDisplayLength">
		<fmt:message key='editQuestion.fieldDisplayLength' />
	</form:label>
	<form:input path="fieldDisplayLength" size="6" maxlength="40" />
</div>
<div>
	<form:label path="maximumLength">
		<fmt:message key='editQuestion.maximumLength' />
	</form:label>
	<form:input path="maximumLength" size="6" maxlength="6" />
</div>
