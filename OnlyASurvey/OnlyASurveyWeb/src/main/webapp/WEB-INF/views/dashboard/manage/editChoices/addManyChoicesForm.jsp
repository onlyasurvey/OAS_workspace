<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><c:out value="${question.displayTitle}"/> - <fmt:message key='addManyOptionsLabel'/></title>
<h1><c:out value="${question.displayTitle}"/></h1>
<h2><fmt:message key='addManyOptionsLabel'/></h2>

<%@ include file="/WEB-INF/views/formErrors.jsp"%>

<form:form action="${question.id}.html" method="post">
<div>
<%@ include file="/WEB-INF/views/dashboard/manage/questionManagement/details/multipleChoice.create.choiceList.jsp"%>
<input type='submit' class='button' name='_am' value="<fmt:message key='save'/>"/>
<input type='submit' class='button' name='_cancel' value="<fmt:message key='cancel'/>"/>
</div>
</form:form>