var blueIcons = new Array();
var greenIcons = new Array();

$(function() {
	for (var i = 0; i < 8; i++) {
		blueIcons[i] = L.icon({
			iconUrl : 'images/blue' + (i + 1) + '_32.png',
			iconRetinaUrl : 'images/blue' + (i + 1) + '_64.png',
			iconSize : [ 32, 32 ],
			iconAnchor : [ 16, 16 ],
			popupAnchor : [ 0, -20 ]
		});
		greenIcons[i] = L.icon({
			iconUrl : 'images/green' + (i + 1) + '_32.png',
			iconRetinaUrl : 'images/green' + (i + 1) + '_64.png',
			iconSize : [ 32, 32 ],
			iconAnchor : [ 16, 16 ],
			popupAnchor : [ 0, -20 ]
		});
	}
});

var gray = L.icon({
	iconUrl : 'images/gray0_32.png',
	iconRetinaUrl : 'images/gray0_64.png',
	iconSize : [ 32, 32 ],
	iconAnchor : [ 16, 16 ],
	popupAnchor : [ 0, -20 ]
});

function toIcon(faction, slots) {
	if (faction == 'blue') {
		return blueIcons[slots.length - 1];
	} else if (faction == 'green') {
		return greenIcons[slots.length - 1];
	} else {
		return gray;
	}
}

function toColor(faction) {
	if (faction == 'blue') {
		return '#269eff';
	} else if (faction == 'green') {
		return '#3bea5e';
	} else {
		return 'white';
	}
}

