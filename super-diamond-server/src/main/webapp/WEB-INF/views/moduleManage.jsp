<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<input type="hidden" value="moduleId" id="pageId" />
<!-- 居右 -->
<div class="pull-right">
	<button type="button" id="addModule" class="btn btn-primary">添加模版</button>
</div>
<div id="addModalWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    	<h3 id="myModalLabel">添加Module</h3>
  	</div>
  	<div class="modal-body">
    	<form id="moduleForm" class="form-horizontal" action='<c:url value="/moduleManage/save" />' method="post">
  			<div class="control-group">
    			<label class="control-label">名称：</label>
    			<div class="controls">
      				<input type="text" id="addModuleName" name=moduleName class="input-large"> <span id="addTip" style="color: red"></span>
    			</div>
  			</div>
		</form>
  	</div>
  	<div class="modal-footer">
    	<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    	<button class="btn btn-primary" id="saveModule">保存</button>
  	</div>
</div>

<table class="table table-bordered table-striped">
	<caption>所有模版</caption>
  	<thead>
    	<tr>
    		<th width="75%">模块名称</th>
    		<th width="25%">模块编辑</th>
    	</tr>
  	</thead>
  	<tbody>
    	<c:forEach items="${modules}" var="moduleName">
       		<tr>
       			<td>
                  	<c:out value="${moduleName}"/>
               	</td>
               	<td>
	               	<a class="deleteConfig" href='/superdiamond/moduleManage/delete?moduleName=<c:out value="${moduleName}"/>' title="删除"><i class="icon-remove"></i></a>&nbsp;&nbsp;
                  	<a href='javascript:templateDetail("<c:out value="${moduleName}"/>")' title="更新"><i class="icon-edit"></i></a>&nbsp;&nbsp;&nbsp;&nbsp;
               	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>
<div id="paginator"></div>

<c:if test="${sessionScope.message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${sessionScope.message}
	</div>
</c:if>

<script type="text/javascript">

function templateDetail(moduleName){
	window.open('/superdiamond/moduleManage/detail?moduleName='+moduleName);
	return false;
}

var options = {
    	size: "small",
    	alignment:"right",
    	totalPages: <c:out value="${totalPages}"/>,
        currentPage: <c:out value="${currentPage}"/>,
        pageUrl: function(type, page, current){
            return "/superdiamond/moduleManage/index?page="+page;
        }
    }
$('#paginator').bootstrapPaginator(options);
    
$(document).ready(function () {
	//打开添加模块的窗口
	$("#addModule").click(function(e) {
		$('#addModalWin').modal({
			keyboard: false
		})
	});
	
	//保存模块
	$("#saveModule").click(function(e) {
		if(!$("#addModuleName").val()) {
			$("#addTip").text("不能为空");
		} else {
			$("#moduleForm")[0].submit();
		}
	});
});
</script>