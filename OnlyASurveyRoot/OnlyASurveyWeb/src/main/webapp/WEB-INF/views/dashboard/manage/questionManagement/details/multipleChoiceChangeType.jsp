<%@ include file="/WEB-INF/views/includes.jspf"%>
<%-- allows the end user to change the type code --%>
<c:set var="checkboxTypeCode"><%=com.oas.model.util.QuestionTypeCode.CHECKBOX %></c:set>
<c:set var="radioTypeCode"><%=com.oas.model.util.QuestionTypeCode.RADIO %></c:set>
<fieldset class="twoColumnLayout">
<legend><fmt:message key='multipleChoiceChangeType.legend'/></legend>
<div>
	<label for="typeCode${radioTypeCode}"><fmt:message key='multipleChoiceChangeType.radiobuttons'/></label>
	<form:radiobutton id="typeCode${radioTypeCode}" path="typeCode" value="<%=com.oas.model.util.QuestionTypeCode.RADIO %>" />
</div>
<div>
	<label for="typeCode${checkboxTypeCode}"><fmt:message key='multipleChoiceChangeType.checkboxes'/></label>
	<form:radiobutton id="typeCode${checkboxTypeCode}" path="typeCode" value="<%=com.oas.model.util.QuestionTypeCode.CHECKBOX %>" />
</div>
</fieldset>
