/**
 * 
 */
package com.strandls.esmodule.models;

/**
 * @author Abhishek Rudra
 *
 */
public class ObservationNearBy {

	private Long observationId;
	private String name;
	private String thumbnail;
	private Double distance;

	/**
	 * 
	 */
	public ObservationNearBy() {
		super();
	}

	/**
	 * @param observationId
	 * @param name
	 * @param thumbnail
	 * @param distance
	 */
	public ObservationNearBy(Long observationId, String name, String thumbnail, Double distance) {
		super();
		this.observationId = observationId;
		this.name = name;
		this.thumbnail = thumbnail;
		this.distance = distance;
	}

	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

}
