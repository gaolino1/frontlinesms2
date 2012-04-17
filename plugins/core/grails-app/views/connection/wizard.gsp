<%@ page contentType="text/html;charset=UTF-8" import="frontlinesms2.Fconnection" %>
<g:javascript library="jquery" plugin="jquery"/>
<jqui:resources theme="medium" plugin="randomtexttosolvebug"/>
<g:javascript>
	url_root = "${request.contextPath}/";
</g:javascript>
<g:javascript src="application.js"/>
<g:javascript src="mediumPopup.js"/>
<div id="tabs" class="vertical-tabs">
	<div class="error-panel hide"><div id="error-icon"></div><g:message code="connection.validation.prompt" /></div>
	<ol>
		<g:if test="${!fconnectionInstance}">
			<li><a href="#tabs-1"><g:message code="connection.type" /></a></li>
		</g:if>
		<li><a href="#tabs-2"><g:message code="connection.details" /></a></li>
		<li><a href="#tabs-3"><g:message code="connection.confirm" /></a></li>
	</ol>
	<g:form name="connectionForm" action="${action}" id='${fconnectionInstance?.id}'>
		<g:render template="type"/>
		<g:render template="details"/>
		<g:render template="confirm"/>
	</g:form>
</div>

<g:javascript>
var fconnection = {
	getType: function() {
		<g:if test="${fconnectionInstance}">return "${fconnectionInstance.getClass().shortName}";</g:if>
		<g:else>return $("#type-list").find("input[checked=checked]").val();</g:else>
	},
	setType: function(connectionType) {
		$("#type-list input[checked=checked]").attr('checked', '');
		$("#type-list ." + connectionType).attr('checked', 'checked');
		<g:each in="${Fconnection.implementations*.shortName}">
			$("#${it}-form").css('display', 'none');
		</g:each>
		$("#" + connectionType + "-form").css('display', 'inline');
	},
	show: function() {
		setConfirmVal('type', fconnection.humanReadableName());
		setConfirmation('name');
		fconnection[fconnection.getType()].show();
	},
	isValid: function() {
		var fields = fconnection[fconnection.getType()].requiredFields
		var valid = false;
		$.each(fields, function(index, value) {
			valid = isFieldSet(value);
			return valid;
		});
		return valid;
	},
	humanReadableName: function() {
		return fconnection[fconnection.getType()].humanReadableName;
	},

	<g:each in="${Fconnection.implementations}">
	${it.shortName}: {
		requiredFields: ["${Fconnection.getNonnullableConfigFields(it).join('", "')}"],
		humanReadableName: "<g:message code="${it.simpleName.toLowerCase()}.label"/>",
		<g:if test="${}">
		show: function() {
			<g:each in="${(Fconnection.implementations - it)*.shortName}">
				$("#${it}-confirm").hide();
			</g:each>
			<g:set var="configFields" value="${it.configFields instanceof Map ? it.configFields.values().flatten() : it.configFields}" />
			<g:each in="${configFields}" var="f">
				<g:if test="${f in it.passwords}">setSecretConfirmation('${f}');</g:if>
				<g:else>setConfirmation('${f}');</g:else>
			</g:each>
			$("#${it.shortName}-confirm").show();
		}
	},
	</g:each>
};
			
function isFieldSet(fieldName) {
	var val = getFieldVal(fieldName);
	console.log("isFieldSet: " + fieldName + val)
	return val!=null && val.length>0;
}

function getFieldVal(fieldName) {
	var val = $('#' + fconnection.getType() + fieldName).val();
	return val;
}

function setConfirmVal(fieldName, val) {
	if($('#' + fconnection.getType() + fieldName).is(":disabled") == false) {
		console.log(fieldName + "is enabled");
		$("#" + fconnection.getType() + "-confirm #confirm-" + fieldName).text(val);
	}
}

function setConfirmation(fieldName) {
	setConfirmVal(fieldName, getFieldVal(fieldName));
}

function setSecretConfirmation(fieldName) {
	val = isFieldSet(fieldName)? '****': 'None';
	setConfirmVal(fieldName, val);
}

function attachCheckBoxListener() {
	$("input[type='checkbox']").bind("change", function(){
		var key = $(this).attr("field");
		console.log("the key is: " + key);
		toggleFields(key);
	});
}

//TODO figure about the best way of figuring out the fields to enable and disable
function toggleFields(key) {
	
}

function initializePopup() {
	fconnection.setType("${fconnectionInstance?fconnectionInstance.getClass().shortName: 'smslib'}");
	
	$("#tabs").bind("tabsshow", fconnection.show);

	$("#tabs-2").contentWidget({
		validate: fconnection.isValid
	});
}
</g:javascript>
