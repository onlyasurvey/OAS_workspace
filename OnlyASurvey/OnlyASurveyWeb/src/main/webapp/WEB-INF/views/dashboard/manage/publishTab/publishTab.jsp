<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <fmt:message key='publishTab.title' /></title>
<p><a href="<c:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>
<c:choose>
<c:when test="${survey.published}">
	<c:set var="draftDisabled" value="" />
	<c:set var="publishDisabled" value="disabled='disabled'" />
</c:when>
<c:otherwise>
	<c:set var="draftDisabled" value="disabled='disabled'" />
	<c:set var="publishDisabled" value="" />
</c:otherwise>
</c:choose>
<div class="yui-g">
	<h1><spring:message code='manageSurveyPage.surveyNamePrefix'/> <c:out value="${survey.displayTitle}" /></h1>
	<p><fmt:message key='manageSurveyPage.introText'/></p>
	<%-- TAB HEADER --%>
	<div class="tabLinkBar"> 
		<a href="<c:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><fmt:message key='generalTab'/></a>
		<a href="<c:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<c:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><spring:message code='preferencesTab'/></a>
		<a href="<c:url value='/html/db/mgt/sec/${survey.id}.html'/>" class="button tabButton"><fmt:message key='securityTab'/></a>
		<a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='publishTab'/></a>
	</div>
	<div class="tabArea">
		<p><fmt:message key='publishTab.pageIntro'/></p>
		<h2><fmt:message key='publishTab.statusHeader'/></h2>
		<p><fmt:message key='publishTab.statusIntro'/></p>

<fieldset class="twoColumnViewLayout">
	<div class="grid_row">
		<div class="cell_1 bold"><fmt:message key='publishTab.currentStatus'/></div>
		<div class="cell_2">
		<c:choose><c:when test="${survey.published}"><fmt:message key='published'/></c:when><c:otherwise><fmt:message key='draft'/></c:otherwise></c:choose>
		</div>
	</div>

	<%-- PUBLISHED --%>
	<c:choose>
	<c:when test="${survey.published}">
		<%-- TODO	
		<div class="grid_row">
			<div class="cell_1"><fmt:message key='publishTab.previewUrl'/></div>
			<div class="cell_2">-</div>
		</div>
		--%>
		<div class="grid_row">
			<div class="cell_1"><fmt:message key="publishTab.testUrl" /></div>
			<div class="cell_2"><a href="${publicUrl}"><fmt:message key="publishTab.testUrl.label" /></a> <fmt:message key="publishTab.testUrl.note" /></div>
		</div>
		<div class="grid_row">
			<div class="cell_1"><fmt:message key="publishTab.publicUrl" /></div>
			<div class="cell_2">${publicUrl}</div>
		</div>
		<c:if test="${not empty(shortUrl)}">
		<div class="grid_row">
			<div class="cell_1"><fmt:message key="publishTab.shortUrl" /></div>
			<div class="cell_2">${shortUrl}</div>
		</div>
		</c:if>
	</c:when>
	<%-- DRAFT --%>
	<c:otherwise>
		<%-- TODO	
		<div class="grid_row">
			<div class="cell_1"><fmt:message key='publishTab.previewUrl'/></div>
			<div class="cell_2"> - </div>
		</div>
		--%>
		<div class="grid_row">
			<div class="cell_1"><fmt:message key="publishTab.publicUrl" /></div>
			<div class="cell_2"><fmt:message key='publishTab.previewUrl.notPublished'/></div>
		</div>
	</c:otherwise>
	</c:choose>
	
<%-- Publish/Draft Buttons --%>
	<form action="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="post">
		<div class="buttonBar">
			<input value="<spring:message code='publish'/>" name="_pb" type="submit" class="button" ${publishDisabled} />
			<input value="<spring:message code='draft'/>" name="_dr" type="submit" class="button" ${draftDisabled} />
		</div>
	</form>
</fieldset>



<%-- 

	WEB-SITE INTEGRATION

	JavaScript-based lightbox and other integration methods.

 --%>
<h2><spring:message code="publishTab.siteIntegration.header" /></h2>
<p><spring:message code="publishTab.siteIntegration.intro" /></p>
<p><spring:message code="siteIntegrationEditor.lightbox.entropyNotice" /></p>
<p><spring:message code="publishTab.siteIntegration.scriptUrl.instructions" arguments="${optInScriptUrl},${optInCSSUrl}" /></p>
<p><strong><spring:message code='siteIntegrationEditor.lightbox.likelihood.input' /> ${survey.optinPercentage}% </strong><spring:message code='siteIntegrationEditor.lightbox.likelihood.input.note' /></p>
<form action="<c:url value='/html/db/mgt/pb/wsi/optin/${survey.id}.html'/>" method='get'>
	<input class='button' type='submit' value='<spring:message code="publishTab.siteIntegration.visitorOptIn" />'
		title='publishTab.siteIntegration.visitorOptIn.title' />
</form>




<%-- Manage Invites --%>
<h2><a name="sendInvites"></a><fmt:message key="publishTab.manageInvites.header" /></h2>
<table class="inviteSummary" width="100%" border="0">
	<tr class="tableHeading">
		<th scope="col"><fmt:message key="publishTab.manageInvites.totalRespondents" /></th>
		<th scope="col"><fmt:message key="publishTab.manageInvites.invitesSent" /></th>
		<th scope="col"><fmt:message key="publishTab.manageInvites.responses" /></th>
	</tr>
	<tr>
		<td>${totalRespondents}</td>
		<td>${totalInvitesSent}</td>
		<td>${totalResponses}</td>
	</tr>
</table>
<a href="inv/${survey.id}.html"><fmt:message key="publishTab.manageInvites.link" /></a>
 
<%-- Statistics --%>
<h2><fmt:message key='publishTab.statsHeader'/></h2>
<p>
	<fmt:message key='publishTab.responseCount'/> ${responseCount}
</p>

<form action="<c:url value='/html/db/mgt/rm/rsp/${survey.id}.html'/>" method="get">
<div>
	<input type='hidden' name='rTo' value="/html/db/mgt/pb/${survey.id}.html" />
	<input type='submit' class='button' value="<fmt:message key='deleteResponseData'/>"/>
</div>		
</form>


</div></div>