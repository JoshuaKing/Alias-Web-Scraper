package implementations;

import interfaces.IAlias;
import interfaces.ILocation;
import interfaces.INationalityScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Log;

public class ScrapeKeywords {
	public static HashMap<Integer, INationalityScore> getLocalisms(String file) {
		HashMap<Integer, INationalityScore> map = new HashMap<Integer, INationalityScore>();
		Connection conn = MySqlConnection.connect();
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM localisms");
			while (rs.next()) {
				String word = rs.getString("word");
				Pattern p = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(file);
				while (m.find()) {
					int uk = rs.getInt("uk"), us = rs.getInt("us");
					int au = rs.getInt("au");
					map.put(new Integer(m.start()), NationalityScore.score(uk, us, au));
				}
			}
		} catch (SQLException e) {
			Log.fatal("MySQL Error when searching for localisms.", 3813);
		}
		return map;
	}
	
	public static HashMap<Integer, ILocation> getLocation(String file) {
		HashMap<Integer, ILocation> locations = new HashMap<Integer, ILocation>();
		Log.debug("Searching for \"Location\" or \"from\"");
		Pattern p = Pattern.compile("[^\\w.](location|from)\\W?[:]?\\W?\\|([^\\|]*)\\|", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(file);
		
		Connection conn = MySqlConnection.connect();
		while (m.find()) {
			String substr = m.group(2).trim();
			Log.debug("Searching near " + substr);
			
			if (substr.contains(",")) {
				String[] chunks = substr.split(",");

				Location loc = new Location();
				int found_at = m.start();
				for (String search : chunks) {
					Location loc2 = new Location();
					
					if (searchLocation(conn, search.trim(), loc2)) {
						if (!loc.merge(loc2)) {
							if (!loc.isEmpty()) locations.put(found_at, loc);
							found_at++;
							loc = loc2;
						}
					}
				}
				if (!loc.isEmpty()) locations.put(found_at, loc);
				return locations;
			}
			
			Pattern p2 = Pattern.compile("([a-z]+)[\\s,]*?", Pattern.CASE_INSENSITIVE);
			Matcher m2 = p2.matcher(substr);
			
			// For each word //
			Location loc = new Location();
			int found_at = -1;
			while (m2.find()) {
				if (found_at == -1) found_at = m.start() + m2.start();
				
				String search = m2.group(1).trim();
				Location loc2 = new Location();
				
				if (searchLocation(conn, search, loc2)) {
					if (!loc.merge(loc2)) {
						if (!loc.isEmpty()) locations.put(found_at, loc);
						found_at = m.start() + m2.start();
						loc = loc2;
					}
				}
			}
			if (!loc.isEmpty()) locations.put(found_at, loc);
		}
		
		return locations;
	}
	
	private static boolean searchLocation(Connection conn, String search, Location loc) {
		boolean returnval = false;
		try {
			if (search.length() >= 2 && search.toUpperCase().equals(search));
			else if (search.length() <= 4) return returnval;
			//Log.debug("searching for " + search);
			
			// Find out if word is a country //
			PreparedStatement st = conn.prepareStatement("SELECT * FROM countries WHERE (country LIKE ? OR iso_two LIKE ? OR iso_three LIKE ?) ORDER BY population LIMIT 1");
			st.setString(1, search);
			st.setString(2, search);
			st.setString(3, search);
			
			ResultSet rs = st.executeQuery();
			if (rs.first()) {
				returnval = true;
				loc.setCountry(rs.getInt("country_id"), rs.getLong("population"));
				Log.debug("Location country found: " + rs.getString("country"));
			}
			
			// Find out if word is a region //
			String query2 = "SELECT regions.*, population, country FROM regions, countries WHERE ";
			query2 += "((region LIKE ? OR code LIKE ?) AND regions.country_id = countries.country_id) ";
			query2 += "ORDER BY population LIMIT 1";
			st = conn.prepareStatement(query2);
			st.setString(1, search);
			st.setString(2, search);
			
			rs = st.executeQuery();
			if (rs.first()) {
				returnval = true;
				loc.setRegion(rs.getInt("region_id"), rs.getInt("country_id"), rs.getLong("population"));
				Log.debug("Location region found: " + rs.getString("region") + " - in " + rs.getString("country"));
			}
			
			// Find out if word is a city //
			String query3 = "SELECT cities.*, population, country FROM cities, countries WHERE ";
			query3 += "((city LIKE ? OR code LIKE ?) AND cities.country_id = countries.country_id) ";
			query3 += "ORDER BY population LIMIT 1";
			st = conn.prepareStatement(query3);
			st.setString(1, search);
			st.setString(2, search);
			
			rs = st.executeQuery();
			if (rs.first()) {
				returnval = true;
				loc.setCity(rs.getInt("city_id"), rs.getInt("region_id"), rs.getInt("country_id"), rs.getLong("population"));
				Log.debug("Location city found: " + rs.getString("city") + " - in " + rs.getString("country") + ". Population " + rs.getLong("population"));
			}			
		} catch (SQLException e) {
			Log.fatal("MySQL Error when searching for location details: " + e.getMessage(), 3516); 
		}
		return returnval;
	}
	
	public static HashMap<Integer, IAlias.Gender> getGender(String file) {
		HashMap<Integer, IAlias.Gender> genders = new HashMap<Integer, IAlias.Gender>();
		
		Log.debug("Searching for \"Gender\" or \"Sex\"");
		Pattern p = Pattern.compile("gender|sex", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(file);
		
		while (m.find()) {
			String substr = file.substring(m.end(), m.end() + 32).trim();
			
			if (substr.matches("(?i).*\\Wfemale\\W.*")) {
				Log.debug("Female");
				genders.put(m.start(), IAlias.Gender.Female);
			} else if (substr.matches("(?i).*\\Wmale\\W.*")) {
				Log.debug("Male");
				genders.put(m.start(), IAlias.Gender.Male);
			}
		}
		
		return genders;
	}
	
	public static HashMap<Integer, Integer> getAge(String file) {
		HashMap<Integer, Integer> ages = new HashMap<Integer, Integer>();
		
		Log.debug("Searching for \"Age\"");
		Pattern p = Pattern.compile("\\Wage\\W", Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("\\W?([1-5][0-9])\\W");
		Matcher m = p.matcher(file);
		
		while (m.find()) {
			String substr = file.substring(m.end(), m.end() + 32).trim();
			Matcher m2 = p2.matcher(substr);
			if (!m2.find()) continue;
			Integer age = Integer.valueOf(m2.group(1));
			ages.put(m2.start(), age);
			Log.debug("Age " + age);
		}
		
		return ages;
	}

	public static HashMap<Integer, String> getEmails(String file) {
		HashMap<Integer, String> emails = new HashMap<Integer, String>();
		Pattern p = Pattern.compile("([\\w._-]{5,}@[\\w._-]{5,})", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(file);
		
		while (m.find()) {
			String email = m.group();
			emails.put(m.start(), email);
		}
		
		Pattern p2 = Pattern.compile("([\\w._-]{5,}) [\\[{(]?at[\\]})]? ([\\w._-]{4,}\\.[a-z._-]+)", Pattern.CASE_INSENSITIVE);
		Matcher m2 = p2.matcher(file);
		
		while (m2.find()) {
			String email = m2.group(1) + "@" + m2.group(2);
			emails.put(m2.start(), email);
		}
		
		Pattern p3 = Pattern.compile("([\\w._-]{5,}) [\\[{(]?at[\\]})]? ([\\w._-]{4,}) [\\[{(]?dot[\\]})]? ([\\w._-]{2,})", Pattern.CASE_INSENSITIVE);
		Matcher m3 = p3.matcher(file);
		
		while (m3.find()) {
			String email = m3.group(1) + "@" + m3.group(2) + "." + m3.group(3);
			emails.put(m3.start(), email);
		}
		return emails;
	}
}
