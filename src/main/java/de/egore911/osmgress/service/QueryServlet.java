package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import de.egore911.osmgress.ConnectionFactory;

public class QueryServlet extends HttpServlet {

	private static final long serialVersionUID = 6670846638516835427L;

	private static final String SELECT_LINKS = "SELECT osmg_link.id, "
			+ "source.osm_id AS source_osm_id, "
			+ "source.name AS source_name, "
			+ "ST_Y(ST_Transform(source.way, 4326)) AS source_latitude, "
			+ "ST_X(ST_Transform(source.way, 4326)) AS source_longitude, "
			+ "target.osm_id AS target_osm_id, "
			+ "target.name AS target_name, "
			+ "ST_Y(ST_Transform(target.way, 4326)) AS target_latitude, "
			+ "ST_X(ST_Transform(target.way, 4326)) AS target_longitude, "
			+ "osmg_user.faction AS target_faction "
			+ "FROM osmg_link "
			+ "INNER JOIN osmg_portal source ON osmg_link.source_id = source.id "
			+ "INNER JOIN osmg_portal target ON osmg_link.target_id = target.id "
			+ "INNER JOIN osmg_user ON osmg_link.owner_id = osmg_user.id "
			+ "WHERE (ST_Y(ST_Transform(source.way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(source.way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(source.way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(source.way, 4326)) < ?) "
			+ "OR (ST_Y(ST_Transform(target.way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(target.way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(target.way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(target.way, 4326)) < ?)";

	private static final String SELECT_PORTALS = "SELECT osmg_portal.id, "
			+ "osmg_portal.name, "
			+ "ST_Y(ST_Transform(way, 4326)) as latitude, "
			+ "ST_X(ST_Transform(way, 4326)) as longitude, "
			+ "faction "
			+ "FROM osmg_portal "
			+ "LEFT OUTER JOIN osmg_user ON osmg_portal.owner_id = osmg_user.id "
			+ "WHERE ST_Y(ST_Transform(way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(way, 4326)) < ?";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		JsonParser parser = new JsonParser();
		JsonElement jsonElement = parser.parse(new InputStreamReader(req
				.getInputStream()));

		if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			double lat_min = jsonObject.get("lat_min").getAsDouble();
			double lat_max = jsonObject.get("lat_max").getAsDouble();
			double lon_min = jsonObject.get("lon_min").getAsDouble();
			double lon_max = jsonObject.get("lon_max").getAsDouble();

			loadPortals(resp, lat_min, lat_max, lon_min, lon_max);
		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		double lat_min = Double.parseDouble(req.getParameter("lat_min"));
		double lat_max = Double.parseDouble(req.getParameter("lat_max"));
		double lon_min = Double.parseDouble(req.getParameter("lon_min"));
		double lon_max = Double.parseDouble(req.getParameter("lon_max"));

		loadPortals(resp, lat_min, lat_max, lon_min, lon_max);
	}

	private void loadPortals(HttpServletResponse resp, double lat_min,
			double lat_max, double lon_min, double lon_max) throws IOException,
			ServletException {

		JsonObject result = new JsonObject();

		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_PORTALS)) {

				statement.setDouble(1, lat_min);
				statement.setDouble(2, lat_max);
				statement.setDouble(3, lon_min);
				statement.setDouble(4, lon_max);

				JsonArray portals = new JsonArray();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						portals.add(convertPortalToJson(resultSet));
					}
				}
				result.add("portals", portals);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_LINKS)) {

				statement.setDouble(1, lat_min);
				statement.setDouble(2, lat_max);
				statement.setDouble(3, lon_min);
				statement.setDouble(4, lon_max);
				statement.setDouble(5, lat_min);
				statement.setDouble(6, lat_max);
				statement.setDouble(7, lon_min);
				statement.setDouble(8, lon_max);

				JsonArray links = new JsonArray();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						links.add(convertLinkToJson(resultSet));
					}
				}
				result.add("links", links);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}

		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter writer = resp.getWriter();
		JsonWriter jsonWriter = new JsonWriter(writer);
		jsonWriter.setLenient(true);
		Streams.write(result, jsonWriter);
	}

	private JsonObject convertPortalToJson(@Nonnull ResultSet resultSetPoint)
			throws SQLException {
		JsonObject portal = new JsonObject();
		long id = resultSetPoint.getLong(1);
		portal.add("id", new JsonPrimitive(id));
		String name = resultSetPoint.getString(2);
		portal.add("name", name == null ? JsonNull.INSTANCE
				: new JsonPrimitive(name));
		double latitude = resultSetPoint.getDouble(3);
		portal.add("latitude", new JsonPrimitive(latitude));
		double longitude = resultSetPoint.getDouble(4);
		portal.add("longitude", new JsonPrimitive(longitude));
		String faction = resultSetPoint.getString(5);
		portal.add("faction", faction == null ? JsonNull.INSTANCE
				: new JsonPrimitive(faction));
		return portal;
	}

	private JsonObject convertLinkToJson(@Nonnull ResultSet resultSetPoint)
			throws SQLException {
		JsonObject link = new JsonObject();
		long id = resultSetPoint.getLong(1);
		link.add("id", new JsonPrimitive(id));

		{
			JsonObject portal = new JsonObject();
			long sourceOsmId = resultSetPoint.getLong(2);
			portal.add("id", new JsonPrimitive(sourceOsmId));
			String name = resultSetPoint.getString(3);
			portal.add("name", name == null ? JsonNull.INSTANCE
					: new JsonPrimitive(name));
			double latitude = resultSetPoint.getDouble(4);
			portal.add("latitude", new JsonPrimitive(latitude));
			double longitude = resultSetPoint.getDouble(5);
			portal.add("longitude", new JsonPrimitive(longitude));
			link.add("source", portal);
		}

		{
			JsonObject portal = new JsonObject();
			long sourceOsmId = resultSetPoint.getLong(6);
			portal.add("id", new JsonPrimitive(sourceOsmId));
			String name = resultSetPoint.getString(7);
			portal.add("name", name == null ? JsonNull.INSTANCE
					: new JsonPrimitive(name));
			double latitude = resultSetPoint.getDouble(8);
			portal.add("latitude", new JsonPrimitive(latitude));
			double longitude = resultSetPoint.getDouble(9);
			portal.add("longitude", new JsonPrimitive(longitude));
			link.add("target", portal);
		}

		String faction = resultSetPoint.getString(10);
		link.add("faction", new JsonPrimitive(faction));

		return link;
	}

}
