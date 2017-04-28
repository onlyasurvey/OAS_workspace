<%@ include file="/WEB-INF/views/includes.jspf"%>

<div>
	<form:label path="maximum">
		<fmt:message key='editQuestion.maximumScaleValue' />
	</form:label>
	<form:input path="maximum" size="3" maxlength="2" />
	<br/>
	<form:label path="labelsOnly">
		<fmt:message key='editQuestion.scale.labelsOnly' />
	</form:label>
	<form:checkbox id="labelsOnly" path="labelsOnly" />
</div>

<%-- SCALE VALUE LABELS; createMode is set in createQuestion/form. --%>
<c:if test="${empty(createMode)}">
	<h3> <fmt:message key='createEditQuestion.scaleValueLabelHeaderLabel'/> </h3>
	
	<table class="multipleSelect" border="0">
		<tr>
			<th class="labels">
				<fmt:message key="title"/>
			</th>
			<th class="actions">&nbsp;</th>
		</tr>
	
		<c:forEach items="${command.labelList}" var="item" varStatus="loopStatus">
			<tr>
				<td class="alignCenter" colspan="2">
					<fieldset class="twoColumnLayout"> 
						<legend><spring:message code='createEditQuestion.scaleValueLabelLegendLabel' arguments="${item.key}"/></legend> 
						<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
						<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
						<c:set var="label"><spring:message code='languageHeaderLabel' arguments="${languageTitle}" /></c:set>
							<div>
								<form:label path="labelList[${item.key}].map[${supportedLanguage.iso3Lang}]">
									${label}: 
								</form:label>
								<form:textarea cssClass="wysiwygEditor"
									path="labelList[${item.key}].map[${supportedLanguage.iso3Lang}]"
		 							rows="5" cols="60" title="${label}" />
							</div>						
						</c:forEach>
					</fieldset>
				</td>
			</tr>
		</c:forEach>
	</table>
</c:if>

<%@ include file="/WEB-INF/views/wysiwygEditor.jsp"%>
