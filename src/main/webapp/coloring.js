var blue8 = L.icon({
	iconUrl : 'images/blue8_32.png',
	iconRetinaUrl : 'images/blue8_64.png',
	iconSize : [ 32, 32 ],
	iconAnchor : [ 16, 16 ],
	popupAnchor : [ 0, -20 ]
});
var green8 = L.icon({
	iconUrl : 'images/green8_32.png',
	iconRetinaUrl : 'images/green8_64.png',
	iconSize : [ 32, 32 ],
	iconAnchor : [ 16, 16 ],
	popupAnchor : [ 0, -20 ]
});

var gray = L.icon({
	iconUrl : 'images/gray0_32.png',
	iconRetinaUrl : 'images/gray0_64.png',
	iconSize : [ 32, 32 ],
	iconAnchor : [ 16, 16 ],
	popupAnchor : [ 0, -20 ]
});

function toIcon(faction) {
	if (faction == 'blue') {
		return blue8;
	} else if (faction == 'green') {
		return green8;
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

