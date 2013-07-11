function GraaaspOpenSocialWrapper() {

	this.loadViewer = function(callback) {
		var batch = osapi.newBatch();
		batch.add('viewer', osapi.people.getViewer());
		batch.execute(callback);
	}

	this.loadActivityStream = function(callback, userId) {
		var batch = osapi.newBatch();
		batch.add('acts', osapi.activitystreams.get({contextId: userId}));
		batch.execute(callback);
	}
}