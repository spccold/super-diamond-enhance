<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<span class="label label-success"><c:out value="${moduleName}"/></span>
<div class="pull-right">
	<button type="button" id="addConfig" class="btn btn-primary">添加配置</button>
</div>
</br>
</br>

<div id="addConfigWin" style="width: 700px" class="modal hide fade"
	tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
	aria-hidden="true">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal"
			aria-hidden="true">×</button>
		<h3 id="myModalLabel">模版配置</h3>
	</div>
	<div class="modal-body">
		<form id="configForm" class="form-horizontal"
			action='<c:url value="/moduleManage/detail/save" />' method="post">
			<div class="control-group">
				<label class="control-label">Config Key：</label>
				<div class="controls">
					 <input type="text" name="configKey" class="input-xxlarge" id="config-configKey">
					 <input type="hidden" name="flag" id="config-flag"/> 
		      		 <input type="hidden" name="moduleName" value="<c:out value="${moduleName}"/>">
		      		 <input type="hidden" name="edit" id="editFlag">
		      		 <input type="hidden" name="oldKey" id="config-configKey-Old">
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
		<button class="btn btn-primary" id="saveConfigExt">保存继续添加</button>
	</div>
</div>

<table class="table table-striped table-bordered">
	<thead>
		<tr>
			<th width="20%">Key</th>
			<th width="25%">Value</th>
			<th width="40%">描述</th>
			<th width="15%">操作</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${moduleDetails}" var="detail">
			<tr>
				<td value='<c:out value="${detail.CONFIG_KEY}"/>'><c:out
						value="${detail.CONFIG_KEY}" /></td>
				
				<td title='<c:out value="${detail.CONFIG_VALUE}"/>'>
					<script	type="text/javascript">
						var value = '<c:out value="${detail.CONFIG_VALUE}"/>';
						if (value.length > 30)
							document.write(value.substring(0, 30) + "...");
						else
							document.write(value);
					</script>
				</td>
				<td title='<c:out value="${detail.CONFIG_DESC}"/>'>
					<script	type="text/javascript">
						var value = '<c:out value="${detail.CONFIG_DESC}"/>';
						if (value.length > 15)
							document.write(value.substring(0, 15) + "...");
						else
							document.write(value);
					</script>
				</td>
				<td>
					<a class="deleteConfig" href='/superdiamond/moduleManage/detail/delete?moduleName=<c:out value="${moduleName}"/>&configKey=<c:out value="${detail.CONFIG_KEY}"/>' title="删除"><i class="icon-remove"></i></a>&nbsp;&nbsp;
                  	<a href='javascript:updateConfig("<c:out value="${detail.CONFIG_KEY}"/>","<c:out value="${detail.CONFIG_VALUE}"/>","<c:out value="${detail.CONFIG_DESC}"/>")' title="更新"><i class="icon-edit"></i></a>&nbsp;&nbsp;&nbsp;&nbsp;
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<c:if test="${message != null}">
	<div class="alert alert-error clearfix" style="margin-bottom: 5px;width: 400px; padding: 2px 15px 2px 10px;">
		${message}
	</div>
</c:if>

<script type="text/javascript">
function updateConfig(configKey,configValue,configDesc) {
	$("#config-configKey").val(configKey);
	$("#config-configKey-Old").val(configKey);
	$("#config-configValue").val(configValue);
	$("#config-configDesc").val(configDesc);
	$("#editFlag").val("edit");
	
	$('#addConfigWin').modal({
		backdrop: false
	})
}


	$(document).ready(function() {

		$("#addConfig").click(function(e) {
			$('#addConfigWin').modal({
				backdrop: true
			})
		});
		//保存模版详情
		$("#saveConfig").click(function(e) {
			if(!$("#config-configKey").val()) {
				$("#configTip").text("configKey不能为空");
			} else if(!$("#config-configValue").val()) {
				$("#configTip").text("configValue不能为空");
			} else {
				$("#configForm")[0].submit();
			}
		});
		//保存并继续添加配置
		$("#saveConfigExt").click(function(e) {
			$("#config-flag").val("con");
			if(!$("#config-configKey").val()) {
				$("#configTip").text("configKey不能为空");
			} else if(!$("#config-configValue").val()) {
				$("#configTip").text("configValue不能为空");
			} else {
				$("#configForm")[0].submit();
			}
		});
		
		//触发 继续添加的事件
		var flag = "<%= request.getParameter("flag")%>";
		if(flag == "con") {
			$("#addConfig").click();
		}
		
	});
</script>