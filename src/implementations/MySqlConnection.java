package implementations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lib.Log;

import interfaces.IMySqlConnection;

public class MySqlConnection implements IMySqlConnection {
	public static Connection connect(String username, String password, String domain,
			String database) {
		String url = "jdbc:mysql://" + domain + "/" + database;
		try {
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			Log.fatal("Could not connect to MySQL server: " + e.getMessage(), 4748);
		}
		return null;
	}

	public static Connection connect() {
		return connect("usernamehere", "passwordhere", "localhost:3306", "scraper");
	}
}
