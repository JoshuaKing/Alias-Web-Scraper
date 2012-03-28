package scraper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import lib.Log;
import implementations.MySqlConnection;
import implementations.ScrapeController;

public class Manager {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Hello, World!\n");
		
		// TODO: Fix up http.agent to be a proper user agent string
		System.setProperty("http.agent", "");
		
		// TODO: Scrape:
		// - youtube.com
		// - [forum.]koohii.com
		// - [bbs.]archlinux.com
		// - [bodyspace.]bodybuilding.com
		// - [news.]ycombinator.com
		// - engadget.com
		// - [answers.]yahoo.com
		// - reddit.com (just aliases)
		// - twitter.com (robots.txt restrictions though)
		// - TPB.se (scrape top 100 torrent's for comments by users)
		
		HashMap<String, ScrapeController> hm = new HashMap<String, ScrapeController>();
		
		Connection conn = MySqlConnection.getConnection();
		try {
			String sql = "SELECT domain, path "
						+ "FROM links "
						+ "WHERE (error='notapplicable' OR error='none') "
						+ "AND TIMESTAMPADD(MINUTE,rescan,completed) < NOW() "
						+ "ORDER BY alias DESC, completed ASC LIMIT 20";
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();
			
			while (rs.next()) {
				String domain = rs.getString(1);
				String path = rs.getString(2);
				Log.info("Checking URL " + domain + path);
				if (!hm.containsKey(domain)) {
					hm.put(domain, new ScrapeController(domain));
				}
				hm.get(domain).addPath(path);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//ScrapeController s = new ScrapeController("http://ubuntuforums.org");
		//s.addPath("/member.php?u=782844");
		//ScrapeController s2 = new ScrapeController("http://bodyspace.bodybuilding.com");
		//s2.addPath("/ban_dit");
		Log.info("Manager> Finished");
	}

}
