package de.egore911.osmgress.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.egore911.osmgress.http.ConnectionFilter;
import de.egore911.osmgress.model.Link;

public class LinkDao {

	public static Link getById(Long id) {
		try {
			try (PreparedStatement statement = ConnectionFilter
					.getConnection()
					.prepareStatement(
							"SELECT source_osm_id, target_osm_id FROM osmg_link WHERE id = ?")) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.getResultSet()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find Portal with id " + id);
					}
					Link link = new Link();
					link.setId(id);
					link.setSourceOsmId(resultSet.getLong(1));
					link.setTargetOsmId(resultSet.getLong(2));
					return link;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
}
