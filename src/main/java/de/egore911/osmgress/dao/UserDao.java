package de.egore911.osmgress.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.egore911.osmgress.http.ConnectionFilter;
import de.egore911.osmgress.model.Faction;
import de.egore911.osmgress.model.User;

public class UserDao {

	public static User getById(Long id) {
		try {
			try (PreparedStatement statement = ConnectionFilter.getConnection()
					.prepareStatement(
							"SELECT name, faction FROM osmg_user WHERE id = ?")) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.getResultSet()) {
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
			try {
				try (PreparedStatement statement = ConnectionFilter
						.getConnection()
						.prepareStatement(
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
			try {
				try (PreparedStatement statement = ConnectionFilter
						.getConnection()
						.prepareStatement(
								"UPDATE osmg_user SET name = ?, faction = ? WHERE id = ?")) {
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
}