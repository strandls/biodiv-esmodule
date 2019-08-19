/**
 * 
 */
package com.strandls.esmodule.models;

import java.util.HashMap;
import java.util.List;

/**
 * @author Abhishek Rudra
 *
 */
public class ObservationInfo {

	HashMap<Object, Long> monthAggregation;
	List<SimilarObservation> similarObservation;
	List<ObservationMapInfo> latlon;

	/**
	 * @param monthAggregation
	 * @param similarObservation
	 * @param latlon
	 */
	public ObservationInfo(HashMap<Object, Long> monthAggregation, List<SimilarObservation> similarObservation,
			List<ObservationMapInfo> latlon) {
		this.monthAggregation = monthAggregation;
		this.similarObservation = similarObservation;
		this.latlon = latlon;
	}

	public HashMap<Object, Long> getMonthAggregation() {
		return monthAggregation;
	}

	public void setMonthAggregation(HashMap<Object, Long> monthAggregation) {
		this.monthAggregation = monthAggregation;
	}

	public List<SimilarObservation> getSimilarObservation() {
		return similarObservation;
	}

	public void setSimilarObservation(List<SimilarObservation> similarObservation) {
		this.similarObservation = similarObservation;
	}

	public List<ObservationMapInfo> getLatlon() {
		return latlon;
	}

	public void setLatlon(List<ObservationMapInfo> latlon) {
		this.latlon = latlon;
	}

}
