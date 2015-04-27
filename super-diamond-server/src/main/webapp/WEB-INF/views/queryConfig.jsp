<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<input type="hidden" value="queryId" id="pageId" />

<table class="table table-bordered table-striped">
	<caption>Profiles</caption>
	<thead>
		<tr>
			<th width="100"><a href='./query/development'>development</a></th>
			<th width="100"><a href='./query/test'>test</a></th>
			<th width="100"><a href='./query/build'>build</a></th>
			<th width="100"><a href='./query/production'>production</a></th>
		</tr>
	</thead>
</table>
