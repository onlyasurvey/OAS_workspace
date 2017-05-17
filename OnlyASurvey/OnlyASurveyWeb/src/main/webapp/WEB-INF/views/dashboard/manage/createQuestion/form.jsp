<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:choose>
	<c:when test='${command.radioType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.radio" /></c:set></c:when>
	<c:when test='${command.checkboxType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.checkbox" /></c:set></c:when>
	<c:when test='${command.textType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.text" /></c:set></c:when>
	<c:when test='${command.essayType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.essay" /></c:set></c:when>
	<c:when test='${command.scaleType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.scale" /></c:set></c:when>
	<c:when test='${command.selectType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.select" /></c:set></c:when>
	<c:when test='${command.constantSumType}'><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle.constantSum" /></c:set></c:when>
	<c:otherwise><c:set var="pageTitle"><fmt:message key="createQuestion.pageTitle" /></c:set></c:otherwise>
</c:choose>
<title>${pageTitle}</title>

<h1>${pageTitle}</h1>
<p><fmt:message key="createQuestion.step1.stepIntro" /></p>
<%@ include file="/WEB-INF/views/formErrors.jsp"%>
<spring:bind path="command">
	<form:form action="${survey.id}.html?${command.typeCode}=" method="post">
		<div class="yui-g">
		<input type='hidden' name='${command.typeCode}' value=''/>
			<h2>
				<fmt:message key="createQuestion.step1.stepHeader" />
			</h2>
			<fieldset class="twoColumnLayout">
				<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
					<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
					<c:set var="label"><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /></c:set>
					<div>
						<label for="m${supportedLanguage.iso3Lang}">${label} *</label>
						<form:textarea path="m[${supportedLanguage.iso3Lang}]" cols="60" rows="3" title="${label}" />
					</div>
				</c:forEach>
			</fieldset>

			<fieldset class="twoColumnLayout">
				<%-- required? --%>
				<c:if test="${command.showRequiredCheckbox}">
				<div>
					<c:set var="alt"><fmt:message key='editQuestion.isRequired'/></c:set>
					<form:label path="required"><fmt:message key='editQuestion.isRequired' /></form:label>
					<form:checkbox id='required' path="required" title="${alt}" />
					<c:if test="${command.radioType || command.checkboxType || command.selectType}">
						<%-- Show note about None option for multiple-choice questions. --%>
						<spring:message code="editQuestion.isRequired.noneNote" />
					</c:if>
				</div>
				</c:if>
				
				<c:if test="${command.radioType || command.checkboxType || command.selectType || command.constantSumType}">
				<div>
					<form:label path="randomizeChoices"><fmt:message key='editQuestion.randomizeChoices' /></form:label>
					<form:checkbox id='randomizeChoices' path="randomizeChoices" />
				</div>
				</c:if>

				<%-- for all question types --%>
				<c:if test="${showOtherTextOption}">
				<div>
					<c:set var="alt"><fmt:message key='editQuestion.allowOtherText'/></c:set>
					<form:label path="allowOtherText"><fmt:message key='editQuestion.allowOtherText' /></form:label>
					<form:checkbox id='allowOtherText' path="allowOtherText" title="${alt}" />
				</div>
				</c:if>

				<%-- details view --%>
				<jsp:include page="../questionManagement/${detailsViewName}.jsp"/>
			</fieldset>

			<%-- buttons at the bottom --%>
			<div class="bottomButtonBar">
				<input value="<fmt:message key='save'/>" name="_save" type="submit" class="button" />
				<c:if test="${command.radioType || command.checkboxType || command.selectType || command.constantSumType}">
					<input value="<fmt:message key='saveAndAddMoreChoices'/>" name="_saveAndEditChoices" type="submit" class="button" />			
				</c:if>
				<input value="<fmt:message key='cancel'/>" name="_cancel" type="submit" class="button" />
			</div>

		</div>

	</form:form>
</spring:bind>
