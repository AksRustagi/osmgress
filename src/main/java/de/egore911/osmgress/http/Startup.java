package de.egore911.osmgress.http;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.egore911.osmgress.ConnectionFactory;

public class Startup implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(Startup.class);

	private static final String CREATE_MISSING_PORTALS = "INSERT INTO osmg_portal (osm_id) "
			+ "SELECT osm_id "
			+ "FROM planet_osm_point "
			+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy') "
			+ "AND osm_id NOT IN (SELECT osm_id FROM osmg_portal) "
			+ "UNION "
			+ "SELECT osm_id "
			+ "FROM planet_osm_polygon "
			+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy') "
			+ "AND osm_id NOT IN (SELECT osm_id FROM osmg_portal)";

	@Override
	public void contextInitialized(ServletContextEvent event) {

		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(CREATE_MISSING_PORTALS)) {
				statement.execute();
			}

			// TODO delete portals and links no longer in osm_point and osm_polygon
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
