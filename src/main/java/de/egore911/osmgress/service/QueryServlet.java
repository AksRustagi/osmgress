package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

public class QueryServlet extends HttpServlet {

	private static final long serialVersionUID = 6670846638516835427L;

	@Override
	public void init() throws ServletException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("application/json; charset=UTF-8");

		double lat_min = Double.parseDouble(req.getParameter("lat_min"));
		double lat_max = Double.parseDouble(req.getParameter("lat_max"));
		double lon_min = Double.parseDouble(req.getParameter("lon_min"));
		double lon_max = Double.parseDouble(req.getParameter("lon_max"));

		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/osmgress_mapnik", "osmgress",
					"osmgress");

			PreparedStatement statement = connection
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
							+ "AND ST_XMin(ST_Transform(way, 4326)) < ?");

			statement.setDouble(1, lat_min);
			statement.setDouble(2, lat_max);
			statement.setDouble(3, lon_min);
			statement.setDouble(4, lon_max);
			statement.setDouble(5, lat_min);
			statement.setDouble(6, lat_max);
			statement.setDouble(7, lon_min);
			statement.setDouble(8, lon_max);

			ResultSet resultSet = statement.executeQuery();

			JsonArray jsonArray = new JsonArray();

			while (resultSet.next()) {
				jsonArray.add(convertToJson(resultSet));
			}
			PrintWriter writer = resp.getWriter();
			JsonWriter jsonWriter = new JsonWriter(writer);
			jsonWriter.setLenient(true);
			Streams.write(jsonArray, jsonWriter);

			connection.close();

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
		jsonObject.add("name", name == null ? JsonNull.INSTANCE : new JsonPrimitive(name));
		double latitude = resultSetPoint.getDouble(3);
		jsonObject.add("latitude", new JsonPrimitive(latitude));
		double longitude = resultSetPoint.getDouble(4);
		jsonObject.add("longitude", new JsonPrimitive(longitude));
		return jsonObject;
	}

}
