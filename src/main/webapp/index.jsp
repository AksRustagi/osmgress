<!DOCTYPE html>
<html>
<head>
<title>osmgress</title>
<link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7.2/leaflet.css" />
<link rel="stylesheet" href="http://netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" />
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
<script src="http://cdn.leafletjs.com/leaflet-0.7.2/leaflet.js" type="text/javascript"></script>
<script src="http://code.jquery.com/jquery-2.1.1.min.js" type="text/javascript"></script>
<script src="coloring.js" type="text/javascript"></script>
<script src="easy-button.js" type="text/javascript"></script>
<style type="text/css">
html,body {
	margin: 0;
	padding: 0;
	height: 100%;
}

#map {
	min-height: 100%;
	height: auto !important;
	height: 100%;
	overflow: hidden !important;
}
</style>
</head>
<body>
	<div id="map"></div>
	<script type="text/javascript">

		// TODO hack
		var user = {
				id: 1,
				name: "egore911@gmail.com",
				faction: "green"
		}

		var map;
		var lookupPortal = {};
		var lookupLink = {};

		function deployResonator(portal) {
			$.get("portal/deployResonator?portal=" + portal + "&owner=" + user.id, undefined, function(data, textStatus, jqXHR) {
				if (data.status == "ok") {
					var marker = lookupPortal[portal];
					var faction = data.portal.owner.faction;
					var slots = data.portal.slots;
					var icon = toIcon(faction, slots);
					marker.setIcon(icon);
					marker.closePopup();
					var popup = marker.getPopup();
					popup.setContent(getMarkerLabel(data.portal));
				}
			});
		}

		var from = undefined;
		function link(portal) {
			if (from == undefined) {
				from = portal;
				var marker = lookupPortal[portal];
				marker.closePopup();
			} else {
				$.get("link?from=" + from + "&to=" + portal + "&owner=" + user.id, undefined, function(data, textStatus, jqXHR) {
					if (data.status == "ok") {
						var marker = lookupPortal[portal];
						marker.closePopup();
						var popup = marker.getPopup();
						popup.setContent(getMarkerLabel(data.link.target));
						marker = lookupPortal[from];
						var popup = marker.getPopup();
						popup.setContent(getMarkerLabel(data.link.source));

						var pointList = [new L.LatLng(data.link.source.latitude, data.link.source.longitude),
						                 new L.LatLng(data.link.target.latitude, data.link.target.longitude)];

						var line = new L.Polyline(pointList, {
							color: toColor(data.link.owner.faction),
							weight: 3,
							opacity: 0.5,
							smoothFactor: 1
						}).addTo(map);
					}
					from = undefined;
				});
			}
		}

		function getMarkerLabel(val) {
			var label = "<b>";
			if (val.name != undefined) {
				label += val.name;
			} else {
				label += "?"
			}
			label += "</b><br/>" + val.id;
			if (val.owner == undefined) {
				label += "<br/><span href=\"#\" onclick=\"deployResonator(" + val.id + "); return false;\" class=\"btn btn-xs btn-info\">Deploy resonator</span>";
			} else if (val.owner.faction == user.faction) {
				label += "<br/>";
				if (val.slots.length < 8) {
					label += "<span href=\"#\" onclick=\"deployResonator(" + val.id + "); return false;\" class=\"btn btn-xs btn-info\">Deploy resonator</span>";
				} else {
					label += "<span href=\"#\" onclick=\"link(" + val.id + "); return false;\" class=\"btn btn-xs btn-info\">Link</span>";
				}
			}
			return label;
		}

		function load(map) {
			var bounds = map.getBounds();
			$.getJSON("query?lat_min=" + bounds.getSouth() + "&lat_max="
					+ bounds.getNorth() + "&lon_min=" + bounds.getWest()
					+ "&lon_max=" + bounds.getEast(), function(data) {
				$.each(data.portals, function(key, val) {
					if (lookupPortal[val.id] == undefined) {
						var marker = L.marker([ val.latitude, val.longitude ], {icon: toIcon(val.owner != undefined ? val.owner.faction : undefined, val.slots)})
								.addTo(map);
						marker.bindPopup(getMarkerLabel(val));
						lookupPortal[val.id] = marker;
					}
				});
				$.each(data.links, function(key, val) {

					if (lookupLink[val.source.id + '-' + val.target.id] == undefined) {
						var pointList = [new L.LatLng(val.source.latitude, val.source.longitude),
						                 new L.LatLng(val.target.latitude, val.target.longitude)];

						var line = new L.Polyline(pointList, {
							color: toColor(val.owner.faction),
							weight: 3,
							opacity: 0.5,
							smoothFactor: 1
						}).addTo(map);
						lookupLink[val.source.id + '-' + val.target.id] = line;
					}
				});
			});
		}

		$(function() {
			map = L.map('map').setView([ 50.356718, 7.599485 ], 14);
			L
					.tileLayer(
							'/osm_tiles/{z}/{x}/{y}.png',
							{
								attribution : 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
								maxZoom : 18
							}).addTo(map);

			L.easyButton('fa-compass', function() {
				map.locate({setView: true, enableHighAccuracy: true});
			}, 'Center to your position');

			load(map);

			map.on('moveend', function(e) {
				load(map);
			});

		});
	</script>
</body>
</html>