<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${survey.displayTitle}" /> - <spring:message code='uploadLogos.pageTitle' /></title>

<div class="yui-g">
	<h1><spring:message code='uploadLogos.h1'/></h1>
	<p><spring:message code='uploadLogos.pageIntro'/></p>

	<h2><spring:message code='uploadLogos.pageHeader'/></h2>
	<ul>
		<li><spring:message code='uploadLogos.note1'/></li>
		<li><spring:message code='uploadLogos.note2'/></li>
	</ul>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

	<form action="${survey.id}.html" method="post" enctype="multipart/form-data">
		<h3><spring:message code='uploadLogos.leftLogoHeader'/></h3>
		<fieldset class="twoColumnLayout"> 
		<c:forEach items="${survey.supportedLanguages}" var="language">
		<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
		<c:set var="languageCode"><c:out value="${language.iso3Lang}"/></c:set>
		<div>
			<label><spring:message code='uploadLogos.leftLogoLabel' arguments="${languageName}"/></label>
			<input type='file' name='llgo[${languageCode}]' />

		</div>
		<div>
			<label for='lalt[${languageCode}]'><spring:message code='uploadLogos.leftLogoTooltip' arguments="${languageName}"/></label>
			<input type='text' id='lalt[${languageCode}]' name='lalt[${languageCode}]' size='35' maxlength='255'/>
		</div>
		</c:forEach>
		</fieldset>
		
		<h3><spring:message code='uploadLogos.rightLogoHeader'/></h3>
		<fieldset class="twoColumnLayout"> 
		<c:forEach items="${survey.supportedLanguages}" var="language">
		<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
		<c:set var="languageCode"><c:out value="${language.iso3Lang}"/></c:set>
		<div>
			<label><spring:message code='uploadLogos.rightLogoLabel' arguments="${languageName}"/></label>
			<input type='file' name='rlgo[${languageCode}]' />

		</div>
		<div>
			<label for='ralt[${languageCode}]'><spring:message code='uploadLogos.rightLogoTooltip' arguments="${languageName}"/></label>
			<input type='text' id='ralt[${languageCode}]' name='ralt[${languageCode}]' size='35' maxlength='255'/>
		</div>
		</c:forEach>
		</fieldset>
		
		<jsp:include page="/WEB-INF/views/saveButtonBar.jsp"/>	
	</form>
</div>
