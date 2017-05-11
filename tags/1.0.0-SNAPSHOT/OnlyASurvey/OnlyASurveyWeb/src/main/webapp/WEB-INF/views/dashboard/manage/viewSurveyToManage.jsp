<%@page import="com.oas.util.QuestionTypeCode"%><%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/> - <fmt:message key='generalTab.title'/></title>
<p><a href="<oas:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>
<div class="yui-g"> 
    <h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><fmt:message key='manageSurveyPage.introText'/></p>
	<div class="tabLinkBar"> 
		<a href="<oas:url value='/html/db/mgt/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='generalTab'/></a>
		<a href="<oas:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<oas:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><fmt:message key='preferencesTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><fmt:message key='publishTab'/></a>
	</div>
	<div class="tabArea"> 
		<%-- TITLES --%>
		<h2><fmt:message key="manageSurveyPage.titles.header"/></h2>
		<p><fmt:message key="manageSurveyPage.titles.introText"/></p>
		<%-- show only titles for currently supported languages, regardless of any existing data. --%>
		<c:forEach items="${survey.supportedLanguages}" var="language">
			<c:set var="value" value="${objectNameMap[language.iso3Lang].value}"/>
			<h3><c:out value="${language.displayTitle}"/></h3>
			<c:if test="${not empty(value)}"><p><c:out value="${value}"/></p></c:if>
		</c:forEach>
		<form action="<oas:url value='/html/db/mgt/nm/${survey.id}.html'/>" method="get"><div>
			<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.editTitleAlt"/>'/>
		</div></form>
		
		<%-- Welcome Message --%>
		<h2><a name="wMs"></a><fmt:message key='manageSurveyPage.welcomePageHeader'/></h2>


		<c:if test="${welcomeMessage != null}"><p><c:out value='${welcomeMessage.displayTitle}'/></p></c:if>
		<form action='<oas:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get"><div>
		<input type='hidden' name='0' value='welcomeMessage'/>
			<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#wMs"/>
		<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.changeText.welcome"/>'/>
		</div></form>
		
		<%-- Thank You Message --%>
		<h2><a name="tyMs"></a><fmt:message key='manageSurveyPage.thankYouMessage'/></h2>
		<c:if test="${thanksMessage != null}"><p><c:out value='${thanksMessage.displayTitle}'/></p></c:if>
		<form action='<oas:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get"><div>
			<input type='hidden' name='0' value='thanksMessage'/>
			<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#tyMs"/>
			<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.changeText.thankYou"/>'/>
		</div></form>
	</div>
</div>
