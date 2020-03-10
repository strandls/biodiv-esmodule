/**
 * 
 */
package com.strandls.esmodule.models;

import java.util.List;
import java.util.Map;

/**
 * @author Abhishek Rudra
 *
 */
public class FilterPanelData {

	private List<String> speciesGroup;
	private List<String> userGroup;
	private List<String> states;
	private Map<String, List<String>> traits;

	/**
	 * 
	 */
	public FilterPanelData() {
		super();
	}

	/**
	 * @param speciesGroup
	 * @param userGroup
	 * @param states
	 * @param traits
	 */
	public FilterPanelData(List<String> speciesGroup, List<String> userGroup, List<String> states,
			Map<String, List<String>> traits) {
		super();
		this.speciesGroup = speciesGroup;
		this.userGroup = userGroup;
		this.states = states;
		this.traits = traits;
	}

	public List<String> getSpeciesGroup() {
		return speciesGroup;
	}

	public void setSpeciesGroup(List<String> speciesGroup) {
		this.speciesGroup = speciesGroup;
	}

	public List<String> getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(List<String> userGroup) {
		this.userGroup = userGroup;
	}

	public List<String> getStates() {
		return states;
	}

	public void setStates(List<String> states) {
		this.states = states;
	}

	public Map<String, List<String>> getTraits() {
		return traits;
	}

	public void setTraits(Map<String, List<String>> traits) {
		this.traits = traits;
	}

}
