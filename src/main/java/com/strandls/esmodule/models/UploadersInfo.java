package com.strandls.esmodule.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadersInfo {

	private String name;
	private String pic;
	private Long authorId;

	public UploadersInfo() {
		super();
	}

	public UploadersInfo(String name, String pic, Long authorId) {
		super();
		this.name = name;
		this.pic = pic;
		this.authorId = authorId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

}
