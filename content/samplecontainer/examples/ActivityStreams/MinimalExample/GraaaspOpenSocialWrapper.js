function GraaaspOpenSocialWrapper() {

	this.loadViewer = function(callback) {
		var batch = osapi.newBatch();
		batch.add('viewer', osapi.people.getViewer());
		batch.execute(callback);
	}

	this.loadActivityStream = function(callback) {
		var batch = osapi.newBatch();
		batch.add('acts', osapi.activitystreams.get({contextId: 1411}));
		batch.execute(callback);
	}
}