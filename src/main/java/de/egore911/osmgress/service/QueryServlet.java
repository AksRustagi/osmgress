package de.egore911.osmgress.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

			PreparedStatement statementPoint = connection
					.prepareStatement("SELECT osm_id, "
							+ "name, "
							+ "ST_Y(ST_Transform(way, 4326)) as latitude, "
							+ "ST_X(ST_Transform(way, 4326)) as longitude "
							+ "FROM planet_osm_point "
							+ "WHERE (amenity='place_of_worship' OR amenity='hospital') "
							+ "AND ST_Y(ST_Transform(way, 4326)) > " + lat_min + " "
							+ "AND ST_Y(ST_Transform(way, 4326)) < " + lat_max + " "
							+ "AND ST_X(ST_Transform(way, 4326)) > " + lon_min + " "
							+ "AND ST_X(ST_Transform(way, 4326)) < " + lon_max);
			ResultSet resultSetPoint = statementPoint.executeQuery();
			PreparedStatement statementPolygon = connection
					.prepareStatement("SELECT osm_id, "
							+ "name, "
							+ "((ST_YMax(ST_Transform(way, 4326)) - ST_YMin(ST_Transform(way, 4326))) / 2) + ST_YMin(ST_Transform(way, 4326)) as latitude, "
							+ "((ST_XMax(ST_Transform(way, 4326)) - ST_XMin(ST_Transform(way, 4326))) / 2) + ST_XMin(ST_Transform(way, 4326)) as longitude "
							+ "FROM planet_osm_polygon "
							+ "WHERE (amenity='place_of_worship' OR amenity='hospital') "
							+ "AND ST_YMax(ST_Transform(way, 4326)) > " + lat_min + " "
							+ "AND ST_YMin(ST_Transform(way, 4326)) < " + lat_max + " "
							+ "AND ST_XMax(ST_Transform(way, 4326)) > " + lon_min + " "
							+ "AND ST_XMin(ST_Transform(way, 4326)) < " + lon_max);
			ResultSet resultSetPolygon = statementPolygon.executeQuery();

			PrintWriter writer = resp.getWriter();

			writer.print("[\n");
			boolean first = true;
			while (resultSetPoint.next()) {
				if (!first) {
					writer.print(",\n");
				}
				convertToJson(resultSetPoint, writer);
				first = false;
			}
			while (resultSetPolygon.next()) {
				if (!first) {
					writer.print(",\n");
				}
				convertToJson(resultSetPolygon, writer);
				first = false;
			}
			writer.print("\n]");

			connection.close();

		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}

	private void convertToJson(ResultSet resultSetPoint, PrintWriter writer)
			throws SQLException {
		writer.print("  {\"id\":");
		long id = resultSetPoint.getLong(1);
		writer.print(id);
		writer.print(",\"name\":");
		String name = resultSetPoint.getString(2);
		writer.print(name == null ? "null" : "\""
				+ name.replace("\"", "\\\"") + "\"");
		writer.print(",\"latitude\":");
		double latitude = resultSetPoint.getDouble(3);
		writer.print(latitude);
		writer.print(",\"longitude\":");
		double longitude = resultSetPoint.getDouble(4);
		writer.print(longitude);
		writer.print("}");
	}

}
