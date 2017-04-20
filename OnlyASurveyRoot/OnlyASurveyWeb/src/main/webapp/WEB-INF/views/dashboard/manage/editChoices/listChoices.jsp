<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${question.displayTitle}"/> - <spring:message code='options' text="Options"/></title>
<p><a href="<c:url value='/html/db/mgt/ql/${question.survey.id}.html'/>"><fmt:message key='backTo'/> <c:out value="${question.survey.displayTitle}"/></a></p>
<h1><c:out value="${question.displayTitle}"/></h1>
<h2><spring:message code='options' text="Options"/></h2>
<div class="yui-g">

<form action="<c:url value='/html/db/mgt/qchs/${question.id}.html'/>?" method="get">
<div>
	<input type='submit' class='button' value="<spring:message code='addOptionLabel'/>" name="_a" />
	<input type='submit' class='button' value="<spring:message code='addManyOptionsLabel'/>" name="_am" />
</div>
</form>

<div>
<table width='100%' class="listOfQuestions"  border="0">
	<tr>
		<th class="actions">&nbsp;</th>
		<th class="actions">&nbsp;</th>
		<th class="labels">
			<spring:message code="title"/>
		</th>
		<th class="actions">&nbsp;</th>
	</tr>

	<c:forEach items="${question.choices}" var="choice" varStatus="loopStatus">
	<c:set var="optionNumber">${loopStatus.index + 1}</c:set>
		<tr>
			<td align='left'>
				<a href="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>?_e"><img src="<c:url value='/incl/images/icon-edit.gif'/>"
					width="25" height="25" class="icon" alt="<spring:message code='editChoiceIconAlt' arguments='${optionNumber}'/>"
					title="<spring:message code='editChoiceIconAlt' arguments='${optionNumber}'/>" /></a>
				<br/>
				<a href="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>?_cln"><img src="<c:url value='/incl/images/clone_icon.gif'/>"
					width="18" height="23" class="icon" alt="<spring:message code='cloneChoiceIconAlt' arguments='${optionNumber}'/>"
					title="<spring:message code='cloneChoiceIconAlt' arguments='${optionNumber}'/>" /></a> 

			</td>
			<td>
				<a href="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>?_mu"><img src="<c:url value='/incl/images/arrow_up.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveUpChoiceIconAlt' arguments='${optionNumber}'/>"
					title="<spring:message code='moveUpChoiceIconAlt' arguments='${optionNumber}'/>" /></a>
				<br/>
				<a href="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>?_md"><img src="<c:url value='/incl/images/arrow_down.png'/>"
					width="16" height="16" class="icon" alt="<spring:message code='moveDownChoiceIconAlt' arguments='${optionNumber}' />"
					title="<spring:message code='moveDownChoiceIconAlt' arguments='${optionNumber}'/>" /></a>
			</td>
			<td>
				<fieldset class="twoColumnLayout"> 
					<legend><spring:message code='createEditQuestion.optionHeaderLabel'/> ${optionNumber}</legend>
					<c:forEach items="${choice.objectNames}" var="name"> 
					<c:set var="languageTitle"><c:out value='${name.language.displayTitle}'/></c:set>
					<div>
						<label><spring:message code='languageHeaderLabel' arguments="${languageTitle}" />:</label>
						<c:out value="${name.value}"/>
					</div>
					</c:forEach>
				</fieldset>
			</td>
			<td align='right'>
				<a href="<c:url value='/html/db/mgt/qchs/${choice.id}.html'/>?_d"><img src="<c:url value='/incl/images/icon-delete.gif'/>"
					alt="<spring:message code='deleteNumber' arguments='${optionNumber}' />"
					title="<spring:message code='deleteNumber' arguments='${optionNumber}' />"
					class="icon" /></a>
			</td>
		</tr>
	</c:forEach>
</table>
</div>

<form action="<c:url value='/html/db/mgt/ql/${question.survey.id}.html'/>">
<div class="bottomButtonBar">
	<input value="<spring:message code='finishedEditingOptions'/>" type="submit" class="button" />
</div>
</form>
</div>