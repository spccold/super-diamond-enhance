<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<a class="brand" href="/superdiamond/index">首页</a>
>>
<b><c:out value="${project.PROJ_NAME}" /> - <c:out value="${type}" /></b>
<br />
<br />

<b>模块：</b>
<select id="sel-queryModule">
	<option value="">全部</option>
	<c:forEach items="${modules}" var="module">
		<option value='<c:out value="${module.MODULE_ID}"/>'><c:out
				value="${module.MODULE_NAME}" /></option>
	</c:forEach>
</select>
<!-- <button type="button" id="queryModule" class="btn btn-primary">查询</button> -->

<div class="pull-right">
	<button type="button" class="btn btn-primary" onclick="exportAllConfigs(<c:out value="${projectId}"/>);">导出配置</button>
	<button type="button" id="preview" class="btn btn-primary">预览</button>
</div>

<div id="addConfigWin" style="width: 700px" class="modal hide fade"
	tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal"
			aria-hidden="true">×</button>
		<h3 id="myModalLabel">参数配置</h3>
	</div>
	<div class="modal-body">
		<form id="configForm" class="form-horizontal"
			action='<c:url value="/config/save" />' method="post">
			<div class="control-group">
				<label class="control-label">模块：</label>
				<div class="controls">
					<select class="input-xxlarge" id="config-moduleId"
						disabled="disabled">
						<option value="">请选择...</option>
						<c:forEach items="${modules}" var="module">
							<option value='<c:out value="${module.MODULE_ID}"/>'><c:out
									value="${module.MODULE_NAME}" /></option>
						</c:forEach>
					</select>
				</div>
				<label class="control-label">资源类型：</label>
    			<div class="controls">
    				<select class="input-xxlarge" name="configType" id="config-configType" disabled="disabled">
						<option value="CONFIG">CONFIG</option>
						<option value="DRM">DRM</option>
					</select>
    			</div>
    			<label class="control-label">可见性：</label>
    			<div class="controls">
    				<select class="input-xxlarge" name="visableType" id="config-visableType" disabled="disabled">
						<option value="PUBLIC">PUBLIC</option>
						<option value="PRIVATE">PRIVATE</option>
					</select>
    			</div>
				<label class="control-label">Config Key：</label>
				<div class="controls">
					<input type="hidden" name="configKey" id="config-configKey-ext" />
					<input type="hidden" name="moduleId" id="config-moduleId-ext" /> <input
						type="hidden" name="configId" id="config-configId" /> <input
						type="hidden" name="projectId"
						value='<c:out value="${projectId}"/>' /> <input type="hidden"
						name="type" value='<c:out value="${type}"/>' /> <input
						type="hidden" name="page" value='<c:out value="${currentPage}"/>' />
					<input type="hidden" name="selModuleId"
						value='<c:out value="${moduleId}"/>' /> <input type="text"
						class="input-xxlarge" id="config-configKey" disabled="disabled">
				</div>
				<label class="control-label">Config Value：</label>
				<div class="controls">
					<textarea rows="8" name="configValue" class="input-xxlarge"
						id="config-configValue"></textarea>
				</div>
				<label class="control-label">描述：</label>
				<div class="controls">
					<textarea rows="2" class="input-xxlarge" name="configDesc"
						id="config-configDesc"></textarea>
				</div>
			</div>
		</form>
	</div>
	<div class="modal-footer">
		<span id="configTip" style="color: red"></span>
		<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
		<button class="btn btn-primary" id="saveConfig">保存</button>
	</div>
</div>
<!-- add by kanguangwen -->
<div id="pushConfigWin" style="width: 700px" class="modal hide fade"
	tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal"
			aria-hidden="true">×</button>
		<h5 id="myModalLabel">
			MODULE:<font color="read"><span id="moduleValue"></span></font> KEY:<font
				color="read"><span id="keyValue"></span></font> VALUE:<font
				color="read"><span id="globalValue"></span></font> newValue:&nbsp;<input
				type="text" id="newValue" />
		</h5>
	</div>
	<div class="modal-body">
		<table class="table table-striped table-bordered">
			<thead>
				<tr>
					<th width="60">客户端类型</th>
		    		<th width="90">客户端地址</th>
		    		<th width="90">连接时间</th>
		    		<th width="90">最后心跳时间</th>
		    		<th width="50">项目名称</th>
		      		<th width="50">操作</th>
				</tr>
			</thead>
			<tbody id="clientInfos"></tbody>
		</table>
	</div>
</div>
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
			<th width="50">Module</th>
      		<th width="50">Key</th>
      		<th>Value</th>
      		<th width="100">描述</th>
      		<th width="70">配置类型</th>
      		<th width="50">可见性</th>
      		<th width="45">操作人</th>
      		<th width="120">操作时间</th>
      		<th width="200">操作</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${configs}" var="config">
			<tr id='row-<c:out value="${config.CONFIG_ID}"/>'>
				<td value='<c:out value="${config.MODULE_ID}"/>'><c:out
						value="${config.MODULE_NAME}" /></td>
				<td value='<c:out value="${config.CONFIG_KEY}"/>'><c:out
						value="${config.CONFIG_KEY}" /></td>
				<c:if test="${!isAdmin && !isOwner && config.VISABLE_TYPE == 'PRIVATE'}">
	               	<td title='******' >
	                  	******
	               	</td>
               	</c:if>
               	<c:if test="${isAdmin || isOwner || config.VISABLE_TYPE == 'PUBLIC'}">
		           	<td title='<c:out value="${config.PRODUCTION_VALUE}"/>'>
		           		<script type="text/javascript">
						var value = '<c:out value="${config.PRODUCTION_VALUE}"/>';
						if (value.length > 30)
							document.write(value.substring(0, 30) + "...");
						else
							document.write(value);
						</script>
					</td>
               	</c:if>
				<td title='<c:out value="${config.CONFIG_DESC}"/>'><script
						type="text/javascript">
					var value = '<c:out value="${config.CONFIG_DESC}"/>';
					if (value.length > 15)
						document.write(value.substring(0, 15) + "...");
					else
						document.write(value);
				</script></td>
				<td title='<c:out value="${config.CONFIG_TYPE}"/>'>
                  	<c:out value="${config.CONFIG_TYPE}"/>
               	</td>
               	<td title='<c:out value="${config.VISABLE_TYPE}"/>'>
                  	<c:out value="${config.VISABLE_TYPE}"/>
               	</td>
				<td><c:out value="${config.PRODUCTION_USER}" /></td>
				<td><c:out value="${config.PRODUCTION_TIME}" /></td>
				<td>
					<c:if test="${isOwner}">
						<a href='javascript:updateConfig(<c:out value="${config.CONFIG_ID}"/>)'
						title="更新"><i class="icon-edit"></i></a>&nbsp;&nbsp; 
						<!-- add by kanguangwen -->
						<c:if test="${config.CONFIG_TYPE == 'DRM'}">
							<button type="button"
								onclick="pushGlobalConfig('${projectId}','${config.MODULE_NAME}','${config.CONFIG_KEY}');"
								class="btn btn-primary">全推</button>&nbsp;&nbsp;&nbsp;&nbsp;
							<button type="button"
								onclick="pushPartConfig('${projectId}','${config.MODULE_NAME}','${config.CONFIG_KEY}','${config.PRODUCTION_VALUE}');"
								class="btn btn-primary">局推</button>
						</c:if>
						<c:if test="${config.CONFIG_TYPE == 'CONFIG'}">
							<button type="button"
								onclick="showClientInfos('${projectId}','${config.MODULE_NAME}');"
								class="btn btn-primary">客户端列表</button>
						</c:if>
					</c:if>
					<c:if test="${!isOwner}">
						<button type="button"
								onclick="showClientInfos('${projectId}','${config.MODULE_NAME}');"
								class="btn btn-primary">客户端列表</button>
					</c:if>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<script type="text/javascript">
	function updateConfig(id) {
		var tds = $("#row-" + id + " > td");
		$("#config-moduleId").val($(tds.get(0)).attr("value"));
		$("#config-configKey").val($(tds.get(1)).attr("value"));
		$("#config-moduleId-ext").val($(tds.get(0)).attr("value"));
		$("#config-configKey-ext").val($(tds.get(1)).attr("value"));
		$("#config-configValue").val($(tds.get(2)).attr("title"));
		$("#config-configDesc").val($(tds.get(3)).attr("title"));
		$("#config-configType").val($(tds.get(4)).attr("title"));
		$("#config-visableType").val($(tds.get(5)).attr("title"));
		$("#config-configId").val(id);

		$('#addConfigWin').modal({
			backdrop : false
		})
	}

	//add by kanguangwen
	//打开推送客户端列表
	function pushPartConfig(projectId, moduleName, configKey, configValue) {
		var pushInfo = "'" + projectId + "'," + "'production'," + "'"
				+ moduleName + "'," + "'" + configKey + "'";
		var temp;
		//获取最新的客户端连接列表
		$.ajax({
					url : '/superdiamond/profile/' + projectId + '/production/'
							+ moduleName,
					type : 'POST',
					dataType : 'html',
					success : function(data, textStatus, xhr) {
						//字符串转换成json对象
						var clientInfos = eval('(' + data + ')');
						var text;
						for ( var clientInfo in clientInfos) {
							temp = pushInfo;
							temp += (",'" + clientInfos[clientInfo].address + "'")
							text += ('<tr><td>'
									+ clientInfos[clientInfo].clientType
									+ '</td><td>'
									+ clientInfos[clientInfo].address
									+ '</td><td>'
									+ clientInfos[clientInfo].connTime
									+ '</td><td>'
									+ clientInfos[clientInfo].lastConnTime
									+ '</td><td>'
									+ clientInfos[clientInfo].projectName
									+ '</td><td><button class=\"btn btn-primary\" onclick=\"pushAction('
									+ temp + ');\">推送</button></td></tr>')
						}
						//展现客户端连接列表
						$("#clientInfos").html('');
						$("#clientInfos").append(text);
					}
				});
		$("#moduleValue").text(moduleName);
		$("#keyValue").text(configKey);
		$("#globalValue").text(configValue);

		$('#pushConfigWin').modal({
			backdrop : true
		})
	}
	//展示CONFIG客户端列表
	function showClientInfos(projectId,moduleName){
		//获取最新的客户端连接列表
		$.ajax({
			  url: '/superdiamond/profile/'+projectId+'/production/'+moduleName,
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
	//向客户端推送单个配置项
	function pushAction(projectId,profile,moduleName,configKey,clientAddress){
		if($("#newValue").val() == ''){
			alert('请输入要推送的数据!');
			return;
		}
	    bootbox.confirm("确认推送? 推送后<font color=\"read\">"+clientAddress+"</font>所对应的客户端数据将被替换为<font color=\"read\">"+$("#newValue").val()+"</font>", function(confirmed) {
	    	if(confirmed){
	    		$.ajax({
	    			  url: '/superdiamond/partPush/'+projectId+'/production/'+moduleName,
	    			  type: 'POST',
	    			  dataType: 'html',
	    			  data: {clientAddress: clientAddress,configKey:configKey,configValue: $("#newValue").val()},
	    			  success: function(data, textStatus, xhr) {
	    					//字符串转换成json对象
	    					if(data != ''){
	    						//简单的alert出推送失败的服务器信息
	    						alert(data);
	    					}else{
	    						alert('推送触发成功!');
	    					}
	    			  }
	    		});
	    	}
	    });
	    return false;
	}


	function pushGlobalConfig(projectId,moduleName,configKey){
		 bootbox.confirm("确认全局推送? 推送后订阅<font color=\"read\">"+configKey+"</font>的所有客户端数据将被替换！", function(confirmed) {
		    	if(confirmed){
		    		$.ajax({
		    			  url: '/superdiamond/globalPush/'+projectId+'/production/'+moduleName,
		    			  type: 'POST',
		    			  dataType: 'html',
		    			  data: {configKey: configKey},
		    			  success: function(data, textStatus, xhr) {
		    					//字符串转换成json对象
		    					if(data != ''){
		    						//简单的alert出推送失败的服务器信息
		    						alert(data);
		    					}else{
		    						alert('推送触发成功!');
		    					}
		    			  }
		    		});
		    	}
		    });
		    return false;
	}
	//导出配置到Excel文件
	function exportAllConfigs(projectId){
		window.location.href="/superdiamond/exportAllConfig/production?projectId="+projectId;
	}
	$(document)
			.ready(
					function() {
						$("#sel-queryModule").val(<c:out value="${moduleId}"/>);

						$("#preview")
								.click(
										function(e) {
											window.location.href = '/superdiamond/profile/preview/<c:out value="${project.PROJ_CODE}"/>/<c:out value="${type}"/>?projectId=<c:out value="${projectId}"/>';
										});

						$("#sel-queryModule")
								.change(
										function(e) {
											var moduleId = $("#sel-queryModule")
													.val();
											var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>';
											if (moduleId)
												url = url + "?moduleId="
														+ moduleId;

											window.location.href = url;
										});

						$("#queryModule")
								.click(
										function(e) {
											var moduleId = $("#sel-queryModule")
													.val();
											var url = '/superdiamond/profile/<c:out value="${type}"/>/<c:out value="${projectId}"/>?projectId=<c:out value="${projectId}"/>';
											if (moduleId)
												url = url + "?moduleId="
														+ moduleId;

											window.location.href = url;
										});

						$("#addConfig").click(function(e) {
							$('#addConfigWin').modal({
								backdrop : true
							})
						});

						$("#saveConfig").click(function(e) {
							if (!$("#config-configValue").val()) {
								$("#configTip").text("configValue不能为空");
							} else {
								$("#configForm")[0].submit();
							}
						});
					});
</script>