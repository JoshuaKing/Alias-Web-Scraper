package interfaces;

public interface IAlias {
	public static enum Gender { Male, Female, Unknown };
	public String getAlias();
	public INationalityScore getNationality();
	public void setLocation(ILocation loc);
	public ILocation getLocation();
	public void setGender(Gender g);
	public void setAge(int age);
	public void setEmail(String email);
	public String getGenderString();
	public Gender getGender();
	public int getAge();
	public String getEmail();
}
