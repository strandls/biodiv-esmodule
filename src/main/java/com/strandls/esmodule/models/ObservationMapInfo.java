/**
 * 
 */
package com.strandls.esmodule.models;

/**
 * @author Abhishek Rudra
 *
 */
public class ObservationMapInfo {

	private Long id;
	private Double latitude;
	private Double longitude;

	/**
	 * @param id
	 * @param latitude
	 * @param longitude
	 */
	public ObservationMapInfo(Long id, Double latitude, Double longitude) {
		super();
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

}
