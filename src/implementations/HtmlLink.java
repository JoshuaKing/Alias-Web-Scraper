package implementations;

import interfaces.IPageController;

public class HtmlLink {
	String origin_domain, origin_path, href, content;
	
	public HtmlLink(IPageController page, String href, String content) {
		origin_domain = page.getDomain();
		origin_path = page.getPath();
		this.href = fixHref(href);
		this.content = content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHref() {
		return origin_domain + content;
	}

	public String getContent() {
		return content;
	}
	
	private String fixHref(String href) {
		// Fix up link
		if (href.startsWith("/")) {
			return origin_domain + href;
		} else if (href.startsWith(".")) {
			return origin_domain + origin_path + href;
		} else if (!href.matches("\\.[com|org|net|us|me|info|to]")) {
			String link = origin_domain + origin_path;
			int pos = link.lastIndexOf('/');
			return link.substring(0, pos) + href;
		}
		return href;
	}

}
