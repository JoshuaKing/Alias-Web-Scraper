package implementations;

public class NationalityScore {
	private int UK, US, AU;
	
	public NationalityScore() {
		this(0, 0, 0);
	}
	
	private NationalityScore(int uk, int us, int au) {
		UK = uk;
		US = us;
		AU = au;
	}

	public void add(NationalityScore ns) {
		UK += ns.getUk();
		US += ns.getUs();
		AU += ns.getAu();
	}

	public int getUk() {
		return UK;
	}

	public int getUs() {
		return US;
	}

	public int getAu() {
		return AU;
	}

	public static NationalityScore score(int uk, int us, int au) {
		return new NationalityScore(uk, us, au);
	}
}
