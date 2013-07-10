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
		var html = 'Your activities : ';
		html +=  '<table>' + processActivities(act) +'</table>';
		document.getElementById(div).innerHTML = html;
		callback();
	});
	}

	function processActivities(activities) {
		var html = '';
		for (idx = 0; idx < 150; idx++) {
			html += '<tr>';
			html += '<td>' + activities[idx].published + ' : ' + activities[idx].object.displayName + ' ' + displayVerb(activities[idx].verb) + ' ' + ((typeof activities[idx].target !== 'undefined') ? activities[idx].target.displayName  : '') + '</td>';
			html += '</tr>';
		}
		return html;
	}

	function displayVerb(verb) {
		var d = "e";
		if (d.indexOf(verb.charAt(verb.length-1).toString) === -1) verb+="ed";
		else verb+="d";
		return verb;
	}
}

