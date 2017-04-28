<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="enterprise.dashboard.pageTitle" /></title>
<h1><fmt:message key="enterprise.dashboard.pageTitle" /></h1>
<p><fmt:message key="enterprise.dashboard.pageIntro" /></p>

<%@ include file="quickStats.jsp" %>

<h2><fmt:message key="enterprise.accountOwner.header"/></h2>
<p><fmt:message key="enterprise.accountOwner.intro"/></p>
<c:if test="${not empty(accountOwners)}">
	<display:table
		name="accountOwners"
		size="accountOwnersSize"
		partialList="true"
		pagesize="25"
		id="acOw"
		uid="acOw"
		requestURI="/html/ent/db/db.html"
		class="enterpriseDashboardSummary"		
		>
	<display:column titleKey="enterprise.accountOwner.username">
	<a href="<c:url value='/html/ent/ao/${acOw.id}.html'/>"><c:out value="${acOw.username}"/></a>
	</display:column>
	<display:column property="joinDate" format="{0,date,short}" titleKey="enterprise.accountOwner.joinDate"/>
	<display:column property="billType" titleKey="enterprise.accountOwner.billType"/>
	<display:column escapeXml="true"  property="organization" titleKey="enterprise.accountOwner.organization"/>
	<display:column escapeXml="true" property="email" autolink="true" titleKey="enterprise.accountOwner.email"/>
	</display:table>
</c:if>


<h2><fmt:message key="enterprise.contactUs.header"/></h2>
<p><fmt:message key="enterprise.contactUs.intro"/></p>
<c:if test="${not empty(contactMessages)}">
	<display:table
		name="contactMessages"
		size="contactMessagesSize"
		partialList="true"
		pagesize="10"
		uid="cMsg"
		requestURI="/html/ent/db/db.html"
		class="enterpriseDashboardSummary"
		>
	<display:column property="created" format="{0,date,short}" titleKey="enterprise.contactUs.date"/>
	<display:column property="accountOwner.username" titleKey="enterprise.contactUs.accountIdentifier"/>
	<display:column escapeXml="true" property="email" autolink="true" titleKey="enterprise.contactUs.email"/>
	<display:column escapeXml="true" property="message" titleKey="enterprise.contactUs.message"/>
	</display:table>
</c:if>
