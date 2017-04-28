<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value='${survey.displayTitle}'/> - <fmt:message key="editQuestion.pageTitle" /></title>
<h1><fmt:message key="editQuestion.pageTitle" /></h1>
<p><fmt:message key="editQuestion.step1.stepIntro" /></p>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<spring:bind path="command">
	<form:form action="${question.id}.html" method="post">
		<div class="yui-g">
			<h2>
				<fmt:message key="editQuestion.step1.stepHeader" />
			</h2>
			<fieldset class="twoColumnLayout">
				<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
					<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
					<div>
						<label for="m${supportedLanguage.iso3Lang}"><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /> *</label>
						<form:textarea path="m[${supportedLanguage.iso3Lang}]" cols="60" rows="3" />
					</div>
				</c:forEach>
			</fieldset>


			<%-- for all question types --%>
			<fieldset class="twoColumnLayout">
				<c:if test="${command.showRequiredCheckbox}">
				<div>
					<form:label path="required"><fmt:message key='editQuestion.isRequired' /></form:label>
					<form:checkbox id='required' path="required" />
					<c:if test="${question.choiceQuestion}">
						<%-- Show note about None option for multiple-choice questions. --%>
						<spring:message code="editQuestion.isRequired.noneNote" />
					</c:if>
				</div>
				</c:if>
				<c:if test="${question.choiceQuestion}">
				<div>
					<form:label path="randomizeChoices"><fmt:message key='editQuestion.randomizeChoices' /></form:label>
					<form:checkbox id='randomizeChoices' path="randomizeChoices" />
				</div>
				</c:if>
				<c:if test="${showOtherTextOption}">
				<div>
					<form:label path="allowOtherText">
						<fmt:message key='editQuestion.allowOtherText' />
					</form:label>
					<form:checkbox id='allowOtherText' path="allowOtherText" />
				</div>
				</c:if>
	
				<%-- details view for NON-MULTIPLE-CHOICE questions --%>
				<c:choose>
				<c:when test="${question.textQuestion}">
					<c:if test="${question.numRows == 1}"><jsp:include page="../questionManagement/details/text.jsp"/></c:if>
					<c:if test="${question.numRows != 1}"><jsp:include page="../questionManagement/details/essay.jsp"/></c:if>
				</c:when>
				<c:when test="${question.scaleQuestion}">
					<jsp:include page="../questionManagement/details/scale.jsp"/>
				</c:when>
				<c:when test="${question.pageQuestion}">
					<jsp:include page="../questionManagement/details/page.jsp"/>
				</c:when>
				</c:choose>
			</fieldset>

			<%-- more multiple choice --%>
			<c:if test="${question.choiceQuestion && (command.radioType || command.checkboxType)}">
				<jsp:include page="../questionManagement/details/multipleChoiceChangeType.jsp"/>
			<%--	<jsp:include page="../questionManagement/details/multipleChoice.edit.choiceList.jsp"/>--%>
			</c:if>
			
			<%-- buttons at the bottom --%>
			<div class="bottomButtonBar">
				<input value="<fmt:message key='save'/>" name="_save" type="submit" class="button" />
				<c:if test="${question.choiceQuestion}">
					<input value="<fmt:message key='saveAndEditChoices'/>" name="_saveAndEditChoices" type="submit" class="button" />
				</c:if>
				<input value="<fmt:message key='cancel'/>" name="_cancel" type="submit" class="button" />
			</div>

		</div>

	</form:form>
</spring:bind>