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
package de.egore911.osmgress.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import de.egore911.osmgress.ConnectionFactory;
import de.egore911.osmgress.model.Link;
import de.egore911.osmgress.model.Portal;
import de.egore911.osmgress.model.User;

public class LinkDao {

	private static final String SELECT_LINK_BASE = "SELECT osmg_link.id, "
			+ "source.id AS source_id, "
			+ "source.name AS source_name, "
			+ "ST_Y(ST_Transform(source.way, 4326)) AS source_latitude, "
			+ "ST_X(ST_Transform(source.way, 4326)) AS source_longitude, "
			+ "source.slots, "
			+ "source.owner_id, "
			+ "source_owner.faction, "
			+ "source_owner.name username, "
			+ "target.id AS target_id, "
			+ "target.name AS target_name, "
			+ "ST_Y(ST_Transform(target.way, 4326)) AS target_latitude, "
			+ "ST_X(ST_Transform(target.way, 4326)) AS target_longitude, "
			+ "target.slots, "
			+ "target.owner_id, "
			+ "target_owner.faction, "
			+ "target_owner.name username, "
			+ "osmg_link.owner_id, "
			+ "osmg_user.faction, "
			+ "osmg_user.name username "
			+ "FROM osmg_link "
			+ "INNER JOIN osmg_portal source ON osmg_link.source_id = source.id "
			+ "INNER JOIN osmg_user source_owner ON source.owner_id = source_owner.id "
			+ "INNER JOIN osmg_portal target ON osmg_link.target_id = target.id "
			+ "INNER JOIN osmg_user target_owner ON target.owner_id = target_owner.id "
			+ "INNER JOIN osmg_user ON osmg_link.owner_id = osmg_user.id";

	private static final String WHERE_ID = " WHERE osmg_link.id = ?";

	private static final String WHERE_SOURCE_TARGET = " WHERE source.id = ? and target.id = ?";

	private static final String WHERE_WITHIN = " WHERE (ST_Y(ST_Transform(source.way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(source.way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(source.way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(source.way, 4326)) < ?) "
			+ "OR (ST_Y(ST_Transform(target.way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(target.way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(target.way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(target.way, 4326)) < ?)";

	public static Link getById(Long id) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_LINK_BASE + WHERE_ID)) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find Portal with id " + id);
					}
					return convertToLink(resultSet);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Collection<Link> getWithin(double lat_min, double lat_max,
			double lon_min, double lon_max) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_LINK_BASE + WHERE_WITHIN)) {

				statement.setDouble(1, lat_min);
				statement.setDouble(2, lat_max);
				statement.setDouble(3, lon_min);
				statement.setDouble(4, lon_max);
				statement.setDouble(5, lat_min);
				statement.setDouble(6, lat_max);
				statement.setDouble(7, lon_min);
				statement.setDouble(8, lon_max);

				Collection<Link> links = new ArrayList<>();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						links.add(convertToLink(resultSet));
					}
				}
				return links;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Link findBySourceTarget(Portal source, Portal target) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_LINK_BASE + WHERE_SOURCE_TARGET)) {
				statement.setLong(1, source.getId());
				statement.setLong(2, target.getId());
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						return null;
					}
					return convertToLink(resultSet);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Link create(Portal source, Portal target, User owner) {
		long id;
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(
							"INSERT INTO osmg_link (source_id, target_id, owner_id) VALUES (?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS)) {
				statement.setLong(1, source.getId());
				statement.setLong(2, target.getId());
				statement.setLong(3, owner.getId());
				statement.execute();

				try (ResultSet resultSet = statement.getGeneratedKeys()) {
					if (!resultSet.next()) {
						throw new IllegalArgumentException(
								"Could not create link");
					}
					id = resultSet.getLong(1);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return getById(id);
	}

	private static Link convertToLink(@Nonnull ResultSet resultSet)
			throws SQLException {
		Link link = new Link();
		long id = resultSet.getLong(1);
		link.setId(id);

		link.setSource(PortalDao.convertToPortal(resultSet, 1));
		link.setTarget(PortalDao.convertToPortal(resultSet, 9));
		link.setOwner(UserDao.convertToUser(resultSet, 17));

		return link;
	}

}
