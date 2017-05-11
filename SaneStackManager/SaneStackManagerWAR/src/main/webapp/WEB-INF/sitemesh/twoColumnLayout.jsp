<%--
  - Description: Sitemesh decorator.
  - Author(s):   Hugh Willson, Andrew Pitt
  --%>

<%@ include file="config/includes.jspf" %>

<!DOCTYPE 
    html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
    
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <title>
      <fmt:message key="jsp.default.title"/> -
      <decorator:title/>
    </title>
    <%@ include file="headFoot/head.jspf" %>
    <%@ include file="headFoot/2colstyle.jspf" %>
    <decorator:head/>
  </head>
  <body>
    <div class="page">
      <div class="core">
        <%@ include file="headFoot/header.jspf" %>
        <%@ include file="breadcrumb/breadcrumb.jspf" %>
        <div class="colLayout">
          <div class="left">
            <%@ include file="sidenav/sidenav.jspf" %>
          </div>
          <div class="center">
              <decorator:body/>
          </div>
        </div>
        <%@ include file="headFoot/footer.jspf" %>
      </div>
    </div>
  </body>
</html>