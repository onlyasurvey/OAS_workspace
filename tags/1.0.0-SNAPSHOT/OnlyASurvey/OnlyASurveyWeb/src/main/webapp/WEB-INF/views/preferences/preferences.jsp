<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="preferences.pageTitle"/></title>
<div class="yui-g">
	<h1>
		<fmt:message key="preferences.pageTitle"/>
	</h1>
	<p>
	<fmt:message key="preferences.pageIntro"/>
	</p>
</div>

<form:form>

<h2><fmt:message key='preferences.personalPreferences'/></h2>

<form:label path="languageId"><fmt:message key="siteLanguage"/></form:label>
<form:select path="languageId" items="${languageList}" itemLabel="displayTitle" itemValue="id"/>



<h2><fmt:message key='preferences.surveyPreferences'/></h2>
<h2><fmt:message key='preferences.billingPreferences'/></h2>

	<%@ include file="/WEB-INF/views/bottomButtonBar.jspf" %>
</form:form>