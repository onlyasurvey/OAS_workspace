<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code='lookAndFeelPage.pageTitle' /></title>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>


<form:form action="${survey.id}.html" method="post">
<div class="yui-g">
	<h1><spring:message code='mti.importTemplate.pageTitle'/></h1>       
	<p><spring:message code='mti.importTemplate.pageIntro'/></p>       
	
	<h2><spring:message code='mti.selectMethod.header' /></h2>
	
	<h3><spring:message code='mti.option1.header' /></h3>
	<p><spring:message code='mti.option1.text1' /></p>
	<p><spring:message code='mti.option1.text2' /></p>
	<p><spring:message code='mti.option1.beginComment' /></p>
	<p><spring:message code='mti.option1.endComment' /></p>
	<p><spring:message code='mti.option1.text3' /></p>

	<h3><spring:message code='mti.option2.header' /></h3>
	<p><spring:message code='mti.option2.text' /></p>

	
	<h2><spring:message code='mti.form.header' /></h2>
	<fieldset class="twoColumnLayout"> 
	<c:forEach items="${survey.supportedLanguages}" var="supportedLanguage">
	<c:set var="languageTitle"><c:out value='${supportedLanguage.displayTitle}'/></c:set>
		<div>
			<label for='map${supportedLanguage.iso3Lang}'><spring:message code='mti.importTemplate.perLanguageLabel' arguments="${languageTitle}" /> *</label>
			<form:input id='map${supportedLanguage.iso3Lang}' path="map[${supportedLanguage.iso3Lang}]" size="80" maxlength="512" />
		</div>
	</c:forEach>
	</fieldset>
	<div class="bottomButtonBar">
		<input name='_save' class='button' type='submit' value="<spring:message code='save'/>" />
		<input name='_cancel' class='button' type='submit' value="<spring:message code='cancel'/>" />
	</div> 
</div>
</form:form>