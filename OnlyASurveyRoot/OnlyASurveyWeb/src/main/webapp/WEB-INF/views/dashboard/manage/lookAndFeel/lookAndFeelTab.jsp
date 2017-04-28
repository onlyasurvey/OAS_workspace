<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ page import="com.oas.model.templating.TemplateType" %>
<title><c:out value="${survey.displayTitle}" /> - <fmt:message key='lookAndFeelTab.title' /></title>
<p><a href="<c:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>

<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><fmt:message key='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<c:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><fmt:message key='generalTab'/></a>
		<a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<c:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><spring:message code='preferencesTab'/></a>
		<a href="<c:url value='/html/db/mgt/sec/${survey.id}.html'/>" class="button tabButton"><fmt:message key='securityTab'/></a>
		<a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><fmt:message key='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><fmt:message key='lookAndFeelPage.pageIntro' /></p>
		
		<h2><spring:message code='lookAndFeelPage.surveyTemplate.header'/></h2>
		
		<div class="grid_row"> 
			<div class="cell_1 bold"><spring:message code='lookAndFeelPage.surveyTemplate.currentTemplate'/></div> 
			<div class="cell_2">
			<c:choose>
				<c:when test="${command.ids[0] == 1}"><spring:message code='lookAndFeelPage.surveyTemplate.default'/></c:when>
				<c:when test="${command.ids[0] == 2}"><spring:message code='lookAndFeelPage.surveyTemplate.uploadYourLogos'/></c:when>
				<c:when test="${command.ids[0] == 99}"><spring:message code='lookAndFeelPage.surveyTemplate.magic'/></c:when>
				<c:otherwise>---</c:otherwise>
			</c:choose>
			</div> 
		</div> 
		
<%-- PREVIEW --%>
		<c:forEach items="${survey.supportedLanguages}" var="language">
			<c:set var="languageCode">${language.iso3Lang}</c:set>
			<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
			<div class="grid_row"> 
				<div class="cell_1 bold"></div> 
				<div class="cell_2"><a href="<c:url value='/html/db/mgt/lnf/pvw/${survey.id}.html?l=${languageCode}'/>"
				><spring:message code='lookAndFeelPage.previewLookAndFeelLink' arguments="${languageName}" /></a></div> 
			</div>
		</c:forEach>
		
		<h2><spring:message code='lookAndFeelPage.surveyTemplate.changeTemplateHeader'/></h2>
		<form:form action="chty/${survey.id}.html" method="post">
		<fieldset class="OASAdmin_checkBoxGroup">
		<legend><spring:message code='lookAndFeelPage.surveyTemplate.templateOptions'/></legend>

		<div>
		<form:radiobutton path="ids" value="1" id="typDef"/>
		<label for="typDef"><spring:message code='lookAndFeelPage.surveyTemplate.default'/></label>
		</div>
		<div>
		<form:radiobutton path="ids" value="2" id="typUpLgo"/>
		<label for="typUpLgo"><spring:message code='lookAndFeelPage.surveyTemplate.uploadYourLogos'/></label>
		</div>
		<div>
		<form:radiobutton path="ids" value="99" id="typMg"/>
		<label for="typMg"><spring:message code='lookAndFeelPage.surveyTemplate.magic'/></label>
		</div>
		
		<div class="buttonBar">
			<input value="<spring:message code='lookAndFeelPage.surveyTemplate.changeButton'/>" type="submit" class="button" />
		</div>
		
		</fieldset>
		</form:form>
	</div>
</div>
		