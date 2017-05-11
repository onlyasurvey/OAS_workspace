<%@ include file="config/includes.jspf"%>
${beforeContent}
<c:choose>
<c:when test="${template.clf2Template}">
	<div class="center">
	<div class="OAS_survey">
	<decorator:body/>
	</div>
	</div>
</c:when>
<c:otherwise>
	<div class="OAS_survey">
	<decorator:body/>
	</div>
</c:otherwise>
</c:choose>
${afterContent}
