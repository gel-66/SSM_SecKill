<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!-- 用于格式化输出时间 -->
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>