package lib;

public class Log {
	private String log;
	
	public Log() {
		log = "";
	}
	
	public Log(String l) {
		log = l + "...";
	}
	
	private void reset() {
		log = "";
	}
	
	public static void debug(String s) {
		System.out.printf("DEBUG:   %s\n", s);
	}
	public static void info(String s) {
		System.out.printf("INFO:    %s\n", s);
	}
	public static void warn(String s) {
		System.out.printf("WARNING: %s\n", s);
	}
	public static void fatal(String s, Integer c) {
		System.out.printf("FATAL:   [%d] %s\n", c, s);
		System.exit(c);
	}
	
	public Log append(String s) {
		log += s;
		return this;
	}
	
	public void flush_dbg() {
		debug(log + " [DONE]");
		reset();
	}
	
	public void flush_inf() {
		info(log + " [DONE]");
		reset();
	}
	
	public void flush_wrn() {
		warn(log + " [DONE]");
		reset();
	}
	
	public void flush_fat(Integer code) {
		fatal(log + " [DONE]", code);
		reset();
	}
	
	public void flush_dbg(String err) {
		debug(log + " [" + err + "]");
		reset();
	}
	
	public void flush_inf(String err) {
		info(log + " [" + err + "]");
		reset();
	}
	
	public void flush_wrn(String err) {
		warn(log + " [" + err + "]");
		reset();
	}
	
	public void flush_fat(String err, Integer code) {
		fatal(log + " [" + err + "]", code);
		reset();
	}
}
