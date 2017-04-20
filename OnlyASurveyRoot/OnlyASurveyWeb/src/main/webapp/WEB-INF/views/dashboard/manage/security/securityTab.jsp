<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <spring:message code='securityTab.title' /></title>
<p><a href="<c:url value='/html/db/db.html'/>"><spring:message code='manageSurveyPage.backLink'/></a></p>

<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><spring:message code='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<c:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><spring:message code='generalTab'/></a>
		<a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><spring:message code='questionsTab'/></a>
		<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><spring:message code='lookAndFeelTab'/></a>
		<a href="<c:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><spring:message code='preferencesTab'/></a>
		<a href="<c:url value='/html/db/mgt/sec/${survey.id}.html'/>" class="button activeTabButton"><spring:message code='securityTab'/></a>
		<a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button tabButton"><spring:message code='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><spring:message code='securityTab.pageIntro'/></p>
		<h2><spring:message code='securityTab.header1'/></h2>
		<div class="grid_row">
			<div class="cell_1 bold"><spring:message code='securityTab.current.label'/></div>
			<div class="cell_2"><spring:message code='${statusLabel}'/></div>
		</div>
		
		<h2><spring:message code='securityTab.header2'/></h2>

		<form:form  action="${survey.id}.html" method="post">
		<fieldset class="OASAdmin_checkBoxGroup">
			<legend><spring:message code='securityTab.intro2'/></legend>
			<input type="hidden" name="_ids" value="on"/> 
			<div>
				<form:radiobutton id="so0" path="ids" value="0" />
				<form:label path="ids" for="so0"><spring:message code='securityTab.level.default'/></form:label>
			</div>
			<div> 
				<form:radiobutton id="so1" path="ids" value="1" />
				<form:label path="ids" for="so1"><spring:message code='securityTab.level.passwordPerSurvey'/></form:label>
			</div>
			<div class="buttonBar">
				<input type='submit' class='button' value="<spring:message code='securityTab.change'/>"/>
			</div>
		</fieldset>
		</form:form>


</div></div>