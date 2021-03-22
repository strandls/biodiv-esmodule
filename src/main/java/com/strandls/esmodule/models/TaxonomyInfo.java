package com.strandls.esmodule.models;

import java.util.List;
import java.util.Map;

public class TaxonomyInfo {

	private Long species_id;
	private String taxonstatus;
	private Long rank;
	private List<Hierarchy> hierarchy;
	private List<CommonNames> common_names;
	private Long id;

	public TaxonomyInfo() {
		super();
	}

	public TaxonomyInfo(Long species_id, String taxonstatus, Long rank, List<Hierarchy> hierarchy,
			List<CommonNames> common_names, Long id) {
		super();
		this.species_id = species_id;
		this.taxonstatus = taxonstatus;
		this.rank = rank;
		this.hierarchy = hierarchy;
		this.common_names = common_names;
		this.id = id;
	}

	public Long getSpecies_id() {
		return species_id;
	}

	public void setSpecies_id(Long species_id) {
		this.species_id = species_id;
	}

	public String getTaxonstatus() {
		return taxonstatus;
	}

	public void setTaxonstatus(String taxonstatus) {
		this.taxonstatus = taxonstatus;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public List<Hierarchy> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(List<Hierarchy> hierarchy) {
		this.hierarchy = hierarchy;
	}

	public List<CommonNames> getCommon_names() {
		return common_names;
	}

	public void setCommon_names(List<CommonNames> common_names) {
		this.common_names = common_names;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}