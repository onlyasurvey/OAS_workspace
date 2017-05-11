<%@ include file="/WEB-INF/views/includes.jspf"%>
<h3><fmt:message key='options'/></h3>

<table class="multipleSelect" border="0">
	<tr>
		<th class="labels">
			<fmt:message key="title"/>
		</th>
		<th class="actions">&nbsp;</th>
	</tr>

	<c:forEach items="${command.choiceList}" var="choice"
		varStatus="loopStatus">
		<tr>
			<td class="alignCenter">
				<fieldset class="twoColumnLayout"> 
					<legend><fmt:message key='createEditQuestion.optionHeaderLabel'/> ${loopStatus.index + 1}</legend> 
					<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
					<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
						<div>
							<form:label path="choiceList[${loopStatus.index}].map[${supportedLanguage.iso3Lang}]">
								<spring:message code='languageHeaderLabel' arguments="${languageTitle}" />: 
							</form:label>
							<form:input
								path="choiceList[${loopStatus.index}].map[${supportedLanguage.iso3Lang}]"
	 							maxlength="255" size="35" />
						</div>						
					</c:forEach>
				</fieldset>
			</td>
			<td>			
			<%--
				<img src="<oas:url value='/incl/images/icon-delete.gif'/>"
					alt="<fmt:message key='deleteNumber'><fmt:param value='${loopStatus.index + 1}' /></fmt:message>"
					title="<fmt:message key='deleteNumber'><fmt:param value='${loopStatus.index + 1}' /></fmt:message>"
					class="icon" />					
			--%>
			</td>
		</tr>
	</c:forEach>
</table>
