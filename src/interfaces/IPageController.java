package interfaces;

import java.io.IOException;

/**
 * Retrieves the HTML page and stores information about it.
 * 
 * @author Josh
 *
 */
public interface IPageController {
	public static final String USER_AGENT = "User DCrawler";
	public static final int MAX_LENGTH_KB = 100 * 1024;
	public String getFile() throws IOException;
	public boolean validHeader();
	public String getDomain();
	public String getPath();
}
