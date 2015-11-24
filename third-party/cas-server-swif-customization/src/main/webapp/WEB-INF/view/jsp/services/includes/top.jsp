<%@ page session="true" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
	<head>
	    <title>CAS &#8211; Central Authentication Service Test Logout</title>
	    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	    
		<style type="text/css">
			<!--			
			div#outerTop {
				width: 430px;
				height:90px;
				/*margin-top: 10px;*/
				position:absolute;
				top:170px;
				left:50%;
				margin-bottom: 50px;
				margin-left: -215px;
				margin-right: auto;
				padding: 0px;
				border:0px;
				-moz-opacity:0.5;
				opacity:.50;
				filter:alpha(opacity=50);
				background-color:#000000;
			}
			
			div#mainTop {
				margin-left: 0%;
				margin-top: 1px;
				padding: 10px;
			}
			
			div#msgTop {
				width: 430px;
				height:90px;
				margin-top: 0px;
				position:absolute;
				top:180px;
				left:50%;
				margin-bottom: 50px;
				margin-left: -215px;
				margin-right: auto;
				padding: 0px;
				border:0px;
			}
			-->
		</style>
		
		<style type="text/css" media="screen">@import 'css/logout.css'/**/;</style>
        
	    <!--[if lte IE 6]>
        	<style type="text/css" media="screen">@import 'css/ie_logout.css';</style>
        <![endif]-->
        
	    <script type="text/javascript" src="js/common_rosters.js"></script>
	</head>

	<body id="cas" onload="init();">  
	    <div id="content">
