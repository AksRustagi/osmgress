package de.egore911.osmgress.http;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionFilter implements Filter {

	private static final Logger LOG = LoggerFactory
			.getLogger(ConnectionFilter.class);

	private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(request, response);
		} finally {
			Connection connection = connectionHolder.get();
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			connectionHolder.remove();
		}
	}

	public static Connection getConnection() throws SQLException {
		Connection connection = connectionHolder.get();
		if (connection == null || connection.isClosed()) {
			try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				throw new SQLException(e);
			}

			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost/osmgress_mapnik", "osmgress",
					"osmgress");
			connectionHolder.set(connection);
		}
		return connection;
	}

	@Override
	public void destroy() {
	}

}
