<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>SANE Stack Manager</title>
		<link rel='stylesheet' type='text/css'
			href='/ExtJS/ext-2.0.1/resources/css/ext-all.css' />
		<link rel='stylesheet' type='text/css'
			href='/ExtJS/ext-2.0.1/resources/css/xtheme-gray.css' />
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache">
		<meta http-equiv="expires" content="0">


		<script type='text/javascript' src='dwr/interface/GuildInfoService.js'></script>
		<script type='text/javascript' src='dwr/engine.js'></script>

		<script type='text/javascript'
			src='/ExtJS/ext-2.0.1/adapter/ext/ext-base.js'></script>
		<script type='text/javascript' src='/ExtJS/ext-2.0.1/ext-all.js'></script>


		<script type='text/javascript'>



</script>

	</head>
	<body>
		<h1>
			SANE Stack - Manager
		</h1>
		<p style='padding: 2em; font-style: italic;'>
			From here you can manage applications that have you listed as an Application Admin. 
		</p>
		<c:foreach>
		
		</c:foreach>
	</body>
</html>
