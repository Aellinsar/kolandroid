package com.starfish.kol.model.models.inventory;

import java.util.ArrayList;

import com.starfish.kol.model.elements.basic.BasicSubtextElement;

public class InventoryItem extends BasicSubtextElement {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 8203542965489207042L;

	private final ArrayList<InventoryAction> actions;

	public InventoryItem(String name, String url,
			ArrayList<InventoryAction> actions) {
		super(name, url);
		this.actions = actions;
	}

	public InventoryItem(String name, String url, String subtext,
			ArrayList<InventoryAction> actions) {
		super(name, url, subtext);
		this.actions = actions;
	}

	public ArrayList<InventoryAction> getActions() {
		return actions;
	}
}
