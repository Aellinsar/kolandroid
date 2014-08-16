package com.starfish.kol.model.models.createchar;

import java.io.Serializable;

public class CharacterType implements Serializable {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -6363919684381553246L;
	
	private final String avatar;
	private final String name;
	private final String desc;
	private final String style;
	
	public CharacterType(String avatar, String name, String description, String style) {
		this.avatar = avatar;
		this.name = name;
		this.desc = description;
		this.style = style;
	}
	
	public String getImage() {
		return avatar;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public String getStyle() {
		return style;
	}
}
