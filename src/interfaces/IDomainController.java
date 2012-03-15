package interfaces;

/**
 * Allows control of accessing the next page from the database in its domain.
 * @author Josh
 *
 */
public interface IDomainController {
	public static final String USER_AGENT = "User DCrawler";
	public boolean robotsAllowed(String path);
	public String getDomain();
}
