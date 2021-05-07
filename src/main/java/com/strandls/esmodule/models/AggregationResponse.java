package com.strandls.esmodule.models;

import java.util.Map;

public class AggregationResponse {

	private Map<Object, Long> groupAggregation;

	public AggregationResponse() {

	}

	public AggregationResponse(Map<Object, Long> groupAggregation) {
		super();
		this.groupAggregation = groupAggregation;
	}

	public Map<Object, Long> getGroupAggregation() {
		return groupAggregation;
	}

	public void setGroupAggregation(Map<Object, Long> groupAggregation) {
		this.groupAggregation = groupAggregation;
	}

}
