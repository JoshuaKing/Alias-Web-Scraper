package implementations;

import interfaces.INationalityScore;

public class NationalityScore implements INationalityScore {
	private int UK, US, AU;
	
	public NationalityScore() {
		this(0, 0, 0);
	}
	
	private NationalityScore(int uk, int us, int au) {
		UK = uk;
		US = us;
		AU = au;
	}

	@Override
	public void add(INationalityScore ns) {
		UK += ns.getUk();
		US += ns.getUs();
		AU += ns.getAu();
	}

	@Override
	public int getUk() {
		return UK;
	}

	@Override
	public int getUs() {
		return US;
	}

	@Override
	public int getAu() {
		return AU;
	}

	public static INationalityScore score(int uk, int us, int au) {
		return new NationalityScore(uk, us, au);
	}
}
