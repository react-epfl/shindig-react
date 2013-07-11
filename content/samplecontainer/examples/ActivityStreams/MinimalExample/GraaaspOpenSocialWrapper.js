function GraaaspOpenSocialWrapper() {

	this.loadViewer = function(callback) {
		var batch = osapi.newBatch();
		batch.add('viewer', osapi.people.getViewer());
		batch.execute(callback);
	}

	this.loadActivityStream = function(id, type, callback) {
		var batch = osapi.newBatch();
		batch.add('acts', osapi.activitystreams.get({contextId: id, contextType: type}));
		batch.execute(callback);
	}
}