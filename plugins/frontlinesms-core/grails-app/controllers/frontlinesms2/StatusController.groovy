package frontlinesms2

class StatusController extends ControllerUtils {
	def deviceDetectionService
	
	def index() {
		redirect action:'show', params:params
	}
	
	def show() {
		[connectionInstanceList: Fconnection.list(),
				connectionInstanceTotal: Fconnection.count(),
				detectedDevices:deviceDetectionService.detected] <<
			getMessageStats() << getFilters()
	}
	
	def detectDevices() {
		deviceDetectionService.detect()
		redirect action:'show'
	}
	
	def resetDetection() {
		deviceDetectionService.reset()
		redirect action:'index'
	}

	private def getMessageStats() {
		def activityInstance = MessageOwner.get(params.activityId)
		params.startDate = params.rangeOption == "between-dates" ? params.startDate : new Date() - 14
		params.endDate = params.rangeOption == "between-dates" ? params.endDate : new Date()
		params.messageOwner = activityInstance
		params.groupInstance = params.groupId ? Group.get(params.groupId) : null
		params.messageStatus = params.messageStatus?.tokenize(",")*.trim()
		def messageStats = TextMessage.getMessageStats(params) // TODO consider changing the output of this method to match the data we actually want
		[messageStats: [xdata: messageStats.keySet().collect{"'${it}'"},
							sent: messageStats.values()*.sent,
							received: messageStats.values()*.received]]
	}
	
	private def getFilters() {
			def groupInstance = params.groupId? Group.get(params.groupId): null
			params.rangeOption = params.rangeOption ?: "two-weeks"
			[groupInstance: groupInstance,
					activityId: params.activityId,
					groupInstanceList : Group.findAll(),
					activityInstanceList: Activity.findAllByDeleted(false),
					folderInstanceList: Folder.findAllByDeleted(false)]
	}
}

