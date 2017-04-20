<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code='questionImagesView.pageTitle' /></title>

<c:set var="blank"><spring:message code='questionImagesView.editToolTip.blank'/></c:set>

<p><a href="<c:url value='${backUrl}'/>"><spring:message code='questionImagesView.backLink'/></a></p>
<div class="yui-g">
<h1><spring:message code='questionImagesView.pageTitle'/></h1>
<p><spring:message code='questionImagesView.pageIntro'/></p>

<h2><spring:message code='questionImagesView.header1'/></h2>
<p><c:out value="${subject.displayTitle}"/> &nbsp;</p> 
<h3><spring:message code='questionImagesView.supportedFormatsAndSize.header'/></h3> 
<ul> 
<li><spring:message code='questionImagesView.supportedFormatsAndSize.line1'/></li> 
<li><spring:message code='questionImagesView.supportedFormatsAndSize.line2'/></li> 
</ul> 

<%--

FOR EACH LANGUAGE

TODO handles only single attachment

--%>

<c:forEach items="${survey.supportedLanguages}" var="language">
	<c:set var="languageName"><c:out value="${language.displayTitle}"/></c:set>
	<c:set var="languageCode"><c:out value="${language.iso3Lang}"/></c:set>
	<c:set var="showImage" value="false"/>
	<c:choose>
	<c:when test="${attachment.payloads[language] != null}">
	<c:set var="payload" value="${attachment.payloads[language]}"/>
	<c:set var="showImage" value="true"/>
	<c:set var="toolTip"><c:out value="${payload.altText}" /></c:set>
	<c:set var="uploadTime" value="${payload.uploadTime.time}"/>
	</c:when>
	<c:otherwise>
	<c:set var="toolTip">${blank}</c:set>
	</c:otherwise>
	</c:choose>
	
		<h2><spring:message code='questionImagesView.header2' arguments="${languageName}"/></h2>
		<c:if test="${showImage}"> 
			<div class="leftLogo">
			<img src="<c:url value='/html/srvy/att/${subject.id}.html?l=${languageCode}&amp;ul=${uploadTime}'/>" alt="${toolTip}" title="${toolTip}"/>
			</div>
		</c:if>
		<div class="linkActions">
			<a href="<c:url value='/html/db/mgt/qatt/ul/${subject.id}.html?l=${languageCode}'/>"><spring:message code='questionImagesView.addLink'/><span class="hiddenLinkText"> <spring:message code='questionImagesView.addLink.hiddenText' arguments="${languageName}"/></span></a>
			<c:if test="${showImage}"> 
			<a href="<c:url value='/html/db/mgt/qatt/rm/${subject.id}.html?l=${languageCode}'/>"><spring:message code='questionImagesView.deleteLink'/><span class="hiddenLinkText"> <spring:message code='questionImagesView.deleteLink.hiddenText' arguments="${languageName}"/></span></a>
			</c:if>
		</div>
		                     
		<div class="toolTipText"> 
		<spring:message code='questionImagesView.editToolTip'/> ${toolTip}
		
		<c:if test="${showImage}"> 
		<a href="<c:url value='/html/db/mgt/qatt/tt/${subject.id}.html?l=${languageCode}'/>"><spring:message code='questionImagesView.editToolTipLink'/><span class="hiddenLinkText"> <spring:message code='questionImagesView.editToolTipLink.hiddenText' arguments="${languageName}"/></span> </a>
		</c:if>
		</div>
</c:forEach>
             
                            
<div class="bottomButtonBarLined"> 
<form action="<c:url value='${backUrl}'/>" method="get"><div>
<input type='submit' class='button' value="<spring:message code='back'/>"/>
</div></form>
</div> 
</div>
