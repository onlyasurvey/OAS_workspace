<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><fmt:message key="publish.error.paymentNeeded.pageTitle"/></title>
<h1><fmt:message key="publish.error.paymentNeeded.pageTitle"/></h1>
<p><fmt:message key="publish.error.paymentNeeded.pageIntro" /></p>

<form action="https://www.sandbox.paypal.com/cgi-bin/webscr" method="post">
<div>
<input type="hidden" name="cmd" value="_s-xclick">
<input type="hidden" name="item_name" value="<fmt:message key='buy.itemName.prefix'/>: <c:out value='${survey.displayTitle}'/>">
<input type="hidden" name="hosted_button_id" value="1949">
<input type="hidden" name="notify_url" value="${notifyUrl}"/>
<%--
--%>
<input type="image" src="https://www.sandbox.paypal.com/en_US/i/btn/btn_buynowCC_LG.gif" border="0" name="submit"
alt="<fmt:message key='payPalLogoAlt"'/>" title="<fmt:message key='payPalLogoAlt"'/>" />
</div>
</form>

<br/><br/><br/><br/>
<div>
<form action="<c:url value='/html/db/pub/fakePaymentProcessor/${survey.id}.html'/>" method="post">
<input type='submit' value='Fake Pay Now Button'/>
</div>
</form>