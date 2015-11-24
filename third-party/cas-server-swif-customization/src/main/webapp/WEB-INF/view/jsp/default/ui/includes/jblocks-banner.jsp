<%@ taglib prefix="jblocks" uri="http://jblocks.proj.ozone/tags" %>

<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/jblock-style.css"/>
<jblocks:banner
cssClass="classificationBanner"
dynamic="true"
dynamicInLine="true" 
beanScope="request"
bean="pageClassification"
abbreviated="false"
noDeclass="true" />