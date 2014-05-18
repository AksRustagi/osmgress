package de.egore911.osmgress.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.postgresql.util.PGobject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.egore911.osmgress.ConnectionFactory;
import de.egore911.osmgress.model.Portal;
import de.egore911.osmgress.model.Slot;
import de.egore911.osmgress.model.User;

public class PortalDao {

	private static final String SELECT_PORTAL_BASE = "SELECT osmg_portal.id, "
			+ "osmg_portal.name, "
			+ "ST_Y(ST_Transform(way, 4326)) as latitude, "
			+ "ST_X(ST_Transform(way, 4326)) as longitude, "
			+ "osmg_portal.slots, "
			+ "osmg_portal.owner_id, "
			+ "osmg_user.faction, "
			+ "osmg_user.name username "
			+ "FROM osmg_portal "
			+ "LEFT OUTER JOIN osmg_user ON osmg_portal.owner_id = osmg_user.id";

	private static final String WHERE_ID = " WHERE osmg_portal.id = ?";

	private static final String WHERE_WITHIN = " WHERE ST_Y(ST_Transform(way, 4326)) > ? "
			+ "AND ST_Y(ST_Transform(way, 4326)) < ? "
			+ "AND ST_X(ST_Transform(way, 4326)) > ? "
			+ "AND ST_X(ST_Transform(way, 4326)) < ?";

	public static Portal getById(Long id) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_PORTAL_BASE + WHERE_ID)) {
				statement.setLong(1, id);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (!resultSet.next()) {
						throw new RuntimeException(
								"Could not find Portal with id " + id);
					}
					return convertToPortal(resultSet, 0);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Collection<Portal> getWithin(double lat_min, double lat_max,
			double lon_min, double lon_max) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement(SELECT_PORTAL_BASE + WHERE_WITHIN)) {

				statement.setDouble(1, lat_min);
				statement.setDouble(2, lat_max);
				statement.setDouble(3, lon_min);
				statement.setDouble(4, lon_max);

				Collection<Portal> portals = new ArrayList<>();
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						portals.add(convertToPortal(resultSet, 0));
					}
				}
				return portals;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static Portal convertToPortal(@Nonnull ResultSet resultSet,
			int offset) throws SQLException {
		Portal portal = new Portal();
		long id = resultSet.getLong(1 + offset);
		portal.setId(id);
		String name = resultSet.getString(2 + offset);
		portal.setName(name);
		double latitude = resultSet.getDouble(3 + offset);
		portal.setLatitude(latitude);
		double longitude = resultSet.getDouble(4 + offset);
		portal.setLongitude(longitude);
		String slots = resultSet.getString(5 + offset);
		Collection<Slot> fromJson = null;
		if (slots != null) {
			Type type = new TypeToken<Collection<Slot>>() {
			}.getType();
			fromJson = new Gson().fromJson(slots, type);
		}
		portal.setSlots(fromJson);

		portal.setOwner(UserDao.convertToUser(resultSet, 5 + offset));
		return portal;
	}

	public static Portal own(Portal portal, User owner) {
		try (Connection connection = ConnectionFactory.getConnection()) {
			try (PreparedStatement statement = connection
					.prepareStatement("UPDATE osmg_portal SET owner_id = ?, slots = ? WHERE id = ?")) {

				statement.setLong(1, owner.getId());
				PGobject slotsJson = new PGobject();
				slotsJson.setType("json");
				slotsJson.setValue(new Gson().toJson(portal.getSlots()));
				statement.setObject(2, slotsJson);
				statement.setLong(3, portal.getId());
				int count = statement.executeUpdate();

				if (count == 0) {
					throw new IllegalArgumentException("Could not own portal");
				} else if (count > 1) {
					throw new IllegalArgumentException(
							"Owned more than one portal");
				}
				portal.setOwner(owner);
				return portal;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
