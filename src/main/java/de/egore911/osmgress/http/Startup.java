/*
 *   Copyright 2014 Christoph Brill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

	private static final String CREATE_MISSING_PORTALS_POINTS = "INSERT INTO osmg_portal (osm_id, name, way) "
			+ "SELECT osm_id, name, way "
			+ "FROM planet_osm_point "
			+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy' OR amenity='restaurant' OR amenity='school' OR shop is not null) "
			+ "AND osm_id NOT IN (SELECT osm_id FROM osmg_portal)";
	private static final String CREATE_MISSING_PORTALS_POLYGONS = "INSERT INTO osmg_portal (osm_id, name, way) "
			+ "SELECT osm_id, name, ST_GeomFromText('POINT(' || ((ST_YMax(ST_Transform(way, 4326)) - ST_YMin(ST_Transform(way, 4326))) / 2) + ST_YMin(ST_Transform(way, 4326)) || ' ' || ((ST_XMax(ST_Transform(way, 4326)) - ST_XMin(ST_Transform(way, 4326))) / 2) + ST_XMin(ST_Transform(way, 4326)) || ')', 3857) "
			+ "FROM planet_osm_polygon "
			+ "WHERE (amenity='place_of_worship' OR amenity='hospital' OR amenity='pharmacy' OR amenity='restaurant' OR amenity='school' OR shop is not null) "
			+ "AND osm_id NOT IN (SELECT osm_id FROM osmg_portal) "
			+ "AND osm_id > 0";

	@Override
	public void contextInitialized(ServletContextEvent event) {

		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(CREATE_MISSING_PORTALS_POINTS)) {
				statement.execute();
			}
			try (PreparedStatement statement = connection
					.prepareStatement(CREATE_MISSING_PORTALS_POLYGONS)) {
				statement.execute();
			}

			// TODO delete portals and links no longer in osm_point and
			// osm_polygon
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
