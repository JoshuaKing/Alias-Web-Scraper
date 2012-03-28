package implementations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Log;

import interfaces.IPageController;
import interfaces.IPageScraper;

public class YoutubeScraper implements IPageScraper {
	IPageController page;
	private String file;
	private final Connection conn = MySqlConnection.getConnection();

	@Override
	public void scrape(IPageController page) {
		this.page = page;
		try {
			file = page.getFile();
		} catch (IOException e) {
			Log.warn("Could not retrieve file: " + e.getMessage());
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE links SET error='http' WHERE domain='http://gdata.youtube.com' AND path=?");
				update.setString(1, page.getPath());
				update.execute();
			} catch (SQLException se) {
				Log.fatal("Could not update error: " + se.getMessage(), 6535);
			}
			return;
		}

		
		if (page.getPath().startsWith("/feeds/api/standardfeeds")) {
			Log.info("Scraping Youtube Feed");
			page.startScrape();
			scrapeFeed();
		} else if (page.getPath().matches("/feeds/api/videos/[^</']+")) {
			Log.info("Scraping Youtube Video");
			page.startScrape();
			scrapeVideo();
		} else if (page.getPath().matches("/feeds/api/videos/[^</']+?/comments.*")) {
			Log.info("Scraping Youtube Comments");
			page.startScrape();
			scrapeComments();
		} else if (page.getPath().matches("/feeds/api/users/[^</']+")) {
			Log.info("Scraping Youtube User");
			page.startScrape();
			scrapeUser();
		} else {
			Log.fatal("Could not scrape URL " + page.getPath(), 9358);
			return;
		}
		page.finishScrape();
	}

	private void scrapeUser() {
		// Make sure user is in the database //
		Pattern p = Pattern.compile("<name>(.*?)</name>");
		Matcher m = p.matcher(file);
		if (!m.find()) {
			Log.warn("Could not detect username.");
			return;
		}
		String name = m.group(1);
		int contactId = insertContact(conn, name);
		Log.debug("User " + name + " results:");
		
		// Gender of User //
		p = Pattern.compile("<yt:gender>([fFmM])</yt:gender>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET gender=? WHERE contact_id=?");
				update.setString(1, m.group(1).toUpperCase());
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > Gender: " + (m.group(1).equalsIgnoreCase("M") ? "Male" : "Female"));
			} catch (SQLException e) {
				Log.fatal("Could not update contact gender: " + e.getMessage(), 9475);
			}
		}
		
		// Age of user //
		p = Pattern.compile("<yt:age>([0-9]+)</yt:age>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET age=? WHERE contact_id=?");
				update.setInt(1, Integer.valueOf(m.group(1)));
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > Age: " + m.group(1));
			} catch (SQLException e) {
				Log.fatal("Could not update contact age: " + e.getMessage(), 9476);
			}
		}
		
		// Location & Reverse-Lookup of iso_two from Countries table //
		p = Pattern.compile("<yt:location>.*?([A-Z]{2}).*?</yt:location>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null, select = null;
			try {
				select = conn.prepareStatement("SELECT country_id FROM countries WHERE iso_two LIKE ?");
				select.setString(1, m.group(1));
				ResultSet rs = select.executeQuery();
				if (rs.next()) {
					update = conn.prepareStatement("UPDATE contacts SET city_id=0, region_id=0, country_id=? WHERE contact_id=?");
					update.setInt(1, Integer.valueOf(rs.getInt(1)));
					update.setInt(2, contactId);
					update.execute();
					Log.debug(" > Location: " + m.group(1));
				}
			} catch (SQLException e) {
				Log.fatal("Could not update contact location: " + e.getMessage(), 9477);
			}
		}
		
		// First Name if available //
		p = Pattern.compile("<yt:firstName>([a-zA-Z]+)</yt:firstName>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET first_name=? WHERE contact_id=?");
				update.setString(1, m.group(1));
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > First Name: " + m.group(1));
			} catch (SQLException e) {
				Log.fatal("Could not update contact first name: " + e.getMessage(), 9478);
			}
		}
		
		// Last Name if available //
		p = Pattern.compile("<yt:lastName>([a-zA-Z]+)</yt:lastName>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET last_name=? WHERE contact_id=?");
				update.setString(1, m.group(1));
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > Last Name: " + m.group(1));
			} catch (SQLException e) {
				Log.fatal("Could not update contact last name: " + e.getMessage(), 9479);
			}
		}
		
		// Both Names - some one messed up //
		p = Pattern.compile("<yt:firstName>([a-zA-Z]+) ([a-zA-Z]+)</yt:firstName>");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET first_name=? WHERE contact_id=?");
				update.setString(1, m.group(1));
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > First Name: " + m.group(1));
				
				update = conn.prepareStatement("UPDATE contacts SET last_name=? WHERE contact_id=?");
				update.setString(1, m.group(2));
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > Last Name: " + m.group(2));
			} catch (SQLException e) {
				Log.fatal("Could not update contact name: " + e.getMessage(), 9480);
			}
		}
		
		// Email if available //
		p = Pattern.compile("([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+)");
		m = p.matcher(file);
		if (m.find()) {
			PreparedStatement update = null;
			try {
				update = conn.prepareStatement("UPDATE contacts SET email=? WHERE contact_id=?");
				update.setString(1, m.group(1).toLowerCase());
				update.setInt(2, contactId);
				update.execute();
				Log.debug(" > Email: " + m.group(1).toLowerCase());
			} catch (SQLException e) {
				Log.fatal("Could not update contact email: " + e.getMessage(), 9481);
			}
		}
		
		// store thumbnail location (or download cache)
		
		scrapeAccounts(name, contactId, file);
	}

	private void scrapeComments() {
		Pattern p = Pattern.compile("<name>(.*?)</name>");
		Matcher m = p.matcher(file);
		ArrayList<String> users = new ArrayList<String>();
		PreparedStatement sst = null, select = null, ist = null, insert = null;
		try {
			sst = conn.prepareStatement("SELECT data_id FROM minidata WHERE link=? AND contact_id=?");
			ist = conn.prepareStatement("INSERT INTO minidata(contact_id, link, human) VALUES (?, ?, ?)");
			select = conn.prepareStatement("SELECT id FROM links WHERE domain='http://gdata.youtube.com' AND path=?");
			insert = conn.prepareStatement("INSERT INTO links(alias, domain, path, rescan) VALUES (1, 'http://gdata.youtube.com', ?, 1440)");
		} catch (SQLException e) {
			Log.warn("SQL Error when trying to create prepared statements: " + e.getMessage());
		}
		
		String userlist = "";
		while (m.find()) {
			String name = m.group(1);
			if (users.contains(name)) {
				continue;
			}
			users.add(name);
			int contactId = insertContact(conn, name);
			
			// Youtube URL //
			String yt = "http://www.youtube.com/user/" + name;
			insertMinidata(contactId, yt, true, sst, ist);
			
			// Youtube API URL //
			String yta = "http://gdata.youtube.com/feeds/api/users/" + name;
			insertMinidata(contactId, yta, false, sst, ist);
			
			try {
				select.setString(1, "/feeds/api/users/" + name);
				ResultSet rs = select.executeQuery();
				if (!rs.next()) {
					insert.setString(1, "/feeds/api/users/" + name);
					insert.execute();
				}
				rs.close();
			} catch (SQLException e) {
				Log.fatal("SQL Error when trying to insert user link: " + e.getMessage(), 8933);
			}
			
			userlist += name + ", ";
		}
		if (userlist.length() > 0) {
			Log.debug("User accounts found: " + userlist);
		} else {
			Log.info("No User accounts found.");
		}
		// Find 'next' link and add it too //
		p = Pattern.compile("<link.*?rel='next'.*?href='http://gdata.youtube.com([^']+)'/>");
		m = p.matcher(file);
		if (m.find()) {
			String path = m.group(1);
			try {
				sst = conn.prepareStatement("SELECT id FROM links WHERE domain='http://gdata.youtube.com' AND path=?");
				ist = conn.prepareStatement("INSERT INTO links(alias, domain, path, rescan) VALUES (0, 'http://gdata.youtube.com', ?, 1440)");
				
				sst.setString(1, path);
				ResultSet rs = sst.executeQuery();
				if (!rs.next()) {
					ist.setString(1, path);
					ist.execute();
				}
				rs.close();
			} catch (SQLException e) {
				Log.warn("SQL Error when trying to insert 'next' link: " + e.getMessage());
			}
		}
	}
	
	private int insertContact(Connection c, String user) {
		int contactId = -1;
		PreparedStatement sst, ist;
		try {
			sst = c.prepareStatement("SELECT contact_id FROM contacts WHERE domain='youtube.com' AND alias=?");
			ist = c.prepareStatement("INSERT INTO contacts(domain, alias) VALUES ('youtube.com', ?)");
			
			sst.setString(1, user);
			ResultSet rs = sst.executeQuery();
			if (!rs.next()) {
				ist.setString(1, user);
				ist.execute();
				rs = sst.executeQuery();
				rs.next();
			}
			contactId = rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			Log.fatal("SQL Error when trying to insert contact: " + e.getMessage(), 4842);
		}
		return contactId;
	}

	private void scrapeVideo() {
		PreparedStatement sst = null, ist = null;
		try {
			sst = conn.prepareStatement("SELECT data_id FROM minidata WHERE link=? AND contact_id=?");
			ist = conn.prepareStatement("INSERT INTO minidata(contact_id, link, human) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			Log.warn("SQL Error when trying to create prepared statements: " + e.getMessage());
		}
		
		Pattern p = Pattern.compile("<author><name>([^<]+)</name><uri>http://gdata.youtube.com([^<]+)</uri>");
		Matcher m = p.matcher(file);
		m.find();
		String author = m.group(1);
		String yta = "http://gdata.youtube.com" + m.group(2);
		
		int contactId = insertContact(conn, author);
		
		// Youtube API URL //
		insertMinidata(contactId, yta, false, sst, ist);
		
		try {
			sst = conn.prepareStatement("SELECT id FROM links WHERE domain='http://gdata.youtube.com' AND path=?");
			ist = conn.prepareStatement("INSERT INTO links(alias, domain, path, rescan) VALUES (1, 'http://gdata.youtube.com', ?, 1440)");
			
			sst.setString(1, m.group(2));
			ResultSet rs = sst.executeQuery();
			if (!rs.next()) {
				ist.setString(1, m.group(2));
				ist.execute();
			}
			rs.close();
		} catch (SQLException e) {
			Log.fatal("SQL Error when trying to insert user link: " + e.getMessage(), 8933);
		}
		
		// Scrape other accounts //
		scrapeAccounts(author, contactId, file);
	}
	
	private void scrapeAccounts(String author, int contactId, String search) {
		PreparedStatement select = null, insert = null;
		try {
			select = conn.prepareStatement("SELECT data_id FROM minidata WHERE link=? AND contact_id=?");
			insert = conn.prepareStatement("INSERT INTO minidata(contact_id, link, human) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			Log.warn("SQL Error when trying to create prepared statements: " + e.getMessage());
			return;
		}
		
		// Youtube URL //
		String yt = "http://www.youtube.com/user/" + author;
		insertMinidata(contactId, yt, true, select, insert);
		
		// Facebook URL extraction //
		Pattern p = Pattern.compile("facebook\\.com/([a-z0-9?=&._-]+)", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(search);
		while (m.find()) {
			String fb = "http://www.facebook.com/" + m.group(1);
			insertMinidata(contactId, fb, true, select, insert);
			Log.info("Facebook account for " + author + ": " + fb + "\n  > Matched: " + m.group());
		}
		
		// Twitter URL extraction //
		p = Pattern.compile("twitter\\.com.{0,3}/([a-z0-9_]+)", Pattern.CASE_INSENSITIVE);
		m = p.matcher(search);
		while (m.find()) {
			String tw = "http://www.twitter.com/" + m.group(1);
			insertMinidata(contactId, tw, true, select, insert);
			Log.info("Twitter account for " + author + ": " + tw + "\n  > Matched: " + m.group());
		}
		
		// Google+ URL extraction //
		p = Pattern.compile("plus\\.google\\.com/([0-9]+)", Pattern.CASE_INSENSITIVE);
		m = p.matcher(search);
		while (m.find()) {
			String gp = "https://plus.google.com/" + m.group(1) + "/";
			insertMinidata(contactId, gp, true, select, insert);
			Log.info("Google+ account for " + author + ": " + gp);
		}
		
		// Me2day URL extraction //
		p = Pattern.compile("me2day\\.net/([a-z0-9._-]+)", Pattern.CASE_INSENSITIVE);
		m = p.matcher(search);
		while (m.find()) {
			String me = "http://www.me2day.net/" + m.group(1);
			insertMinidata(contactId, me, true, select, insert);
			Log.debug("Me2day account for " + author + ": " + me);
		}
		
		// Deviant Art URL extraction //
		p = Pattern.compile("([a-z0-9]+)\\.deviantart\\.com", Pattern.CASE_INSENSITIVE);
		m = p.matcher(search);
		while (m.find()) {
			String da = "http://" + m.group(1) + ".deviantart.com/";
			insertMinidata(contactId, da, true, select, insert);
			Log.info("Deviant Art account for " + author + ": " + da + "\n  > Matched: " + m.group());
		}
		
		// TODO: Email, StackOverflow, Whirlpool
	}
	
	private boolean insertMinidata(int contact, String link, boolean human, PreparedStatement select, PreparedStatement insert) {
		try {
			select.setString(1, link);
			select.setInt(2, contact);
			ResultSet rs = select.executeQuery();
			if (!rs.next()) {
				insert.setInt(1, contact);
				insert.setString(2, link);
				insert.setInt(3, human ? 1 : 0);
				insert.execute();
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private void scrapeFeed() {
		ArrayList<String> videos = new ArrayList<String>();
		Pattern p = Pattern.compile("http://gdata.youtube.com(/feeds/api/videos/[^</']+)");
		Matcher m = p.matcher(file);

		Connection conn = MySqlConnection.getConnection();
		PreparedStatement sst = null, ist = null;
		try {
			sst = conn.prepareStatement("SELECT id FROM links WHERE domain='http://gdata.youtube.com' AND path=?");
			ist = conn.prepareStatement("INSERT INTO links(alias, domain, path, rescan) VALUES (0, 'http://gdata.youtube.com', ?, 1440)");
		} catch (SQLException e) {
			Log.warn("SQL Error when trying to insert link: " + e.getMessage());
		}
		
		while (m.find()) {
			String path = m.group(1);
			if (videos.contains(path)) {
				continue;
			}
			videos.add(path);
			Log.info("Video at " + m.group() + " found.");
			try {
				sst.setString(1, path);
				ResultSet rs = sst.executeQuery();
				if (!rs.next()) {
					ist.setString(1, path);
					ist.execute();
				}
				rs.close();
				
				sst.setString(1, path + "/comments?max-results=50");
				rs = sst.executeQuery();
				if (!rs.next()) {
					ist.setString(1, path + "/comments?max-results=50");
					ist.execute();
				}
				rs.close();
			} catch (SQLException e) {
				Log.warn("Could not add link: " + e.getMessage());
			}
		}
		
		// Find 'next' link and add it too //
		p = Pattern.compile("<link.*?rel='next'.*?href='http://gdata.youtube.com([^']+)'/>");
		m = p.matcher(file);
		if (m.find()) {
			String path = m.group(1);
			try {
				sst.setString(1, path);
				ResultSet rs = sst.executeQuery();
				if (!rs.next()) {
					ist.setString(1, path);
					ist.execute();
				}
				rs.close();
			} catch (SQLException e) {
				Log.warn("SQL Error when trying to insert 'next' link: " + e.getMessage());
			}
		}
	}

}
