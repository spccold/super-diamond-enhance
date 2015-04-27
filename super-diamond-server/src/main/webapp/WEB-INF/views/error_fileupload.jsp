<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${errorMsg != null}">
	<c:out value="${errorMsg}"/>
</c:if>
<c:if test="${errorMsg = null}">
	Excel Upload Error!
</c:if>