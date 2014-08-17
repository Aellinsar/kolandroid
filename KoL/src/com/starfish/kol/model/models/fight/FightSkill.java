package com.starfish.kol.model.models.fight;

import com.starfish.kol.connection.Session;
import com.starfish.kol.model.elements.ActionElement;
import com.starfish.kol.model.elements.interfaces.SubtextElement;
import com.starfish.kol.util.Regex;

public class FightSkill extends ActionElement implements SubtextElement {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 3970718234205335257L;

	private static final Regex OPTION_MP = new Regex("\\(.*?\\)", 0);
	
	private final String subtext;
		
	public FightSkill(Session session, String text, String img, String action) {
		super(session, OPTION_MP.replaceAll(text, ""), img, action);
		
		this.subtext = OPTION_MP.extractSingle(text);
	}

	@Override
	public String getSubtext() {
		if(subtext == null) return "";
		return subtext;
	}
}