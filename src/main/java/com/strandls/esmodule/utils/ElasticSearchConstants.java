package com.strandls.esmodule.utils;

public enum ElasticSearchConstants {
	
	/*
	 * Index name and their default names
	 */
	extended_taxon_definition("etdi"),
	extended_records("er");
	
	
	private String value;
	
	public String getValue() {
		return this.value;
	}
	
	 private ElasticSearchConstants(String value) 
	    { 
	        this.value = value; 
	    } 
	
}
