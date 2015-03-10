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

import javax.annotation.Nonnull;

import de.egore911.osmgress.ConnectionFactory;
import de.egore911.osmgress.model.Faction;
import de.egore911.osmgress.model.User;

public class UserDao {

	private static final String SELECT_USER_BASE = "SELECT id, name, faction "
			+ "FROM osmg_user";

	private static final String WHERE_ID = " WHERE id = ?";
	private static final String WHERE_NAME = " WHERE name = ?";

	public static User getById(Long id) {
		try (Connection connection = ConnectionFactory.getConnection()) {

			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_USER_BASE + WHERE_ID)) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find User with id " + id);
					}
					User user = new User();
					user.setId(resultSet.getLong(1));
					user.setName(resultSet.getString(2));
					user.setFaction(Faction.valueOf(resultSet.getString(3)));
					return user;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static User getByName(String name) {
		try (Connection connection = ConnectionFactory.getConnection()) {

			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_USER_BASE + WHERE_NAME)) {
				statement.setString(1, name);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find User with name " + name);
					}
					User user = new User();
					user.setId(resultSet.getLong(1));
					user.setName(resultSet.getString(2));
					user.setFaction(Faction.valueOf(resultSet.getString(3)));
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
