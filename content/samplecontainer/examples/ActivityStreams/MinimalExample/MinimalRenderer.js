function MinimalRenderer(){

	var social = new GraaaspOpenSocialWrapper();

	this.renderWelcome = function(div, callback) {
		social.loadViewer(function(response) {
			viewer = response.viewer;
			var html = '<h2>' + viewer.displayName +' dashboard </h2>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	this.renderActivities = function(div, callback, verb, quantity) {
	social.loadActivityStream(function(response) {
		act = response.acts;
		var html = 'Your activities : ';
		html +=  '<table>' + processActivities(act, verb, quantity) +'</table>';
		document.getElementById(div).innerHTML = html;
		callback();
	});
	}

	function processActivities(activities, verb, quantity) {
		var html = '';
		for (idx = 0; idx < quantity; idx++) {
			if (verb == "all" || verb == activities[idx].verb) {
				html += '<tr>';
				html += '<td>' + activities[idx].published + ' : ' + activities[idx].object.displayName + ' ' + displayVerb(activities[idx].verb) + ' ' + ((typeof(activities[idx].target.id) != 'undefined') ? activities[idx].target.displayName  : '') + '</td>';
				html += '</tr>';
			}
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

