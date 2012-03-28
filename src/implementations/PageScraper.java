package implementations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lib.Log;
import interfaces.IPageController;
import interfaces.IPageScraper;

public class PageScraper implements IPageScraper {
	private IPageController page;
	private String file, notags;
	private ArrayList<HtmlLink> links;
	private ArrayList<Alias> aliases;
	public TreeMap<Integer, Alias> alias_locations;
	
	@Override
	public void scrape(IPageController page) {
		this.page = page;
		links = new ArrayList<HtmlLink>();
		aliases = new ArrayList<Alias>();
		alias_locations = new TreeMap<Integer, Alias>();
		try {
			file = page.getFile();
			notags = file.replaceAll("\n|\r", "").replaceAll("<!--.*?-->", " ");// "[SCRAPER:REMOVECOMMENT]");
			notags = notags.replaceAll("(?i)<style[^>]*>[^<]*</style>", " ");// "[SCRAPER:REMOVESTYLE]");
			notags = notags.replaceAll("(?is)<script[^>]*>.*?</script>", "|");// "[SCRAPER:REMOVESCRIPT]");
			notags = notags.replaceAll("<[^>]*>", "|");// "[SCRAPER:REMOVETAGS]");
			notags = notags.replaceAll("\\|\\s+", "|");
			notags = notags.replaceAll("\\|+", "|");
			notags = notags.replaceAll("\\s+", " ");
			Log.debug(notags);
			parseFile();
		} catch (IOException e) {
			file = "";
			notags = "";
		}
	}
	
	private void parseFile() {
		extractLinks();
		extractAliases();
		locateAliases();
		scrapeLocalisms();
		scrapeLocation();
		scrapeGender();
		scrapeAge();
		scrapeEmails();
		//TODO:
		// 1. Scrape names
		// 2. Alias links mean any heading:value pairs are relevant to
		// the alias in question, though emails and other aliases may be scraped
		// 3. Differentiate between an email and "Email: xxx@yyy"
		// 4. Store results (Including which aliases where found on a page)
		// 5. Scrape Occupation:xxx
		// 6. Recurse
		
	}

	private void scrapeEmails() {
		Log l = new Log("Scraping emails");
		HashMap<Integer, String> keywords = ScrapeKeywords.getEmails(notags);
		Set<Integer> keys = keywords.keySet();
		for (Integer key : keys) {
			Entry<Integer, Alias> closest = alias_locations.floorEntry(key);
			if (closest == null) continue;
			Alias alias =  closest.getValue();
			alias.setEmail(keywords.get(key));
			l.append(" " + alias.getAlias() + "|" + alias.getEmail());
		}
		l.flush_dbg();
	}

	private void scrapeAge() {
		Log l = new Log("Scraping ages");
		HashMap<Integer, Integer> keywords = ScrapeKeywords.getAge(notags);
		Set<Integer> keys = keywords.keySet();
		for (Integer key : keys) {
			Entry<Integer, Alias> closest = alias_locations.floorEntry(key);
			if (closest == null) continue;
			Alias alias =  closest.getValue();
			alias.setAge(keywords.get(key));
			l.append(" " + alias.getAlias() + "|" + alias.getAge());
		}
		l.flush_dbg();
	}

	private void scrapeGender() {
		Log l = new Log("Scraping genders");
		HashMap<Integer, Alias.Gender> keywords = ScrapeKeywords.getGender(notags);
		Set<Integer> keys = keywords.keySet();
		for (Integer key : keys) {
			Entry<Integer, Alias> closest = alias_locations.floorEntry(key);
			if (closest == null) continue;
			Alias alias =  closest.getValue();
			alias.setGender(keywords.get(key));
			l.append(" " + alias.getAlias() + "|" + alias.getGender());
		}
		l.flush_dbg();
	}

	private void scrapeLocalisms() {
		Log l = new Log("Scraping localisms");
		HashMap<Integer, NationalityScore> keywords = ScrapeKeywords.getLocalisms(notags);
		Set<Integer> keys = keywords.keySet();
		for (Integer key : keys) {
			Entry<Integer, Alias> closest = alias_locations.floorEntry(key);
			if (closest == null) continue;
			Alias alias =  closest.getValue();
			alias.getNationality().add(keywords.get(key));
			l.append(" " + alias.getAlias() + "|" + key);
		}
		l.flush_dbg();
	}
	
	private void scrapeLocation() {
		Log l = new Log("Scraping locations");
		HashMap<Integer, Location> locations = ScrapeKeywords.getLocation(notags);
		Set<Integer> keys = locations.keySet();
		for (Integer key : keys) {
			Entry<Integer, Alias> closest = alias_locations.floorEntry(key);
			if (closest == null) continue;
			Alias alias =  closest.getValue();
			long pop = alias.getLocation().getPopulation();
			if (pop > locations.get(key).getPopulation()) continue;
			alias.setLocation(locations.get(key));
			l.append(" " + alias.getAlias() + "|" + alias.getLocation());
		}
		l.flush_dbg();
	}

	private void extractLinks() {
		Log l = new Log("Parsing links");
		Pattern link = Pattern.compile("<\\s*a.*?href\\s*=\\s*['|\"]([^'\"]+)['|\"][^>]*>([^<]+)<", Pattern.CASE_INSENSITIVE);
		Matcher link_match;
		try {
			link_match = link.matcher(page.getFile());
		} catch (IOException e) {
			l.append("Could not scrape links.");
			return;
		}
		while (link_match.find()) {
			String href = link_match.group(1).trim();
			String content = link_match.group(2).trim().replaceAll("\\<.*?>", "");
			HtmlLink h = new HtmlLink(page, href, content);
			links.add(h);
		}
		
		l.flush_inf();
	}
	
	private void extractAliases() {
		// Search for aliases which are written as "by xxxx" without a link //
		// too inaccurate //
		/*Pattern by_alias = Pattern.compile("[^a-z0-9_-]by[\\s]+([a-z0-9_-]*[a-z][a-z0-9_-]*)", Pattern.CASE_INSENSITIVE);
		Matcher alias_match = by_alias.matcher(file);
		while (alias_match.find()) {
			crawl.add(new Alias(alias_match.group(1)));
			Log.debug("[1] Alias found: " + alias_match.group(1));
		}*/
		
		// Search for aliases by looking for promising links containing buzz words //
		Iterator<HtmlLink> it = links.iterator();
		Pattern contents_pattern = Pattern.compile("^([a-z0-9_-]*[a-z][a-z0-9_-]*)$", Pattern.CASE_INSENSITIVE);
		Pattern href_pattern = Pattern.compile("[member|user|profile|/u[0-9]{3,7}|[member|profile].*[?].*u=[0-9]{3,7}]", Pattern.CASE_INSENSITIVE);
		while (it.hasNext()) {
		HtmlLink h = it.next();
			Matcher match1 = href_pattern.matcher(h.getHref());
			Matcher match2 = contents_pattern.matcher(h.getContent());
			if (match1.find() && match2.find()) {
				aliases.add(new Alias(match2.group()));
				Log.debug("[2] Alias found: " + match2.group()); 
			}
		}
		
		// Look for an explicit statement "username" closely followed by a possible alias //
		Pattern explicit_alias = Pattern.compile("\\Wusername\\W", Pattern.CASE_INSENSITIVE);
		Matcher explicit_match = explicit_alias.matcher(notags);
		Pattern extract_alias = Pattern.compile("([a-z0-9_-]+[a-z][a-z0-9_-]+)", Pattern.CASE_INSENSITIVE);
		while (explicit_match.find()) {
			String small_string = notags.substring(explicit_match.end(), explicit_match.end()+64).trim();
			
			Matcher match = extract_alias.matcher(small_string);
			if (match.find()) {
				aliases.add(new Alias(match.group(1)));
				Log.debug("[3] Alias found: " + match.group(1));
			}
		}
		
		// Search for aliases by looking for common alias patterns //
		// Pattern eg. alice92 //
		Pattern alias_pattern = Pattern.compile("\\|([a-z0-9_-]{4,}[0-9]{1,4})\\|", Pattern.CASE_INSENSITIVE);
		Matcher match = alias_pattern.matcher(notags);
		while (match.find()) {
			aliases.add(new Alias(match.group(1)));
			Log.debug("[4] Alias found: " + match.group(1));
		}
		
		// Pattern eg. writing_writer //
		Pattern alias_pattern2 = Pattern.compile("\\|([!a-z0-9_-]{3,}_[!a-z0-9_-]{3,})\\|", Pattern.CASE_INSENSITIVE);
		Matcher match2 = alias_pattern2.matcher(notags);
		while (match2.find()) {
			aliases.add(new Alias(match2.group(1)));
			Log.debug("[5] Alias found: " + match2.group(1));
		}
				
		
		Log l = new Log("Removing unlikely aliases");
		Iterator<Alias> it_alias = aliases.iterator();
		while (it_alias.hasNext()) {
			Alias a = it_alias.next();
			String alias = a.getAlias();
			// which|date|the|you|share|like|login|next|help|home|email|main|
			if (alias.matches("^(?i)[a-z]+ing|comment[s]?|signup|submit|cancel|permalink|previous|register|search|password|username|facebook|twitter$")) {
				l.append(" " + alias);
				it_alias.remove();
			} else if (alias.length() <= 5) {
				l.append(" " + alias);
				it_alias.remove();
			}
		}
		l.flush_dbg();
		
		Log.info(aliases.size() + " aliases found.");
	}
	
	private void locateAliases() {
		Log l = new Log("Locating aliases");
		Iterator<Alias> it = aliases.iterator();
		while (it.hasNext()) {
			Alias a = it.next();
			Matcher m = Pattern.compile("[^\\w_-](" + a.getAlias() + ")[^\\w_-]").matcher(notags);
			while (m.find()) {
				alias_locations.put(new Integer(m.start(1)), a);
				l.append(" " + a.getAlias() + "|" + m.start(1));
			}
		}
		l.flush_inf();
	}
}
