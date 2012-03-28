package lib;

public class Log {
	private String log;
	private static int LEVEL = 2;
	
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
		if (LEVEL > 1) return;
		System.out.printf("DEBUG:   %s\n", s);
	}
	public static void info(String s) {
		if (LEVEL > 2) return;
		System.out.printf("INFO:    %s\n", s);
	}
	public static void warn(String s) {
		if (LEVEL > 3) return;
		System.out.printf("WARNING: %s\n", s);
	}
	public static void fatal(String s, Integer c) {
		if (LEVEL > 4) return;
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
