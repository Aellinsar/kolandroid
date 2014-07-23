package com.starfish.kol.model.elements.basic;

import com.starfish.kol.model.elements.interfaces.SubtextItem;

public class BasicSubtextItem extends BasicElement implements SubtextItem
{
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 4028114518332065225L;
	
	private final String subtext;
	
	public BasicSubtextItem(String text) {
		this(text, "");
	}
	
	public BasicSubtextItem(String text, String img) {
		this(text, img, "");
	}

	public BasicSubtextItem(String text, String img, String subtext) {
		super(text, img);
		this.subtext = subtext;
	}
	
	@Override
	public String getSubtext() {
		return this.subtext;
	}
}