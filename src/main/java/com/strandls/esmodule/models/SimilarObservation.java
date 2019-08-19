/**
 * 
 */
package com.strandls.esmodule.models;

/**
 * @author Abhishek Rudra
 *
 */
public class SimilarObservation {
	private Long observationId;
	private String reprImage;

	/**
	 * @param observationId
	 * @param reprImage
	 */
	public SimilarObservation(Long observationId, String reprImage) {
		super();
		this.observationId = observationId;
		this.reprImage = reprImage;
	}

	public Long getObservationId() {
		return observationId;
	}

	public void setObservationId(Long observationId) {
		this.observationId = observationId;
	}

	public String getReprImage() {
		return reprImage;
	}

	public void setReprImage(String reprImage) {
		this.reprImage = reprImage;
	}

}
