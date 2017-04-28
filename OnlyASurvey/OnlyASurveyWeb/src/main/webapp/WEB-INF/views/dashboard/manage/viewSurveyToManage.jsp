<%@page import="com.oas.model.util.QuestionTypeCode"%><%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}"/> - <fmt:message key='generalTab.title'/></title>
<p><a href="<c:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>
<div class="yui-g"> 
    <h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><fmt:message key='manageSurveyPage.introText'/></p>
	<div class="tabLinkBar"> 
		<a href="<c:url value='/html/db/mgt/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='generalTab'/></a>
		<a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<c:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><spring:message code='preferencesTab'/></a>
		<a href="<c:url value='/html/db/mgt/sec/${survey.id}.html'/>" class="button tabButton"><fmt:message key='securityTab'/></a>
		<a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><fmt:message key='publishTab'/></a>
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
		<form action="<c:url value='/html/db/mgt/nm/${survey.id}.html'/>" method="get"><div>
			<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.editTitleAlt"/>'/>
		</div></form>
		
		<%-- Welcome Message --%>
		<h2><a name="wMs"></a><fmt:message key='manageSurveyPage.welcomePageHeader'/></h2>


		<c:if test="${welcomeMessage != null}"><div><oas:html value='${welcomeMessage.displayTitle}'/></div></c:if>
		<form action='<c:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get"><div>
		<input type='hidden' name='0' value='welcomeMessage'/>
			<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#wMs"/>
		<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.changeText.welcome"/>'/>
		</div></form>
		
		<%-- Thank You Message --%>
		<h2><a name="tyMs"></a><fmt:message key='manageSurveyPage.thankYouMessage'/></h2>
		<c:if test="${thanksMessage != null}"><div><oas:html value='${thanksMessage.displayTitle}'/></div></c:if>
		<form action='<c:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get"><div>
			<input type='hidden' name='0' value='thanksMessage'/>
			<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#tyMs"/>
			<input type='submit' class='button' value='<fmt:message key="manageSurveyPage.editButton"/>' title='<fmt:message key="manageSurveyPage.changeText.thankYou"/>'/>
		</div></form>


		<%-- Paused Message --%>
		<h2><a name="pMs"></a><fmt:message key='publishTab.pausedMessageHeader'/></h2>
		<c:if test="${pausedMessage != null}"><div><oas:html value='${pausedMessage.displayTitle}'/></div></c:if>
		<form action='<c:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get">
		<div>
		<input type='hidden' name='0' value='pausedMessage'/>
		<input type='submit' class='button' value='<fmt:message key="publishTab.pausedMessageEditButton"/>' title='<fmt:message key="publishTab.pausedMessageEditButtonTitle"/>'/>
		<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#pMs"/>
		</div>
		</form>
		
		<%-- Closed Message --%>
		<h2><a name="cMs"></a><fmt:message key='publishTab.closedMessageHeader'/></h2>
		<c:if test="${closedMessage != null}"><div><oas:html value='${closedMessage.displayTitle}'/></div></c:if>
		<form action='<c:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get">
		<div>
		<input type='hidden' name='0' value='closedMessage'/>
		<input type='submit' class='button' value='<fmt:message key="publishTab.closedMessageEditButton"/>' title='<fmt:message key="publishTab.closedMessageEditButtonTitle"/>'/>
		<input type='hidden' name='rTo' value="/html/db/mgt/${survey.id}.html#cMs"/>
		</div>
		</form>
				
	</div>
</div>
