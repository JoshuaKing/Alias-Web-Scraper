package implementations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Log;
import interfaces.IDomainController;

public class DomainController implements IDomainController {
	private String domain;
	private ArrayList<String> rules;
	private URLConnection conn;
	
	public DomainController(String domain) throws NullPointerException {
		if (domain == null) throw new NullPointerException();
		this.domain = domain;
		rules = new ArrayList<String>();
		if (!connect()) return;
		parseRobots();
	}
	
	private boolean connect() {
		try {
			conn = new URL(domain + "/robots.txt").openConnection();
			conn.setRequestProperty("User-Agent", USER_AGENT);
			Log.debug("Connected to " + domain + "/robots.txt");
			return true;
		} catch (MalformedURLException e) {
			Log.warn("Invalid domain: " + domain);
			return false;
		} catch (IOException e) {
			Log.warn("Could not open connection.");
			return false;
		}
	}
	
	private void parseRobots() {
		try {			
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String input_line;
			Pattern pattern_disallow = Pattern.compile("Disallow:(.*)", Pattern.CASE_INSENSITIVE);
			while ((input_line = in.readLine()) != null) {
				if (!input_line.trim().matches("(?i)User-agent: [*|" + USER_AGENT + "]")) {
					continue;
				}
				while ((input_line = in.readLine()) != null) {
					Matcher matcher = pattern_disallow.matcher(input_line.trim());
					while (matcher.find()) {
						if (matcher.group(1).trim() == "") {
							rules.clear();
							Log.debug("Robots.txt file parsed: Found empty Disallow.");
							return;
						}
						this.rules.add(matcher.group(1).trim().replace("*", "\\*").replace("+","\\+"));
					}
				}
			}
			Log.debug("Robots.txt file parsed. Rules: " + rules.size());
			return;
		
		} catch (IOException e) {
			Log.info("Robots.txt not found.");
			return;
		}
	}

	@Override
	public boolean robotsAllowed(String path) {
		for (String regex : this.rules)
			try {
				if (path.matches(regex + "\\W.*")) {//) || path.matches(regex)) {
					Log.info("Robots NOT allowed at " + domain + path);
					return false;
				}
				
			
			} catch (Exception e) {
				Log.warn("Robots.txt parsed caused an error: " + e.getMessage());
				continue;
			}
		
		Log.info("Robots allowed at " + domain + path);
		return true;
	}

	@Override
	public String getDomain() {
		return domain;
	}

}
