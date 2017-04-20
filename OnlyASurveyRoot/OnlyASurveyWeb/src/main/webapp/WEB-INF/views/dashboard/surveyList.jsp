<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='surveyListPage.title' /></title>
<h1>
	<fmt:message key='surveyListPage.title' />
</h1>
<p>
	<fmt:message key="surveyListPage.introText" />
</p>
<form class="newSurveyForm" action="<c:url value='/html/db/crt.html'/>" method="get">
<div>
	<input type="submit" class="button" value="<fmt:message key="surveyListPage.createSurveyButton" />" />
</div>
</form>
<table class="manageSurveysTable" summary="" border="0">
	<tr>
		<th><fmt:message key='surveyListPage.titleHeader' /></th>
		<th><fmt:message key='surveyListPage.responses' /></th>
		<th><fmt:message key='surveyListPage.status' /></th>
		<th>&nbsp;</th>
	</tr>
	<c:forEach items="${summaryList}" var="summary">
	<c:set var="surveyName"><c:out value='${summary.survey.displayTitle}'/></c:set>
		<tr>
			<td>${surveyName}</td>
			<td>
				${summary.count}
			</td>
			<td>
				<c:if test="${summary.survey.published}">
					<fmt:message key="published" />
				</c:if>
				<c:if test="${! summary.survey.published}">
					<fmt:message key="draft" />
				</c:if>
			</td>
			<td>
				<c:if test="${summary.survey.changeAllowed}">
					<a href="<c:url value='/html/db/mgt/${summary.survey.id}.html'/>"><img class="icon" src="<c:url value='/incl/images/icon-edit.gif'/>"
						alt="<fmt:message key='editSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='editSurveyIconAlt'/> ${surveyName}" /></a>
					<img class="icon" src="<c:url value='/incl/images/view.gif'/>"
						alt="<fmt:message key='viewSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='viewSurveyIconAlt'/> ${surveyName}"	/>
					<a href='<c:url value="/html/db/mgt/rm/${summary.survey.id}.html"/>'><img class="deleteIcon" src="<c:url value='/incl/images/icon-delete.gif'/>"
						alt="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName}" /></a>
					<a href='<c:url value="/html/db/rpt/${summary.survey.id}.html"/>'><img class="icon" src="<c:url value='/incl/images/icon-report.png'/>"
						alt="<fmt:message key='reportsIconAlt'/> ${surveyName}" title="<fmt:message key='reportsIconAlt'/> ${surveyName}" /></a>
				</c:if>
				<c:if test="${!summary.survey.changeAllowed}">
					<img class="icon" src="<c:url value='/incl/images/edit_disabled.png'/>"
						alt="<fmt:message key='editSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>"
						title="<fmt:message key='editSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>" />
					<img class="icon" src="<c:url value='/incl/images/view_disabled.gif'/>"
						alt="<fmt:message key='viewSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>"
						title="<fmt:message key='viewSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>" />
					<img class="deleteIcon" src="<c:url value='/incl/images/icon-delete_disabled.gif'/>"
						alt="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>"
						title="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>" />
					<img class="icon" src="<c:url value='/incl/images/report_disabled.png'/>"
						alt="<fmt:message key='reportsIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>"
						title="<fmt:message key='reportsIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>" />
				</c:if>
			</td>
		</tr>
	</c:forEach>
</table>