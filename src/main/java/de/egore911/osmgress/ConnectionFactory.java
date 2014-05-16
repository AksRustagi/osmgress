package de.egore911.osmgress;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionFactory {

	public static Connection getConnection() {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			DataSource dataSource = (DataSource) envContext
					.lookup("jdbc/osmgress_mapnik");
			Connection connection = dataSource.getConnection();
			return connection;
		} catch (NamingException | SQLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
