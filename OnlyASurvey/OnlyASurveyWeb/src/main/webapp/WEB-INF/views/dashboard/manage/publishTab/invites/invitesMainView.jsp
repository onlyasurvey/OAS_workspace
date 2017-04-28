<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='invitesPage.pageTitle' /></title>
<div class="yui-g"> 
<p><a href="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>"><fmt:message key='invitesPage.backLink'/></a></p>

<h1><fmt:message key='invitesPage.pageTitle'/></h1>
<p><fmt:message key='invitesPage.introText'/></p>
<h2><fmt:message key='invitesPage.respondentsList'/></h2>
<div class="inviteLinkActions">
	<a class="linkButton" href="nwrsp/${survey.id}.html"><fmt:message key='invitesPage.addRespondentLink'/></a>
	<c:if test="${not empty inviteList && survey.published}">
		<a class="linkButton" href="nvtll/${survey.id}.html"><fmt:message key='invitesPage.inviteAllLink'/></a>  
		<a class="linkButton" href="nvtnw/${survey.id}.html"><fmt:message key='invitesPage.inviteNewLink'/></a> 
		<a class="linkButton" href="sndrmndr/${survey.id}.html"><fmt:message key='invitesPage.sendReminderLink'/></a>
	</c:if> 
</div>  

<c:if test="${not empty inviteList}">
	<display:table
		name="inviteList"
		size="inviteListSize"
		partialList="true"
		pagesize="20"
		uid="lid"
		requestURI="/html/db/mgt/pb/inv/${survey.id}.html"
		class="respondenteList"
		>
	<display:setProperty name="paging.banner.placement" value="bottom" />
	<display:column headerClass="alignLeft" scope="col" property="emailAddress" titleKey="invitesPage.emailHeader"/>
	<display:column headerClass="col" titleKey="invitesPage.statusHeader"><fmt:message key="invitesPage.status.${lid.status}" />
	</display:column>
	<display:column headerClass="col" property="reminderCount" autolink="true" titleKey="invitesPage.reminderNumHeader"/>
	<display:column headerClass="col" >
	<c:set var="deleteTitle"><spring:message code='invitesPage.deleteIcon.title' arguments="${lid.emailAddress}"/></c:set>
	<a href="dl/${lid.id}/${lid.survey.id}.html">
	<img src="<c:url value='/incl/images/icon-delete.gif'/>" alt="<c:out value='${deleteTitle}'/>"
		title="<c:out value='${deleteTitle}'/>" class="deleteIcon" />
	</a>
	</display:column>
	</display:table>
</c:if>

<form action="<c:url value='/html/db/mgt/pb/${survey.id}.html'/>" method="get">
<div class="bottomButtonBar">
<input type="submit" class="button" value="<spring:message code='back'/>" />
</div> 
</form>


                 



</div> 
  