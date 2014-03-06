<table border="1">
	<g:each in="${umaInfoList}" var="umaInfo">
		<tr>
			<td>
				<g:set var="umaStr" value="${umaInfo.getJun() + '着'}" />
				<g:set var="umaStr" value="${umaStr + '(' + umaInfo.getNinki() + '人気)'}" />
				<g:set var="umaStr" value="${umaStr + ' ' + umaInfo.getUmaban()}" />
				<g:set var="umaStr" value="${umaStr + ' ' + umaInfo.getUma()}" />
				<div class="umaIdx" id="${umaInfo.getUmaban()}">${umaStr}</div>
			</td>
		</tr>
	</g:each>
</table>