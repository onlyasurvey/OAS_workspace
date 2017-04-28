<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <spring:message code='preferencesTab.title' /></title>
<p><a href="<c:url value='/html/db/db.html'/>"><spring:message code='manageSurveyPage.backLink'/></a></p>
<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><spring:message code='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<c:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><spring:message code='generalTab'/></a>
		<a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><spring:message code='questionsTab'/></a>
		<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><spring:message code='lookAndFeelTab'/></a>
		<a href="<c:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button activeTabButton"><spring:message code='preferencesTab'/></a>
		<a href="<c:url value='/html/db/mgt/sec/${survey.id}.html'/>" class="button tabButton"><fmt:message key='securityTab'/></a>
		<a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><spring:message code='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><spring:message code='setSurveyPreferences.pageIntro' /></p>
		<h2><spring:message code='setSurveyPreferences.languagesHeader' /></h2>
		<c:if test="${ ! survey.changeAllowed}">
			<p><spring:message code='notice.published.cannotChangeLanguages'/></p>
		</c:if>
		
		<%@ include file="/WEB-INF/views/formErrors.jsp"%>
		
		<%-- LANGUAGES: CHANGE --%>
		<c:if test="${survey.changeAllowed}">
			<form:form action="${survey.id}.html" >
			<fieldset class="twoColumnLayout">
			<legend><spring:message code='setSurveyPreferences.legend'/></legend>
			<c:forEach items="${supportedLanguages}" var="language">
			<div>
				<form:label path="ids" for="lang${language.id}">
					<c:out value='${language.displayTitle}' />
				</form:label>
				<form:checkbox path="ids" value="${language.id}" id="lang${language.id}"  />
			</div>
			</c:forEach>
			</fieldset>
			<%-- BUTTONS --%>
			<div class="bottomButtonBar"> 
				<input value="<spring:message code='save'/>" name="_evas"  type="submit" class="button" /> 
			</div> 
			</form:form>
		</c:if>
		
		<%-- LANGUAGES: VIEW ONLY --%>
		<c:if test="${ ! survey.changeAllowed}">
			<ul>
			<c:forEach items="${survey.supportedLanguages}" var="language">
			<li><c:out value='${language.displayTitle}' /></li>
			</c:forEach>
			</ul>
		</c:if>
	</div>
</div>
