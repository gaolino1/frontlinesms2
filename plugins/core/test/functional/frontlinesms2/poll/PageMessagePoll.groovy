package frontlinesms2.poll

import frontlinesms2.*

class PageMessagePoll extends frontlinesms2.message.PageMessageActivity {
	static url = 'message/activity'
	static content = {
		moreActions { $('div.header-buttons select#more-actions') }
	}

}