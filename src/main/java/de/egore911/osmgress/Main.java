package de.egore911.osmgress;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException,
			SQLException {

		Class.forName("org.postgresql.Driver");

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
						+ "ORDER BY distance ASC "
						+ "LIMIT 100");
		ResultSet resultSet = statement.executeQuery();
		StringBuilder builder = new StringBuilder();
		builder.append("[\n");
		boolean first = true;
		while (resultSet.next()) {
			if (!first) {
				builder.append(",\n");
			}
			builder.append("  {\"id\":");
			long id = resultSet.getLong(1);
			builder.append(id);
			builder.append("},{\"name\":");
			String name = resultSet.getString(2);
			builder.append(name == null ? "null" : "\"" + name + "\"");
			builder.append("},{\"x\":");
			double x = resultSet.getDouble(3);
			builder.append(x);
			builder.append("},{\"y\":");
			double y = resultSet.getDouble(4);
			builder.append(y);
			builder.append("},{\"distance\":");
			double distance = resultSet.getDouble(5);
			builder.append(distance);
			builder.append("}");
			first = false;
		}
		builder.append("\n]");

		System.out.println(builder);

		connection.close();
	}

}
