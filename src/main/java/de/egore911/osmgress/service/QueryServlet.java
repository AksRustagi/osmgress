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
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement("SELECT osm_id, "
							+ "name, "
							+ "ST_Y(ST_Transform(way, 4326)) as latitude, "
							+ "ST_X(ST_Transform(way, 4326)) as longitude "
							+ "FROM planet_osm_point "
							+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy') "
							+ "AND ST_Y(ST_Transform(way, 4326)) > ? "
							+ "AND ST_Y(ST_Transform(way, 4326)) < ? "
							+ "AND ST_X(ST_Transform(way, 4326)) > ? "
							+ "AND ST_X(ST_Transform(way, 4326)) < ? UNION "
							+ "SELECT osm_id, "
							+ "name, "
							+ "((ST_YMax(ST_Transform(way, 4326)) - ST_YMin(ST_Transform(way, 4326))) / 2) + ST_YMin(ST_Transform(way, 4326)) as latitude, "
							+ "((ST_XMax(ST_Transform(way, 4326)) - ST_XMin(ST_Transform(way, 4326))) / 2) + ST_XMin(ST_Transform(way, 4326)) as longitude "
							+ "FROM planet_osm_polygon "
							+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy') "
							+ "AND ST_YMax(ST_Transform(way, 4326)) > ? "
							+ "AND ST_YMin(ST_Transform(way, 4326)) < ? "
							+ "AND ST_XMax(ST_Transform(way, 4326)) > ? "
							+ "AND ST_XMin(ST_Transform(way, 4326)) < ?")) {

				statement.setDouble(1, lat_min);
				statement.setDouble(2, lat_max);
				statement.setDouble(3, lon_min);
				statement.setDouble(4, lon_max);
				statement.setDouble(5, lat_min);
				statement.setDouble(6, lat_max);
				statement.setDouble(7, lon_min);
				statement.setDouble(8, lon_max);

				JsonArray jsonArray = new JsonArray();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						jsonArray.add(convertToJson(resultSet));
					}
				}
				resp.setContentType("application/json; charset=UTF-8");
				PrintWriter writer = resp.getWriter();
				JsonWriter jsonWriter = new JsonWriter(writer);
				jsonWriter.setLenient(true);
				Streams.write(jsonArray, jsonWriter);
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	private JsonObject convertToJson(@Nonnull ResultSet resultSetPoint)
			throws SQLException {
		JsonObject jsonObject = new JsonObject();
		long id = resultSetPoint.getLong(1);
		jsonObject.add("id", new JsonPrimitive(id));
		String name = resultSetPoint.getString(2);
		jsonObject.add("name", name == null ? JsonNull.INSTANCE
				: new JsonPrimitive(name));
		double latitude = resultSetPoint.getDouble(3);
		jsonObject.add("latitude", new JsonPrimitive(latitude));
		double longitude = resultSetPoint.getDouble(4);
		jsonObject.add("longitude", new JsonPrimitive(longitude));
		return jsonObject;
	}

}
