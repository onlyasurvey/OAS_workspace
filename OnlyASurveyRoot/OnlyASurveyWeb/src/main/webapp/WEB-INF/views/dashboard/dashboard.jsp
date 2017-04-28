<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="dashboardPage.title" /> - <sec:authentication property="principal.username"/></title>

<c:set var="numPublished" value="0"/>
<c:set var="numDraft" value="0"/>

<div class="yui-g">
<h1><fmt:message key="dashboardPage.title" /></h1>
<p><fmt:message key="dashboardPage.introText" /></p>

<p><a href="<c:url value='/html/db/crt.html'/>"><fmt:message key='dashboardPage.createNewSurvey'/></a></p>

<table class="manageSurveysTable" summary="" border="0">
	<tr>
		<th><fmt:message key='surveyList.titleHeader' /></th>
		<th><fmt:message key='surveyList.responses' /></th>
		<th><fmt:message key='surveyList.status' /></th>
		<th>&nbsp;</th>
	</tr>
	<c:forEach items="${summaryList}" var="summary">
	<c:set var="surveyName"><c:out value='${summary.survey.displayTitle}'/></c:set>
		<c:if test="${summary.survey.published}">
			<c:set var="status"><fmt:message key="published" /></c:set>
			<c:set var="numPublished" value="${numPublished + 1}"/>
		</c:if>
		<c:if test="${! summary.survey.published}">
			<c:set var="status"><fmt:message key="draft" /></c:set>
			<c:set var="numDraft" value="${numDraft + 1}"/>
		</c:if>
		<tr>
			<td>
				<a class="surveyLink alignLeft" href="<c:url value="/html/db/mgt/${summary.survey.id}.html"/>">${surveyName}</a>
			</td>
			<td>
				${summary.count}
			</td>
			<td>
				${status}
			</td>
			<td>
			<%--
			
				EDIT and VIEW
				
			
				<a href="<c:url value='/html/db/mgt/${summary.survey.id}.html'/>"><img class="icon" src="<c:url value='/incl/images/icon-edit.gif'/>"
					alt="<fmt:message key='editSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='editSurveyIconAlt'/> ${surveyName}" /></a>
			--%>
			<%--
				<img class="icon" src="<c:url value='/incl/images/view_disabled.gif'/>"
					alt="<fmt:message key='viewSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='viewSurveyIconAlt'/> ${surveyName}"	/>
			--%>
			<%--
			
				REPORTING
				
				depends on isPublished
			
			--%>
				<c:choose>
				<c:when test="${summary.survey.published || summary.count > 0}">
					<a href='<c:url value="/html/db/rpt/${summary.survey.id}.html"/>'><img class="icon" src="<c:url value='/incl/images/icon-report.png'/>"
						alt="<fmt:message key='reportsIconAlt'/> ${surveyName}" title="<fmt:message key='reportsIconAlt'/> ${surveyName}" /></a>
				</c:when>
				<c:otherwise>
					<img class="icon" src="<c:url value='/incl/images/report_disabled.png'/>"
						alt="<fmt:message key='reportsIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>"
						title="<fmt:message key='reportsIconAlt'/> ${surveyName} <fmt:message key='disabledIconAlt'/>" />
				</c:otherwise>
				</c:choose>


			<%--
			
				DELETE
				
			--%>

				<a href='<c:url value="/html/db/mgt/rm/${summary.survey.id}.html?rTo=/html/db/db.html"/>'><img class="deleteIcon" src="<c:url value='/incl/images/icon-delete.gif'/>"
					alt="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName}" title="<fmt:message key='deleteSurveyIconAlt'/> ${surveyName}" /></a>
			</td>
		</tr>
	</c:forEach>
</table>
</div>

<!--  Row 2 -->
<div class="yui-g">
<div class="yui-u first">


<h2>View Reports</h2>
<c:if test="${publishedCount > 0}">
<ul>
	<c:forEach items="${summaryList}" var="summary">
	<c:if test="${summary.survey.published}">
		<c:set var="surveyName"><c:out value='${summary.survey.displayTitle}'/></c:set>
		<li><a href="<c:url value='/html/db/rpt/${summary.survey.id}.html'/>" title="<fmt:message key='reportsIconAlt'/> ${surveyName}">${surveyName}</a></li>
	</c:if>
	</c:forEach>
</ul>
</c:if>
<p><a href="<c:url value='/html/db/rpt.html'/>">View All Reports</a></p>
</div>
<div class="yui-u">
<h2>Account Information</h2>

<p><strong>Account Type:</strong> ${accountOwner.billType}</p>
<p><strong>Survey Count: </strong></p>
<ul>
	<li><strong>Published</strong>: ${numPublished}</li>
	<li><strong>Draft</strong>: ${numDraft}</li>
</ul>

</div>
</div>
