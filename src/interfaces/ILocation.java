package interfaces;

public interface ILocation {
	public boolean setCountry(int countryid, long population);
	public boolean setRegion(int regionid, int countryid, long population);
	public boolean setCity(int cityid, int regionid, int countryid, long population);
	public long getPopulation();
	public boolean merge(ILocation location);
	public Integer getCity();
	public Integer getRegion();
	public Integer getCountry();
	public boolean isEmpty();
	//public boolean moreSpecific(ILocation location);
}
