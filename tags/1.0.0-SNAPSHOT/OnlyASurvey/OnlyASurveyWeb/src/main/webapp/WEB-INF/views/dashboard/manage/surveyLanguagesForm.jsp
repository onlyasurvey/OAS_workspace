<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <fmt:message key='preferencesTab.title' /></title>
<p><a href="<oas:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>
<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><fmt:message key='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<oas:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><fmt:message key='generalTab'/></a>
		<a href="<oas:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<oas:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='preferencesTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><fmt:message key='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><fmt:message key='setSurveyPreferences.pageIntro' /></p>
		<h2><fmt:message key='setSurveyPreferences.languagesHeader' /></h2>
		<c:if test="${ ! survey.changeAllowed}">
			<p><fmt:message key='notice.published.cannotChangeLanguages'/></p>
		</c:if>
		
		<%@ include file="/WEB-INF/views/formErrors.jsp"%>
		
		<%-- LANGUAGES: CHANGE --%>
		<c:if test="${survey.changeAllowed}">
			<form:form action="${survey.id}.html">
			<fieldset class="twoColumnLayout">
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
				<input value="<fmt:message key='save'/>" name="_save"  type="submit" class="button" /> 
				<%--<input value="<fmt:message key='cancel'/>" name="_cancel"  type="submit" class="button" />--%>
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
