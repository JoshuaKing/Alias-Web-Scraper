package scraper;

import lib.Log;
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
		
		// TODO: Fetch alias information from related sites, 
		// eg. find Twitter accounts, youtube accounts,
		// possibly Facebook/G+ accounts from a google API search.
		// This will allow people to try to fetch information available, with
		// a given alias.
		
		//ScrapeController s1 = new ScrapeController("http://youtube.com");
		//s1.addPath("/videos?blah");
		//s1.addPath("/watch?v=pAbnP8y550o");
		ScrapeController s = new ScrapeController("http://ubuntuforums.org");
		s.addPath("/member.php?u=782844");
		//ScrapeController s2 = new ScrapeController("http://bodyspace.bodybuilding.com");
		//s2.addPath("/ban_dit");
		Log.info("Manager> Finished");
	}

}
