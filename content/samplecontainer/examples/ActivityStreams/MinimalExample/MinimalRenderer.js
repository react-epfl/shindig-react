function MinimalRenderer(){

	var social = new GraaaspOpenSocialWrapper();

	this.renderWelcome = function(div, callback) {
		social.loadViewer(function(response) {
			viewer = response.viewer;
			var html = '<h1 />Hello ' + viewer.displayName +' !</h1>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	this.renderActivities = function(div, callback) {
	social.loadActivityStream(function(response) {
		act = response.acts;
		var html = 'Activity retrieved by Shindig : ';
		html += act.id;
		// html +=  ' ID = ' + processActivities(act);
		document.getElementById(div).innerHTML = html;
		callback();
	});

	function processActivities(activities) {
		var html = '';
		for (idx = 0; idx < activities.length; idx++) {
			html += '<tr>';
			html += '<td>' + activities[idx].id + '</td>';
			html += '</tr>';
		}
		return html;
	}
}


}