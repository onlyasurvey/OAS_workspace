<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <fmt:message key='publishTab.title' /></title>
<p><a href="<oas:url value='/html/db/db.html'/>"><fmt:message key='manageSurveyPage.backLink'/></a></p>
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
		<a href="<oas:url value='/html/db/mgt/${survey.id}.html'/>" class="button tabButton"><fmt:message key='generalTab'/></a>
		<a href="<oas:url value='/html/db/mgt/ql/${survey.id}.html'/>" class="button tabButton"><fmt:message key='questionsTab'/></a>
		<a href="<oas:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class="button tabButton"><fmt:message key='lookAndFeelTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pref/${survey.id}.html'/>" class="button tabButton"><fmt:message key='preferencesTab'/></a>
		<a href="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" class="button activeTabButton"><fmt:message key='publishTab'/></a>
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
			<div class="cell_1"><fmt:message key="publishTab.publicUrl" /></div>
			<div class="cell_2"><a href="${publicUrl}">${publicUrl}</a></div>
		</div>
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
	<form action="<oas:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="post">
		<div class="buttonBar">
			<input value="<spring:message code='publish'/>" name="_pb" type="submit" class="button" ${publishDisabled} />
			<input value="<spring:message code='draft'/>" name="_dr" type="submit" class="button" ${draftDisabled} />
		</div>
	</form>
</fieldset>

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
               
<%-- Paused Message --%>
<h2><a name="pMs"></a><fmt:message key='publishTab.pausedMessageHeader'/></h2>
<c:if test="${pausedMessage != null}"><p><c:out value='${pausedMessage.displayTitle}'/></p></c:if>
<form action='<oas:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get">
<div>
<input type='hidden' name='0' value='pausedMessage'/>
<input type='submit' class='button' value='<fmt:message key="publishTab.pausedMessageEditButton"/>' title='<fmt:message key="publishTab.pausedMessageEditButtonTitle"/>'/>
<input type='hidden' name='rTo' value="/html/db/mgt/pb/${survey.id}.html#pMs"/>
</div>
</form>

<%-- Closed Message --%>
<h2><a name="cMs"></a><fmt:message key='publishTab.closedMessageHeader'/></h2>
<c:if test="${closedMessage != null}"><p><c:out value='${closedMessage.displayTitle}'/></p></c:if>
<form action='<oas:url value="/html/db/mgt/ct/${survey.id}.html"/>' method="get">
<div>
<input type='hidden' name='0' value='closedMessage'/>
<input type='submit' class='button' value='<fmt:message key="publishTab.closedMessageEditButton"/>' title='<fmt:message key="publishTab.closedMessageEditButtonTitle"/>'/>
<input type='hidden' name='rTo' value="/html/db/mgt/pb/${survey.id}.html#cMs"/>
</div>
</form>

<%-- Statistics --%>
<h2><fmt:message key='publishTab.statsHeader'/></h2>
<p>
	<fmt:message key='publishTab.responseCount'/> ${responseCount}
</p>

<form action="<oas:url value='/html/db/mgt/rm/rsp/${survey.id}.html'/>" method="get">
<div>
	<input type='hidden' name='rTo' value="<oas:encode value='/html/db/mgt/pb/${survey.id}.html'/>"/>
	<input type='submit' class='button' value="<fmt:message key='deleteResponseData'/>"/>
</div>		
</form>


</div></div>