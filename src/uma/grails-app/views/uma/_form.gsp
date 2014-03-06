<%@ page import="uma.Uma" %>



<div class="fieldcontain ${hasErrors(bean: umaInstance, field: 'str1', 'error')} ">
	<label for="str1">
		<g:message code="uma.str1.label" default="Str1" />
		
	</label>
	<g:textField name="str1" value="${umaInstance?.str1}"/>
</div>

