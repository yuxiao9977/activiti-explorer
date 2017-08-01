<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<input id="test1" /><button onclick="writeback()">确定</button>
	<script type="text/javascript">
		function writeback() {
			var v = document.getElementById("test1").value;
			window.opener.scope_properties.property.value = v;
			window.opener.document.getElementById("pro_text").value = v;
			window.close();
		}
	</script>
</body>
</html>