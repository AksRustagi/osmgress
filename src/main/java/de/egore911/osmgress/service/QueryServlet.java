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
		
		try {
			Connection connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/osmgress_mapnik", "osmgress",
					"osmgress");

			PreparedStatement statement = connection
					.prepareStatement("SELECT osm_id, "
							+ "name, "
							+ "ST_X (ST_Transform (way, 4326)), "
							+ "ST_Y (ST_Transform (way, 4326)), "
							+ "ST_Distance(ST_Transform(way, 4326), ST_Geomfromtext('POINT(7.599485 50.356718)',4326)) as distance "
							+ "FROM planet_osm_point "
							+ "WHERE amenity='place_of_worship' "
							+ "ORDER BY distance ASC " + "LIMIT 100");
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
				writer.print(name == null ? "null" : "\"" + name.replace("\"", "\\\"") + "\"");
				writer.print(",\"x\":");
				double x = resultSet.getDouble(3);
				writer.print(x);
				writer.print(",\"y\":");
				double y = resultSet.getDouble(4);
				writer.print(y);
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
