/**
 * 
 */
package com.strandls.esmodule.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaxVotedReco {

	private Long id;
	private String scientific_name;
	private Long rank;
	private String ranktext;
	private String taxonstatus;

	/**
	 * 
	 */
	public MaxVotedReco() {
		super();
	}

	/**
	 * @param id
	 * @param scientific_name
	 * @param rank
	 * @param ranktext
	 * @param taxonstatus
	 */
	public MaxVotedReco(Long id, String scientific_name, Long rank, String ranktext, String taxonstatus) {
		super();
		this.id = id;
		this.scientific_name = scientific_name;
		this.rank = rank;
		this.ranktext = ranktext;
		this.taxonstatus = taxonstatus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getScientific_name() {
		return scientific_name;
	}

	public void setScientific_name(String scientific_name) {
		this.scientific_name = scientific_name;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public String getRanktext() {
		return ranktext;
	}

	public void setRanktext(String ranktext) {
		this.ranktext = ranktext;
	}

	public String getTaxonstatus() {
		return taxonstatus;
	}

	public void setTaxonstatus(String taxonstatus) {
		this.taxonstatus = taxonstatus;
	}

}
