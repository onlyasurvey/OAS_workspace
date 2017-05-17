<%@ include file="/WEB-INF/views/includes.jspf"%>

<%-- required since we're not using the full form: tagset --%>
<fieldset class="twoColumnLayout">
<input type="hidden" name="_choiceIdList" value="on" />

	<legend><c:out value="${question.displayTitle}" />${requiredIndicator}</legend>
	<c:forEach items="${choiceList}" var="item">
		<c:set var="itemTitle"><c:out value="${item.displayTitle}" /></c:set>
		<div>
			<c:if test="${question.unlimited}">
				<form:label path="command.sumByChoiceId[${item.id}]">${itemTitle}</form:label>
				<form:input path="command.sumByChoiceId[${item.id}]" size="5" maxlength="8" />
			</c:if>
			<c:if test="${!question.unlimited}">
				<form:radiobutton path="command.choiceIdList" label=" ${itemTitle}"
					value="${item.id}" cssClass="radio" />
			</c:if>
		</div>
	</c:forEach>
		

	<c:if test="${question.allowOtherText}">
		<div class="otherOption">
			<label for='choiceIdListOtherText${question.id}'>
				<fmt:message key='question.ifOtherPleaseSpecify' />
			</label>
			<form:input id='choiceIdListOtherText${question.id}' path="command.otherText" size="35" />
		</div>
	</c:if>

</fieldset>