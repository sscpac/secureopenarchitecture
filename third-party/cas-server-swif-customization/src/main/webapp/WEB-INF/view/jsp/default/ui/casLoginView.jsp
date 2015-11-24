<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:directive.include file="includes/top.jsp" />

<!--  for logout
<div id="outerTop">
  <div id="mainTop">
  </div>
</div>

<div id="msgTop">
    <div id="logoutMsgHdr">Logged Out</div>
    <div id="logoutMsg">You have successfully logged out. Please enter your User ID and Password to log in again.</div>
</div>
 -->

<div id="outer">
  <div id="main">
    <div class="msgWindow messageContainer">
      <div class="msgWindow-tl">
        <div class="msgWindow-tr">
          <div class="msgWindow-tc">
            <div class="loginHeaderTxt">Login</div>
          </div>
        </div>
      </div>
      <div class="msgWindow-bwrap">
        <div class="msgWindow-ml">
          <div class="msgWindow-mr">
            <div class="msgWindow-mc">
              <div class="msgWindow-body">
                <form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
                  <div class="box" id="login">
                    <!-- <spring:message code="screen.welcome.welcome" /> -->
                    <h2>
                      <spring:message code="screen.welcome.instructions" />
                    </h2>
                    <div class="rowUser">
                      <label for="username">
                        <spring:message code="screen.welcome.label.netid" />
                      </label>
                      <c:if test="${not empty sessionScope.openIdLocalId}"> <strong>${sessionScope.openIdLocalId}</strong>
                        <input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
                      </c:if>
                      <c:if test="${empty sessionScope.openIdLocalId}">
                        <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                        <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
                      </c:if>
                    </div>
                    <div class="rowPwd">
                      <label for="password">
                        <spring:message code="screen.welcome.label.password" />
                      </label>
                      <%--
                            NOTE: Certain browsers will offer the option of caching passwords for a user.  There is a non-standard attribute,
                            "autocomplete" that when set to "off" will tell certain browsers not to prompt to cache credentials.  For more
                            information, see the following web page:
                            http://www.geocities.com/technofundo/tech/web/ie_autocomplete.html
                      --%>
                      <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
                      <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    </div>
                    <div class="row check">
                      <form:errors path="*" cssClass="errors" id="status" element="div" />
                      <input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
                      <label class="warnLabel" for="warn"><span class="warnText"><spring:message code="screen.welcome.label.warn" /></span></label>
                      <input type="hidden" name="lt" value="${flowExecutionKey}" />
                      <input type="hidden" name="_eventId" value="submit" />
                      
                      <input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" /> </div>
                  </div>
                </form:form>
              </div>
            </div>
          </div>
        </div>
        <div class="msgWindow-bl x-panel-nofooter">
          <div class="msgWindow-br">
            <div class="msgWindow-bc"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<jsp:directive.include file="includes/bottom.jsp" />
