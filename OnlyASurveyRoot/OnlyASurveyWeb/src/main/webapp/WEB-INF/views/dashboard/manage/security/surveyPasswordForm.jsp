<%@ include file="/WEB-INF/views/includes.jspf"%>
<title><spring:message code='securityTab.passwordForm.pageTitle' /></title>

<div class="yui-g">
<h1><spring:message code='securityTab.passwordForm.pageTitle'/></h1>
<%@ include file="/WEB-INF/views/formErrors.jsp" %>
<p><spring:message code='securityTab.passwordForm.pageIntro'/></p> 

<form action="${survey.id}.html" method="post"> 
<fieldset class="twoColumnLayout"> 
<div> 
<label for='pw1'><spring:message code='securityTab.password'/></label> 
<input id='pw1' type="password" name='pw1' size="35" maxlength="55"/>             
</div> 

<div>
<label for='pw2'><spring:message code='securityTab.password2'/></label> 
<input id='pw2' type="password" name='pw2' size="35" maxlength="55"/>
</div>

<div>
<label>&nbsp;</label>
<input type='submit' class='button' name="_evas" value='<spring:message code='save'/>'/>
<input type='submit' class='button' name="_lecnac" value='<spring:message code='cancel'/>'/>
</div>
</fieldset>
</form>          
</div>