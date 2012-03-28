package implementations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import exceptions.RobotsNotAllowed;

import lib.Log;
import interfaces.IPageController;
import interfaces.IPageScraper;

public class PageController implements IPageController {
	private String domain, path, file;
	private URLConnection conn = null;
	
	public PageController(DomainController domain, String path) throws RobotsNotAllowed {
		if (!domain.robotsAllowed(path)) throw new RobotsNotAllowed();
		this.domain = domain.getDomain();
		this.path = path;
		file = null;
	}
	
	@Override
	public String getFile() throws IOException {
		if (file != null) return file;
		if (!connect()) throw new IOException("Connection Error.");
		if (!retrieve()) throw new IOException("Retrieval of file failed.");
		return file;
	}

	@Override
	public boolean validHeader() {
		String content_type = this.conn.getContentType();
		if (!content_type.matches("text/html\\s*(;.*)?") && !content_type.equals("text/plain")) {
			Log.warn("Content-Type " + content_type + " is invalid.");
			return false;
		}
		
		if (conn.getContentLength() > MAX_LENGTH_KB) {
			Log.warn("Content-Length " + conn.getContentLength() + " is too large.");
			return false;
		}
		
		Log.debug("Content-Type: " + this.conn.getContentType());
		Log.debug("Content-Length: " + this.conn.getContentLength());
		return true;
	}

	private boolean connect() {
		try {
			conn = new URL(domain + path).openConnection();
			conn.setRequestProperty("User-Agent", USER_AGENT);
			Log.debug("Connected to " + domain + path);
			return true;
		} catch (MalformedURLException e) {
			Log.warn("Invalid domain: " + domain);
			return false;
		} catch (IOException e) {
			Log.warn("Could not open connection.");
			return false;
		}
	}
	
	private Boolean retrieve() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String input_line;
			while ((input_line = in.readLine()) != null) file += input_line;
			return true;			
		
		} catch (IOException e) {
			Log.warn("Could not retrieve file: " + e.getMessage());
			return false;
		}
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public IPageScraper getScraper() {
		if (domain.contains("youtube.com")) {
			return new YoutubeScraper();
		}
		return new PageScraper();
	}

	@Override
	public void finishScrape() {
		Connection c = MySqlConnection.getConnection();
		PreparedStatement st;
		try {
			st = c.prepareStatement("UPDATE links SET completed=NOW() WHERE domain=? AND path=?");
			st.setString(1, domain);
			st.setString(2, path);
			st.execute();
		} catch (SQLException e) {
			Log.fatal("Could not mark link as complete: " + e.getMessage(), 2930);
		}
	}

	@Override
	public void startScrape() {
		Connection c = MySqlConnection.getConnection();
		PreparedStatement st;
		try {
			st = c.prepareStatement("UPDATE links SET started=NOW() WHERE domain=? AND path=?");
			st.setString(1, domain);
			st.setString(2, path);
			st.execute();
		} catch (SQLException e) {
			Log.warn("Could not mark link as started: " + e.getMessage());
		}
	}
}
