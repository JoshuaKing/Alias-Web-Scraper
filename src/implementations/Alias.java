package implementations;

import interfaces.IAlias;
import interfaces.ILocation;
import interfaces.INationalityScore;

public class Alias implements IAlias {
	private String alias, email;
	private NationalityScore ns;
	private ILocation location;
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
	
	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public INationalityScore getNationality() {
		return ns;
	}

	@Override
	public void setLocation(ILocation loc) {
		location = loc;
	}

	@Override
	public ILocation getLocation() {
		return location;
	}

	@Override
	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String getGenderString() {
		if (gender.equals(Gender.Male)) {
			return "M";
		} else if (gender.equals(Gender.Female)) {
			return "F";
		}
		
		return "?";
	}

	@Override
	public void setGender(Gender g) {
		gender = g;
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getEmail() {
		return email;
	}
	
	
}
