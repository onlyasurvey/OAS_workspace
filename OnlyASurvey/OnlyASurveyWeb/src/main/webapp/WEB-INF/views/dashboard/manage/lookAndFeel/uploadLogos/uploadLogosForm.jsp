<%@ include file="/WEB-INF/views/includes.jspf"%>
<%@ include file="/WEB-INF/views/requiredIndicator.jsp" %>
<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
<title><spring:message code='${pageTitle}' arguments="${languageName}" /></title>

<%-- note mixing of resources from uploadLogos.* and uploadLogos.uploadImage.* --%>
<div class="yui-g">
	<h1><spring:message code='${pageTitle}' arguments="${languageName}"/></h1>
	<p><spring:message code='uploadLogos.uploadImage.pageIntro'/></p>
	<ul>
		<li><spring:message code='uploadLogos.note1'/></li>
		<li><spring:message code='uploadLogos.note2'/></li>
	</ul>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

	<form action="${survey.id}.html" method="post" enctype="multipart/form-data">
	<fieldset class="twoColumnLayout">
	<div>
		<label for='fileField'><spring:message code='uploadLogos.uploadImage.fileLabel'/> ${requiredIndicator}</label>
		<input type="file" name="upload" id="fileField"/>
	</div>
	<div>
		<label for='altText'><spring:message code='uploadLogos.uploadImage.toolTipLabel'/></label>
		<input type="text" name="altText" id="altText" size="50" maxlength="255"/>
	</div>
	<jsp:include page="/WEB-INF/views/saveButtonBar-indent.jsp"/>
	</fieldset>
	</form>
</div>
