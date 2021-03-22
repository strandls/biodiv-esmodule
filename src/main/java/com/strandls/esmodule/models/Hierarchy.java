package com.strandls.esmodule.models;

public class Hierarchy {

	private Long rank;
	private String normalized_name;
	private Long taxon_id;

	public Hierarchy() {
		super();
	}

	public Hierarchy(Long taxon_id, String normalized_name, Long rank) {
		super();
		this.taxon_id = taxon_id;
		this.normalized_name = normalized_name;
		this.rank = rank;
	}

	public Long getTaxon_id() {
		return taxon_id;
	}

	public void setTaxon_id(Long taxon_id) {
		this.taxon_id = taxon_id;
	}

	public String getNormalized_name() {
		return normalized_name;
	}

	public void setNormalized_name(String normalized_name) {
		this.normalized_name = normalized_name;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

}
