package implementations;

public class Alias {
	public static enum Gender { Male, Female, Unknown };
	
	private String alias, email;
	private NationalityScore ns;
	private Location location;
	private Gender gender;
	private Integer age;
	
	public Alias(String alias) {
		this.alias = alias;
		ns = new NationalityScore();
		location = new Location();
		gender = Gender.Unknown;
		age = 0;
		email = "";
	}
	public String getAlias() {
		return alias;
	}

	public NationalityScore getNationality() {
		return ns;
	}

	public void setLocation(Location loc) {
		location = loc;
	}

	public Location getLocation() {
		return location;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGenderString() {
		if (gender.equals(Gender.Male)) {
			return "M";
		} else if (gender.equals(Gender.Female)) {
			return "F";
		}
		
		return "?";
	}

	public void setGender(Gender g) {
		gender = g;
	}

	public Gender getGender() {
		return gender;
	}

	public int getAge() {
		return age;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
	
}
