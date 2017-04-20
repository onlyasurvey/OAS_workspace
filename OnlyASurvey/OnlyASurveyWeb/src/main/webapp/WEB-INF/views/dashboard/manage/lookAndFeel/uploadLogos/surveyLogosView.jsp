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
		
		<c:forEach items="${survey.supportedLanguages}" var="language">
			<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
			<c:set var="languageCode"><c:out value="${language.iso3Lang}"/></c:set>
			<c:set var="imageAlt"><spring:message code='questionImagesView.editToolTip.blank'/></c:set>
			<c:choose>
				<c:when test="${not empty(leftLogos[languageCode])}">
					<c:set var="hasLogo" value="true" />
					<c:if test="${not empty(leftLogos[languageCode].altText)}">
						<c:set var="imageAlt"><c:out value="${leftLogos[languageCode].altText}"/></c:set>
					</c:if>
				</c:when>
				<c:otherwise>
					<c:set var="hasLogo" />
				</c:otherwise>
			</c:choose>
			
			<h2><spring:message code='uploadLogos.leftLogoHeader' arguments='${languageName}'/></h2>
			
			<%-- IF left logo EXISTS FOR language --%>
			<c:if test="${hasLogo}">
			<div class="leftLogo">
				<c:set var="date" value="${leftLogos[languageCode].uploadTime.time}"/>
				<img src="<c:url value='/html/srvy/lg/${survey.id}.${rightLogoExtension}?p=l&amp;l=${languageCode}&amp;dt=${date}'/>" alt="${imageAlt}" title="${imageAlt}" />
			</div>
			</c:if>
			<div class="linkActions">
				<a href="<c:url value='/html/db/mgt/lnf/uplg/l/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.leftLogoAdd.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.leftLogoAdd' arguments='${languageName}'/></a>
				<c:if test="${hasLogo}">
				<a href="<c:url value='/html/db/mgt/lnf/rmlg/l/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.leftLogoDelete.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.leftLogoDelete' arguments='${languageName}'/></a>
				</c:if> 
			</div>
			<c:if test="${hasLogo}">
			<div class="toolTipText">
				<spring:message code='uploadLogos.leftLogoTooltip'/>
				${imageAlt}
				<a href="<c:url value='/html/db/mgt/lnf/lgos/tt/l/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.leftLogoTooltip.edit.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.leftLogoTooltip.edit' arguments='${languageName}'/></a> 
			</div>
			</c:if>
		</c:forEach>		
		
		
		<c:forEach items="${survey.supportedLanguages}" var="language">
			<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
			<c:set var="languageCode"><c:out value="${language.iso3Lang}"/></c:set>
			<c:set var="imageAlt"><spring:message code='questionImagesView.editToolTip.blank'/></c:set>
			<c:choose>
				<c:when test="${not empty(rightLogos[languageCode])}">
					<c:set var="hasLogo" value="true" />
					<c:if test="${not empty(rightLogos[languageCode].altText)}">
						<c:set var="imageAlt"><c:out value="${rightLogos[languageCode].altText}"/></c:set>
					</c:if>
				</c:when>
				<c:otherwise>
					<c:set var="hasLogo" />
				</c:otherwise>
			</c:choose>
			
			<h2><spring:message code='uploadLogos.rightLogoHeader' arguments="${languageName}"/></h2>
			
			<%-- IF right logo EXISTS FOR language --%>
			<c:if test="${hasLogo}">
			<div class="rightLogo">
				<c:set var="date" value="${rightLogos[languageCode].uploadTime.time}"/>
				<img src="<c:url value='/html/srvy/lg/${survey.id}.${rightLogoExtension}?p=r&amp;l=${languageCode}&amp;dt=${date}'/>" alt="${imageAlt}" title="${imageAlt}" />
			</div>
			</c:if>
			<div class="linkActions">
				<a href="<c:url value='/html/db/mgt/lnf/uplg/r/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.rightLogoAdd.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.rightLogoAdd' arguments='${languageName}'/></a> 
				<c:if test="${hasLogo}">
				<a href="<c:url value='/html/db/mgt/lnf/rmlg/r/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.rightLogoDelete.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.rightLogoDelete' arguments='${languageName}'/></a>
				</c:if> 
			</div>
			<c:if test="${hasLogo}">
			<div class="toolTipText">
				<spring:message code='uploadLogos.rightLogoTooltip'/>
				${imageAlt}
				<a href="<c:url value='/html/db/mgt/lnf/lgos/tt/r/${languageCode}/${survey.id}.html'/>" title="<spring:message code='uploadLogos.rightLogoTooltip.edit.title' arguments='${languageName}'/>"
					><spring:message code='uploadLogos.rightLogoTooltip.edit' arguments='${languageName}'/></a>
			</div>
			</c:if>
		</c:forEach>		
	</form>
	
	<div class="bottomButtonBar"> 
	<a href="<c:url value='/html/db/mgt/lnf/${survey.id}.html'/>" class='linkButton'><spring:message code='back'/></a>
	</div>
</div>
