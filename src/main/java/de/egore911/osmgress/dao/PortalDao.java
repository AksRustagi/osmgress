package de.egore911.osmgress.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.egore911.osmgress.ConnectionFactory;
import de.egore911.osmgress.model.Portal;

public class PortalDao {

	public static Portal getById(Long id) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement("SELECT owner_id FROM osmg_portal WHERE osm_id = ?")) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.getResultSet()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find Portal with id " + id);
					}
					Portal portal = new Portal();
					portal.setOsmId(id);
					portal.setOwnerId(resultSet.getLong(1));
					return portal;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
