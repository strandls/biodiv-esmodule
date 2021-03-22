package com.strandls.esmodule.models;

public class CommonNames {
	private String common_names;
	private Long language_id;
	private String language_name;

	public CommonNames() {
		super();
	}

	public CommonNames(String common_names, Long language_id, String language_name) {
		super();
		this.common_names = common_names;
		this.language_id = language_id;
		this.language_name = language_name;
	}

	public String getCommon_names() {
		return common_names;
	}

	public void setCommon_names(String common_names) {
		this.common_names = common_names;
	}

	public Long getLanguage_id() {
		return language_id;
	}

	public void setLanguage_id(Long language_id) {
		this.language_id = language_id;
	}

	public String getLanguage_name() {
		return language_name;
	}

	public void setLanguage_name(String language_name) {
		this.language_name = language_name;
	}

}
