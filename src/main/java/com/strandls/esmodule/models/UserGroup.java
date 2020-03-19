/**
 * 
 */
package com.strandls.esmodule.models;

/**
 * @author Abhishek Rudra
 *
 */
public class UserGroup {

	private Long id;

	/**
	 * 
	 */
	public UserGroup() {
		super();
	}

	private String name;

	/**
	 * @param id
	 * @param name
	 */
	public UserGroup(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
