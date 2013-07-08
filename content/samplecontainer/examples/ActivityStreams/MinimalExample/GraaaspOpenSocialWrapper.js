function GraaaspOpenSocialWrapper() {

	this.loadViewer = function(callback) {
		var batch = osapi.newBatch();
		batch.add('viewer', osapi.people.getViewer());
		batch.execute(callback);
	}

	this.loadActivityStream = function(callback) {
		var batch = osapi.newBatch();
		batch.add('acts', osapi.activitystreams.get({userId: 1411, activityId: 7}));
		batch.execute(callback);
	}
}