<%@ page import="frontlinesms2.Webconnection" %>
<tr class="prop web-connection-parameter">
	<td>
		<g:textField name="param-name" class="param-name customactivity-field" value='${name}' required="true"/>
	</td>
	<td>
		<g:textField name="param-value" class="param-value customactivity-field" value='${value}'/>
	</td>
	<td>
		<fsms:magicWand target="param-value" fields="${Webconnection.subFields*.key}" onchange='webconnectionDialog.handlers.autofillValue(this);'/>
	</td>
	<td>
		<a onclick="webconnectionDialog.handlers.removeRule(this)" class="remove-command" />
	</td>
</tr>

