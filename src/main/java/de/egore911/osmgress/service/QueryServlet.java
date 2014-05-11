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

		double lon = ((lon_max - lon_min) / 2) + lon_min;
		double lat = ((lat_max - lat_min) / 2) + lat_min;

		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/osmgress_mapnik", "osmgress",
					"osmgress");

			PreparedStatement statement = connection
					.prepareStatement("SELECT osm_id, "
							+ "name, "
							+ "ST_Y (ST_Transform (way, 4326)), "
							+ "ST_X (ST_Transform (way, 4326)), "
							+ "ST_Distance(ST_Transform(way, 4326), ST_Geomfromtext('POINT("
							+ lon + " " + lat + ")',4326)) as distance "
							+ "FROM planet_osm_point "
							+ "WHERE amenity='place_of_worship' "
							+ "AND ST_Y (ST_Transform (way, 4326)) > "
							+ lat_min + " "
							+ "AND ST_Y (ST_Transform (way, 4326)) < "
							+ lat_max + " "
							+ "AND ST_X (ST_Transform (way, 4326)) > "
							+ lon_min + " "
							+ "AND ST_X (ST_Transform (way, 4326)) < "
							+ lon_max + " " + "ORDER BY distance ASC");
			ResultSet resultSet = statement.executeQuery();

			PrintWriter writer = resp.getWriter();

			writer.print("[\n");
			boolean first = true;
			while (resultSet.next()) {
				if (!first) {
					writer.print(",\n");
				}
				writer.print("  {\"id\":");
				long id = resultSet.getLong(1);
				writer.print(id);
				writer.print(",\"name\":");
				String name = resultSet.getString(2);
				writer.print(name == null ? "null" : "\""
						+ name.replace("\"", "\\\"") + "\"");
				writer.print(",\"latitude\":");
				double latitude = resultSet.getDouble(3);
				writer.print(latitude);
				writer.print(",\"longitude\":");
				double longitude = resultSet.getDouble(4);
				writer.print(longitude);
				writer.print(",\"distance\":");
				double distance = resultSet.getDouble(5);
				writer.print(distance);
				writer.print("}");
				first = false;
			}
			writer.print("\n]");

			connection.close();

		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}

}
