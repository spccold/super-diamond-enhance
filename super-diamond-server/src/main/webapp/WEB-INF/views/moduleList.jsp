<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div id="modulePreviewWin" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    	<h3 id="myModalLabel">${moduleName}预览</h3>
  	</div>
  	<div class="modal-body">
    	<textarea style="width: 500px; height: 300px; font-size: 12px; line-height: 16px;" id="previewTextArea"></textarea>
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
					<button type="button" onclick="modulePreview('${moduleName}')" class="btn btn-primary">预览</button>
					<button type="button" onclick="moduleImport('<c:out value="${projectId}"/>','${moduleName}')" class="btn btn-primary">导入</button>
               	</td>
            </tr>
     	</c:forEach>
	</tbody>
</table>
<div id="paginator"></div>

<script type="text/javascript">
//子窗口关闭，刷新父窗口
window.onunload = function(){ 
	window.opener.location.reload(); 
} 

function moduleImport(projectId,moduleName){
	$.ajax({
		url: '/superdiamond/moduleManage/importModule',
		type: 'POST',
		dataType: 'html',
		data: {projectId: projectId,moduleName:moduleName},
	})
	.done(function(data) {
		alert(data);
	});
	
}

function modulePreview(moduleName){
	$.ajax({
		url: '/superdiamond/moduleManage/preview',
		type: 'POST',
		dataType: 'html',
		data: {moduleName:moduleName},
	})
	.done(function(data) {
		$("#previewTextArea").html("");
		$("#previewTextArea").append(data);
	});
	
	$('#modulePreviewWin').modal({
		backdrop: true
	})		
}
</script>