<html>
<head>
<title>uma</title>
<%@ page import="uma.JraWeb" %>
<meta name="layout" content="main">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="${resource(dir: 'js', file: 'jquery.js')}"></script>
<style type="text/css" >
	body {background-color:#A0FFFF;}
    .raceIdx { font-weight: bold; cursor: pointer; margin:0;}
    .umaIdx { font-size: 80%; cursor: pointer; margin:0;}
    .button { background-color:#EEEEEE; border-style:outset; border-width:3px}
</style>
<script type="text/javascript" >
$(document).ready(function(){
    $('.raceIdx').click(function(){
        var race = $(this).next('.race');
        var fileName = race.attr("id");
        if(race.text().length == 0) {
	        $.ajax({
	            url: '/uma/uma/show',
	            data: {fileName:fileName},
	            async: false,
	            type: "POST",
	            dataType: "text",
	            success: function (data) {
	                race.html(data);
	            }
	        });
        }
        race.stop(true, true).slideToggle(0);
    });
});
</script>
</head>
<body>
	<%--
	<form method="post" action="show"><input type="text" name="input" value="${value}"> <input type="submit"></form>
	 --%>
	<input class="button" type="button" value="未勝利データ" name="misyori" onclick=""/>
	<table border="1">
	<g:each in="${raceInfoList}" var="raceInfo">
	    <tr>
	        <td>
	        	<g:set var="raceStr" value="${raceInfo.getDate().replaceAll("_", "/")}"/>
	        	<g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getPlace()}"/>
	        	<g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getR() + 'R'}"/>
	        	<g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getStartTime()}"/>
	        	<%-- <g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getCond()}"/> --%>
	        	<%-- <g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getLen()}"/> --%>
	        	<g:set var="raceStr" value="${raceStr + ' ' + raceInfo.getJyoken()}"/>
	        	<div class="raceIdx" id="${raceInfo.getFileName()}">${raceStr}</div>
	        	<div class="race" id="${raceInfo.getFileName()}" style="display: none;"></div>
	        </td>
        </tr>
	</g:each>
	</table>
</body>
</html>