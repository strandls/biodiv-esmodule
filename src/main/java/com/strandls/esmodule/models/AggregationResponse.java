package com.strandls.esmodule.models;

import java.util.HashMap;

public class AggregationResponse {

	private HashMap<Object, Long> groupAggregation;

	public AggregationResponse() {
		
	}
	public AggregationResponse(HashMap<Object, Long> groupAggregation) {
		super();
		this.groupAggregation = groupAggregation;
	}

	public HashMap<Object, Long> getGroupAggregation() {
		return groupAggregation;
	}

	public void setGroupAggregation(HashMap<Object, Long> groupAggregation) {
		this.groupAggregation = groupAggregation;
	}
	

}
