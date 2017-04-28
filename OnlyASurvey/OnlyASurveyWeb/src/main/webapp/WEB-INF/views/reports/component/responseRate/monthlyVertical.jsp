<%@ include file="/WEB-INF/views/includes.jspf"%>
<table class="responseResults" width="100%" border="0"> 
  <tr class="tableHeading"> 
    <th scope="col">&nbsp;</th>
    <c:forEach items="${byMonth}" var="item">
    <th scope="col"><fmt:message key='month${item.id.date.month}'/></th>
    </c:forEach>
    <th scope="col"><fmt:message key='report.total'/></th> 
  </tr> 
  <tr> 
    <th><fmt:message key='report.responses' /></th>
    <c:set var="total" value="0"/>
    <c:forEach items="${byMonth}" var="item">
    	<td>${item.count}</td>
    	<c:set var="total" value="${total + item.count}"/>
    </c:forEach>
    <td>${total}</td> 
  </tr> 
</table>