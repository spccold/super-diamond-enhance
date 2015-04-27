<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<a class="brand" href="/superdiamond/index">首页</a> >> 配置查询&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class="brand" href="/superdiamond/query/development">DEVELOPMENT</a>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class="brand" href="/superdiamond/query/test">TESE</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class="brand" href="/superdiamond/query/production">PRODUCTION</a>
<br/><br/> 

<form id ="queryForm" action='<c:url value="/query/build" />' >
	<input type="hidden" name="type" value='<c:out value="${type}"/>'/>
   	<input id="currentPage" type="hidden" name="page" value='<c:out value="${currentPage}"/>'/>
	<input type="text" class="form-control" name="moduleName" placeholder="模块名称">&nbsp;&nbsp;&nbsp;&nbsp; 
	<input type="text" class="form-control" name="configKey" placeholder="Key">&nbsp;&nbsp;&nbsp;&nbsp; 
	<input type="text" class="form-control" name="configValue" placeholder="Value">&nbsp;&nbsp;&nbsp;&nbsp; 
	<input type="text" class="form-control" name="configDesc" placeholder="配置描述">
</form>

<div id="clientInfosWin" style="width:700px" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  	<div class="modal-header">
    	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
    	<h3>客户端列表</h3>
  	</div>
  	<div class="modal-body">
    	<table class="table table-striped table-bordered">
		  	<thead>
		    	<tr>
		    		<th width="20%">客户端类型</th>
		    		<th width="20%">客户端地址</th>
		    		<th width="20%">连接时间</th>
		    		<th width="20%">最后心跳时间</th>
		    		<th width="20%">项目名称</th>
		    	</tr>
		  	</thead>
		  	<tbody id="showClientInfos"></tbody>
		</table>
  	</div>
</div>

<table class="table table-striped table-bordered">
	<thead>
		<tr>
			<th width="100">项目名称</th>
			<th width="50">Module</th>
			<th width="50">Key</th>
			<th>Value</th>
			<th width="100">描述</th>
			<th width="70">配置类型</th>
			<th width="50">可见性</th>
			<th width="45">操作人</th>
			<th width="120">操作时间</th>
			<th width="150">操作</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${configs}" var="config">
			<tr id='row-<c:out value="${config.CONFIG_ID}"/>'>
				<td value='<c:out value="${config.PROJ_NAME}"/>'><c:out
						value="${config.PROJ_NAME}" /></td>
				<td value='<c:out value="${config.MODULE_ID}"/>'><c:out
						value="${config.MODULE_NAME}" /></td>
				<td value='<c:out value="${config.CONFIG_KEY}"/>'><c:out
						value="${config.CONFIG_KEY}" /></td>
				<td title='<c:out value="${config.BUILD_VALUE}"/>'>
					<script type="text/javascript">
						var value = '<c:out value="${config.BUILD_VALUE}"/>';
						if (value.length > 30)
							document.write(value.substring(0, 30) + "...");
						else
							document.write(value);
					</script>
				</td>
				<td title='<c:out value="${config.CONFIG_DESC}"/>'>
					<script	type="text/javascript">
						var value = '<c:out value="${config.CONFIG_DESC}"/>';
						if (value.length > 15)
							document.write(value.substring(0, 15) + "...");
						else
							document.write(value);
					</script>
				</td>
				<td title='<c:out value="${config.CONFIG_TYPE}"/>'><c:out
						value="${config.CONFIG_TYPE}" /></td>
				<td title='<c:out value="${config.VISABLE_TYPE}"/>'><c:out
						value="${config.VISABLE_TYPE}" /></td>
				<td><c:out value="${config.OPT_USER}" /></td>
				<td><c:out value="${config.OPT_TIME}" /></td>
				<td>
					<button type="button"
							onclick="showClientInfos('${config.PROJ_ID}','${config.MODULE_NAME}');"
							class="btn btn-primary">客户端列表</button>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script type="text/javascript">
	$("body").keydown(function() {
		if (event.keyCode == "13") {//keyCode=13是回车键
			//置为第一页
			$("#currentPage").val(1);
			$("#queryForm").submit();
		}
	});
	//展示CONFIG客户端列表
	function showClientInfos(projectId,moduleName){
		//获取最新的客户端连接列表
		$.ajax({
			  url: '/superdiamond/profile/'+projectId+'/build/'+moduleName,
			  type: 'POST',
			  dataType: 'html',
			  success: function(data, textStatus, xhr) {
					//字符串转换成json对象
				    var clientInfos = eval('(' + data + ')');
				  	var text;
					for(var clientInfo in clientInfos){
						text += ('<tr><td>'+
						clientInfos[clientInfo].clientType
				        +'</td><td>'+
						clientInfos[clientInfo].address
				        +'</td><td>'+
						clientInfos[clientInfo].connTime
				        +'</td><td>'+
						clientInfos[clientInfo].lastConnTime
				        +'</td><td>'+
						clientInfos[clientInfo].projectName
				        +'</td></tr>')
					}
					//展现客户端连接列表
					$("#showClientInfos").html('');
					$("#showClientInfos").append(text);
			  }
		});

		$('#clientInfosWin').modal({
			backdrop: true
		})		 
	}
</script>