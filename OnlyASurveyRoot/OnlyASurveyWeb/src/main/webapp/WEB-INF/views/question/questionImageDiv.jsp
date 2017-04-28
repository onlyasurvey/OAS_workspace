<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:if test="${questionImage != null}">
<c:set var="payload" value="${questionImage}"/>
<div class="questionWithImage">
<img src="<c:url value='/html/srvy/att/${question.id}.html?dt=${payload.uploadTime.time}'/>"
	alt="<c:out value='${payload.altText}'/>"
	title="<c:out value='${payload.altText}'/>"
	/>
</div>
</c:if>