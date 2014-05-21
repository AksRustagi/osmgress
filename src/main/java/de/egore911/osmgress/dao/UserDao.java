/*
 *   Copyright 2014 Christoph Brill
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package de.egore911.osmgress.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Nonnull;

import de.egore911.osmgress.ConnectionFactory;
import de.egore911.osmgress.model.Faction;
import de.egore911.osmgress.model.User;

public class UserDao {

	private static final String SELECT_PORTAL_BASE = "SELECT name, faction "
			+ "FROM osmg_user";

	private static final String WHERE_ID = " WHERE id = ?";

	public static User getById(Long id) {
		try (Connection connection = ConnectionFactory.getConnection()) {

			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_PORTAL_BASE + WHERE_ID)) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find User with id " + id);
					}
					User user = new User();
					user.setId(id);
					user.setName(resultSet.getString(1));
					user.setFaction(Faction.valueOf(resultSet.getString(2)));
					return user;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public User persist(User user) {
		if (user.getId() != null) {
			try (Connection connection = ConnectionFactory.getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement(
						"INSERT INTO osmg_user(name, faction) values (?, ?)",
						Statement.RETURN_GENERATED_KEYS)) {
					statement.setString(1, user.getName());
					statement.setString(2, user.getFaction().name());
					statement.executeUpdate();
					try (ResultSet resultSet = statement.getGeneratedKeys()) {
						if (!resultSet.next()) {
							throw new RuntimeException("Could not create User");
						}
						user.setId(resultSet.getLong(1));
						return user;
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			try (Connection connection = ConnectionFactory.getConnection()) {

				try (PreparedStatement statement = connection
						.prepareStatement("UPDATE osmg_user SET name = ?, faction = ? WHERE id = ?")) {
					statement.setString(1, user.getName());
					statement.setString(2, user.getFaction().name());
					statement.setLong(3, user.getId());
					int executeUpdate = statement.executeUpdate();
					if (executeUpdate == 0) {
						throw new RuntimeException("Could not update User");
					}
					return user;
				}
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	public static User convertToUser(@Nonnull ResultSet resultSet, int offset)
			throws SQLException {
		String username = resultSet.getString(3 + offset);
		if (username != null) {
			long ownerId = resultSet.getLong(1 + offset);
			String faction = resultSet.getString(2 + offset);
			User user = new User();
			user.setId(ownerId);
			user.setFaction(Faction.valueOf(faction));
			user.setName(username);
			return user;
		}
		return null;
	}
}
