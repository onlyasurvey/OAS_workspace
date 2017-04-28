<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ include file="requiredIndicatorInclude.jsp" %>
<%@page import="com.oas.model.question.*" %>
<%-- required since we're not using the full form: tagset --%>
<fieldset class="ratioQuestion">
<input type="hidden" name="_choiceIdList" value="on" />
	<h2><c:out value="${question.displayTitle}" />${requiredIndicator}</h2>




<table class="ratioTable" summary="" border="0"> 
<tbody>
<tr>

<c:forEach items="${question.possibleValues}" var="rating">
<%-- NOTE: this is ugly, but it works. --%>
<c:set var="ratingForLabel" scope="request">${rating}</c:set>
<c:set var="questionForLabel" scope="request" value="${question}"/>
<c:set var="label"><%
	ScaleQuestion q = (com.oas.model.question.ScaleQuestion) request.getAttribute("questionForLabel");
	int scale = Integer.valueOf((String)request.getAttribute("ratingForLabel")).intValue();
	String label = q.getLabelForScale(scale);
	if(label == null || label.trim().length() == 0) {
		label = "&nbsp;";
	}
	out.print(label);
%></c:set>
	<td class="ratioTableHeader">
		<label for="q${question.id}v${rating}"><span class="topRQ"><oas:html value="${label}"/></span>
<c:if test="${! question.labelsOnly}">
		<span class="bottomRQ"><oas:html value="${rating}"/></span>
</c:if>
		</label>
	</td>
</c:forEach>
</tr>
<tr>
<c:forEach items="${question.possibleValues}" var="rating">
	<c:set var="checked"><c:if test="${rating == command.answer}">checked='checked'</c:if></c:set>
	<td><input id="q${question.id}v${rating}" name="answer" value="${rating}" type="radio" ${checked} /></td>
</c:forEach>
</tr>
</tbody></table>

</fieldset>
<%--
	<div class="OASRow">
		<c:forEach items="${question.possibleValues}" var="rating">
			<div class="OAS11col${rating} alignCenter"><label for="q${question.id}v${rating}">${rating}</label></div>
		</c:forEach>
	</div>
	<div class="clearBoth"></div>	
	<div class="OASRow">
	<c:forEach items="${question.possibleValues}" var="rating">
		<c:set var="checked"><c:if test="${rating == command.answer}">checked='checked'</c:if></c:set>
		<div class="OAS11col${rating} alignCenter">
		<input id="q${question.id}v${rating}"
			name="answer" type="radio" value="${rating}" ${checked}
			/></div>
	</c:forEach>
	</div>
--%>
