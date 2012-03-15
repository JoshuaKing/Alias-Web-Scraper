package implementations;

import interfaces.ILocation;

public class Location implements ILocation {
	private Integer city_id, region_id, country_id;
	private long pop;
	
	public Location() {
		country_id = region_id = city_id = null;
		pop = 0;
	}
	
	@Override
	public boolean setCountry(int countryid, long population) {
		if (population < pop) return false;
		country_id = countryid;
		pop = population;
		return true;
	}

	@Override
	public boolean setRegion(int regionid, int countryid, long population) {
		if (population < pop) return false;
		region_id = regionid;
		country_id = countryid;
		pop = population;
		return true;
	}

	@Override
	public boolean setCity(int cityid, int regionid, int countryid, long population) {
		if (population < pop) return false;
		city_id = cityid;
		region_id = regionid;
		country_id = countryid;
		pop = population;
		return true;
	}

	@Override
	public long getPopulation() {
		return pop;
	}

	@Override
	public boolean merge(ILocation location) {
		if (location.getCountry() == null) return true;
		if (country_id == null) country_id = location.getCountry();
		else if (!location.getCountry().equals(country_id)) return false;

		if (location.getRegion() == null) return true;
		if (region_id == null) region_id = location.getRegion();
		else if (!location.getRegion().equals(region_id)) return false;
		
		if (location.getCity() == null) return true;
		if (city_id == null) city_id = location.getCity();
		else if (!location.getCity().equals(city_id)) return false;
		
		return true;
	}

	@Override
	public Integer getCity() {
		return city_id;
	}

	@Override
	public Integer getRegion() {
		return region_id;
	}

	@Override
	public Integer getCountry() {
		return country_id;
	}

	@Override
	public boolean isEmpty() {
		if (null == country_id && null == region_id && null == city_id)
			return true;
		return false;
	}
	
	public String toString() {
		if (country_id == null) return "No Location";
		String details = "Country: " + country_id + " (Population: " + pop + ")";
		if (region_id == null) return details;
		details += " Region: " + region_id;
		if (city_id == null) return details;
		return details + " City: " + city_id;
	}
}
