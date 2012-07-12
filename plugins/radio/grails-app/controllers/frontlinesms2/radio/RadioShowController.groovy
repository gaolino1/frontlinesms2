package frontlinesms2.radio

import frontlinesms2.MessageController
import frontlinesms2.*
import java.util.Date
import grails.converters.*
import java.text.SimpleDateFormat

class RadioShowController extends MessageController {
	static allowedMethods = [save: "POST"]
	
	def index() {
		params.sort = 'date'
		redirect(action:messageSection, params:params)
	}
	
	def create() {
		[showInstance: new RadioShow()]
	}

	def save() {
		def showInstance = RadioShow.get(params.ownerId) ?: new RadioShow()
		showInstance.properties = params
		if (showInstance.validate()) {
			showInstance.save()
			flash.message = message(code: 'radio.show.saved')
		} else {
			flash.message = message(code: 'radio.show.invalid.name')
		}
		redirect(controller: 'message', action: "inbox")
}
	
	def radioShow() {
		withRadioShow { showInstance ->
			if(showInstance) {
				def messageInstanceList = showInstance?.getShowMessages(params.starred)
				def radioMessageInstanceList = []
				messageInstanceList?.list(params).inject([]) { messageB, messageA ->
				    if(messageB && dateToString(messageB.date) != dateToString(messageA.date) && params.sort == 'date')
				        radioMessageInstanceList.add(dateToString(messageA.date))
				    radioMessageInstanceList.add(messageA)
				    return messageA
				}
				render view:'standard',
					model:[messageInstanceList: radioMessageInstanceList,
						   messageSection: 'radioShow',
						   messageInstanceTotal: messageInstanceList?.count(),
						   ownerInstance: showInstance] << this.getShowModel()
		} else {
			flash.message = message(code: 'radio.show.not.found')
			redirect(action: 'inbox')
		}
		}
	}
	
	def startShow() {
		def showInstance = RadioShow.findById(params.id)
		println "params.id: ${params.id}"
		if(showInstance?.start()) {
			println "${showInstance.name} show started"
			showInstance.save(flush:true)
			render "$showInstance.id"
		} else {
			flash.message = message code:'radio.show.onair.error', args:[RadioShow.findByIsRunning(true)?.name]
			render text:flash.message
		}
	}
	
	def stopShow() {
		def showInstance = RadioShow.findById(params.id)
		showInstance.stop()
		showInstance.save(flush:true)
		render "$showInstance.id"
	}
	
	private def getShowModel(messageInstanceList) {
		def model = super.getShowModel(messageInstanceList)
		model << [radioShowInstanceList: RadioShow.findAll()]
		return model
	}
	
	def getNewRadioMessageCount = {
		if(params.messageSection == 'radioShow') {
			def messageCount = [totalMessages:[RadioShow.get(params.ownerId)?.getShowMessages()?.count()]]
			render messageCount as JSON
		} else {
			getNewMessageCount()
		}
	}
	
	def addActivity() {
		def activityInstance = Activity.get(params.activityId)
		def showInstance = RadioShow.get(params.radioShowId)
		
		if(showInstance && activityInstance) {
			showInstance.addToActivities(activityInstance)
		}
		else if(activityInstance) {
			RadioShow.findByOwnedActivity(activityInstance).get()?.removeFromActivities(activityInstance)
		}
		redirect controller:"message", action:"activity", params: [ownerId: params.activityId]
	}
	
	def selectActivity() {
		def activityInstance = Activity.get(params.ownerId)
		def radioShowIntance = RadioShow.findByOwnedActivity(activityInstance).get()
		[ownerInstance:activityInstance, currentShow:radioShowIntance, radioShows:RadioShow.findAllByDeleted(false)]
	}

	def rename() {
		withRadioShow{ showInstance ->
			[showInstance: showInstance]
		}
	}

	def confirmDelete() {
		def showInstance = RadioShow.get(params.id)
		model:[ownerName:showInstance.name,
				ownerInstance:showInstance]
	}

	def delete() {
		withRadioShow params.id, { showInstance->
			if(showInstance.isRunning){
				showInstance.isRunning = false
				showInstance.save()
			}
			trashService.sendToTrash(showInstance)
			showInstance.activities?.each{ activity ->
				trashService.sendToTrash(activity)
			}
			flash.message = defaultMessage 'trashed'
			redirect controller:"message", action:"inbox"
		}
	}

	def restore() {
		def radioShow = RadioShow.findById(params.id)
		if(radioShow){
			Trash.findByObject(radioShow)?.delete()
			radioShow.deleted = false
			radioShow.messages.each{
				it.isDeleted = false
				it.save(failOnError: true, flush: true)
			}
			radioShow.activities.each{ activity->
				activity.deleted = false
				activity.save()
				activity.messages.each{
					it.isDeleted = false
					it.save(failOnError: true, flush: true)
				}
				Trash.findByObject(activity)?.delete()
			}

			if(radioShow.save()) {
				flash.message = defaultMessage 'restored'
			} else {
				flash.message = defaultMessage 'restore.failed', activity.id
			}
		}
		redirect controller:"message", action:"trash"
	}
	
	private void removeActivityFromRadioShow(Activity activity) {
		RadioShow.findAll().collect { showInstance ->
			if(activity in showInstance.activities) {
				showInstance.removeFromActivities(activity)
				showInstance.save()
			}
		}
	}
	
	private String dateToString(Date date) {
		new SimpleDateFormat("EEEE, MMMM dd", Locale.US).format(date)
	}

	private def withRadioShow(id=params.ownerId, Closure c) {
		def showInstance = RadioShow.findByIdAndDeleted(id, false)
		if (showInstance) c showInstance
		else render text:defaultMessage('notfound', params.id)
	}

//TODO clean up default message declaration to prevent future duplication
	private def defaultMessage(String code, Object... args=[]) {
		def messageName = message code:'radio.label'
		return message(code:'default.' + code,
				args:[messageName] + args)
	}
	
}
