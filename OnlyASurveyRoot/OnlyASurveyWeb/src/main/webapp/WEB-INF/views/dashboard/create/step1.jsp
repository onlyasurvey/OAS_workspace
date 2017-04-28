<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="createSurvey.step1.title" /></title>
<div class='yui-g'>
<p><a href="<c:url value='/html/db/db.html'/>"><fmt:message key='backLink.dashboard'/></a></p>
<h1><fmt:message key="createSurvey.step1.title" /></h1>
<p><fmt:message key='createSurvey.step1.explanation'/></p>
<h2><fmt:message key="createSurvey.step1.stepHeader" /></h2>

<form:form action="crt.html" method="post">
<div>
	<input type='hidden' name='rTo' value="<c:out value='${param.rTo}'/>"/>
	<%@ include file="/WEB-INF/views/formErrors.jsp"%>
	<fieldset>
		<legend><fmt:message key="createSurvey.step1.stepIntro" /></legend>
		<c:forEach items="${supportedLanguages}" var="lang">
			<div>
				<form:checkbox path="ids" value="${lang.id}" id="lang${lang.id}" />
				<form:label path="ids" for="lang${lang.id}">
					<c:out value='${lang.displayTitle}' />
				</form:label>
			</div>
		</c:forEach>
	</fieldset>

</div>
<div class='bottomButtonBar'>
	<input type='submit' name='_lecnac' class='button' value="<fmt:message key='back' />" />
	<input type='submit' name='_evas' class='button' value="<fmt:message key='next' />" />
</div>
</form:form>
</div>