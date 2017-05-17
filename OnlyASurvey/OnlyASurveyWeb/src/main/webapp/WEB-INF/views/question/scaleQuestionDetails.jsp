<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ include file="requiredIndicatorInclude.jsp" %>

<%-- required since we're not using the full form: tagset --%>
<fieldset class="ratioQuestion">
<input type="hidden" name="_choiceIdList" value="on" />
	<legend><c:out value="${question.displayTitle}" />${requiredIndicator}</legend>
	<div class="OASRow">
		<c:forEach items="${question.possibleValues}" var="rating">
			<div class="OAS11col${rating} alignCenter"><label for="q${question.id}v${rating}">${rating}</label></div>
		</c:forEach>
	</div>
	<div class="clearBoth"></div>	
	<div class="OASRow">
	<c:forEach items="${question.possibleValues}" var="rating">
		<c:set var="checked"><c:if test="${rating == command.answer}">checked='checked'</c:if></c:set>
		<div class="OAS11col${rating} alignCenter"><input id="q${question.id}v${rating}"
			name="answer" type="radio" value="${rating}" ${checked}
			/></div>
	</c:forEach>
			</div>
	</div>
</fieldset>
