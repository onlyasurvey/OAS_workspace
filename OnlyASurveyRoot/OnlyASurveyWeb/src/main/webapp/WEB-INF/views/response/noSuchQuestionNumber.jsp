<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key='response.questionNumberNotFound.pageTitle'/></title>
<h1><fmt:message key='response.questionNumberNotFound.pageTitle'/></h1>
<p><a href="<c:url value='/html/res/${response.id}.html'/>"/><fmt:message key='response.questionNumberNotFound.goToStart'/></a></p>
<div class='errorMessage'><fmt:message key='response.questionNumberNotFound.pageIntro'/></div>
