<%@ include file="/WEB-INF/views/includes.jspf"%>
<c:set var="requiredIndicator" value=""/>
<c:if test="${question.required}">
<c:set var="requiredIndicator"><span class="requiredIndicator"> *</span></c:set>
</c:if>
