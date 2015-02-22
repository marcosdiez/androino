<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="org.androino.prototype.server.*" %>

<html>
  <head>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
<script type="text/javascript">
function formSubmit(btn)
{
fo = document.getElementById("frm1");
bt = document.getElementById("btn1");
bt.value = btn;
fo.submit();
}
</script>

  </head>

  <body>
 Hola
  
<hr/>
  <%=  EventQueue.getInstance().debugInfo()%>
<hr/>

<p>
Select the user and click to simulate button event
</p>
User:
<form id="frm1" action="new_event">
<input id="btn1" type="hidden" name="MSG" value="B_0">
<select name="USER">
  <option value="A">A</option>
  <option value="B">B</option>
</select>
</form>


<form action="#">
<input type="button" onclick="formSubmit('B_1')" value="0" />
<input type="button" onclick="formSubmit('B_2')" value="X" />
<input type="button" onclick="formSubmit('B_3')" value="O" />
<br/>
<input type="button" onclick="formSubmit('B_4')" value="X" />
<input type="button" onclick="formSubmit('B_5')" value="O" />
<input type="button" onclick="formSubmit('B_6')" value="X" />
<br/>
<input type="button" onclick="formSubmit('B_7')" value="0" />
<input type="button" onclick="formSubmit('B_8')" value="X" />
<input type="button" onclick="formSubmit('B_9')" value="O" />
<br/>
</form>


</body>
</html>  