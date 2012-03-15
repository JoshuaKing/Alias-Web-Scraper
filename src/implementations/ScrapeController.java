package implementations;

import java.util.ArrayList;

import exceptions.RobotsNotAllowed;

import interfaces.IDomainController;
import interfaces.IPageController;
import interfaces.IScrapeController;

public class ScrapeController implements IScrapeController {
	IDomainController domain;
	ArrayList<String> paths;
	
	public ScrapeController(String domain) {
		this.domain = new DomainController(domain);
		paths = new ArrayList<String>();
	}

	@Override
	public void addPath(String path) {
		paths.add(path);
		scrape();
	}
	
	private void scrape() {
		while (paths.size() > 0 && !setupScrape());
	}
	
	private boolean setupScrape() {
		IPageController page;
		try {
			page = new PageController(domain, paths.remove(0));
		} catch (RobotsNotAllowed e) {
			return false;
		}
		PageScraper scrape = new PageScraper(page);
		return true;
	}
}
