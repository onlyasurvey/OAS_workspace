<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:set var="languageName"><c:out value='${language.displayTitle}'/></c:set>
<title><spring:message code='uploadQuestionImage.pageTitle' arguments="${languageName}" /></title>
<div class="yui-g">
<h1><spring:message code='uploadQuestionImage.pageHeader' arguments="${languageName}"/></h1>
<p><spring:message code='uploadQuestionImage.formatHeader' arguments="${languageName}" /></p>
<ul> 
<li><spring:message code='uploadQuestionImage.format1'/></li>
<li><spring:message code='uploadQuestionImage.format2'/></li>
</ul>
<form action="<c:url value='/html/db/mgt/qatt/ul/${subject.id}.html'/>" method="post" enctype="multipart/form-data">
<fieldset class="twoColumnLayout"> 
<div> 
<label for='ff'><spring:message code='uploadQuestionImage.imageFileLabel'/></label> 
<input type="file" name="upload" id="ff"/> 
</div>
<div>
	<label>&nbsp;</label>
	<input value="<fmt:message key='save'/>" name="_evas" type="submit" class="button" />
	<input value="<fmt:message key='cancel'/>" name="_lecnac" type="submit" class="button" />
	<input type='hidden' name='language' value='${command.language}'/>
</div>
</fieldset>
</form>

</div>
