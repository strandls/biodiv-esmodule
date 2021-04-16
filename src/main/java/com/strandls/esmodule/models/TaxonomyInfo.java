package com.strandls.esmodule.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxonomyInfo {

	private Long species_id;
	private String taxonstatus;
	private Long rank;
	private List<TaxonHierarchy> hierarchy;
	private List<CommonNames> common_names;
	private Long id;
	private String position;
	private String italicised_form;
	private Long accepted_name_id;
	private String cannonicalName;
	private String name;
	private Long group_id;
	private String group_name;

	public TaxonomyInfo() {
		super();
	}

	public TaxonomyInfo(Long species_id, String taxonstatus, Long rank, List<TaxonHierarchy> hierarchy,
			List<CommonNames> common_names, Long id, String position, String italicised_form, Long accepted_name_id,
			String cannonicalName, String name, Long group_id, String group_name) {
		super();
		this.species_id = species_id;
		this.taxonstatus = taxonstatus;
		this.rank = rank;
		this.hierarchy = hierarchy;
		this.common_names = common_names;
		this.id = id;
		this.position = position;
		this.italicised_form = italicised_form;
		this.accepted_name_id = accepted_name_id;
		this.cannonicalName = cannonicalName;
		this.name = name;
		this.group_id = group_id;
		this.group_name = group_name;
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

	public List<TaxonHierarchy> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(List<TaxonHierarchy> hierarchy) {
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

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getItalicised_form() {
		return italicised_form;
	}

	public void setItalicised_form(String italicised_form) {
		this.italicised_form = italicised_form;
	}

	public Long getAccepted_name_id() {
		return accepted_name_id;
	}

	public void setAccepted_name_id(Long accepted_name_id) {
		this.accepted_name_id = accepted_name_id;
	}

	public String getCannonicalName() {
		return cannonicalName;
	}

	public void setCannonicalName(String cannonicalName) {
		this.cannonicalName = cannonicalName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getGroup_id() {
		return group_id;
	}

	public void setGroup_id(Long group_id) {
		this.group_id = group_id;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

}