function MinimalRenderer(){

	var social = new GraaaspOpenSocialWrapper();
	var activities;

	this.renderTitle = function(div, callback) {
		social.loadViewer(function(response) {
			viewer = response.viewer;
			var html = '<h2>' + viewer.displayName +' dashboard </h2>';
			document.getElementById(div).innerHTML = html;
			callback();
		});
	}

	this.renderActivities = function(div, callback, verb, quantity) {
		social.loadActivityStream(function(response) {
			activities = response.acts;
			var html = '<p>Your activities : </p>';
			html +=  '<table>' + processActivities(activities, verb, quantity) +'</table>';
			document.getElementById(div).innerHTML = html;
			callback();
		}, 1411);
	}

	function processActivities(activities, verb, quantity) {
		var html = '';
		nbRes = 0;
		idx = 0;
		while(idx < Object.keys(activities).length && nbRes < quantity) {
			if (verb == "all" || verb == activities[idx].verb) {
				html += '<tr>';
				html += '<td>' + activities[idx].published + ' : ' + activities[idx].object.displayName + ' ' + conjugateVerb(activities[idx].verb) + ' ' + ((typeof(activities[idx].target.id) != 'undefined') ? activities[idx].target.displayName  : '') + '</td>';
				html += '</tr>';
				nbRes++;
			}
			idx++;
		}
		return html;
	}

	this.updateActivities = function(div, callback, verb, quantity) {
		var html = '<p>Your activities : </p>';
		html +=  '<table>' + processActivities(activities, verb, quantity) +'</table>';
		document.getElementById(div).innerHTML = html;
		callback();
	}

	function conjugateVerb(verb) {
		if (verb.charAt(verb.length-1) != 'e') verb+="ed";
		else verb+="d";
		return verb;
	}
}

