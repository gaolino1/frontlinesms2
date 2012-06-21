<%@ page import="frontlinesms2.Poll; frontlinesms2.radio.RadioShow" %>
<div class="input">
	<label for="pollType"><g:message code="poll.type.prompt"/></label>
	<ul class="select">
		<g:set var="isYesNo" value="${activityInstanceToEdit?.yesNo}"/>
		<li>
			<label for="pollType"><g:message code="poll.question.yes.no"/></label>
			<g:radio name="pollType" value="yesNo" checked="${!activityInstanceToEdit || isYesNo}" disabled="${activityInstanceToEdit && !isYesNo}"/>
		</li>
		<li>
			<label for="pollType"><g:message code="poll.question.multiple"/></label>
			<g:radio name="pollType" value="multiple" checked="${activityInstanceToEdit && !isYesNo}" disabled="${activityInstanceToEdit && isYesNo}"/>
		</li>
	</ul>
</div>
<div class="add-to-radioshow">
	<g:select class="dropdown" name='radioShowId' value=""
	    noSelection="${['':'Assign to Radio Show...']}"
	    from='${RadioShow.findAll()}'
	    optionKey="id" optionValue="name"/>
</div>
<div class="input required">
	<label for="question">
		<g:message code="poll.question.prompt"/>
	</label>
	<g:textArea name="question" value="${activityInstanceToEdit?.question}" class="required"/>
</div>
<div class="input optional">
	<label for="dontSendMessage"><g:message code="poll.message.none"/></label>
	<g:checkBox name="dontSendMessage" value="no-message" checked='false'/>
</div>

<r:script>
	$("input[name='dontSendMessage']").live("change", function() {
		if(isGroupChecked("dontSendMessage")) {
			disableTab(4);
			disableTab(5);
			//update confirm screen
			updateConfirmationMessage();
		} else {
			enableTab(4);
			enableTab(5);
		}
	});

	$("input[name='pollType']").live("change", function() {
		if ($("input[name='pollType']:checked").val() == "yesNo") {
			disableTab(1);
		} else {
			enableTab(1);
		}
		autoUpdate = true;
		updateConfirmationMessage();
	});
</r:script>
