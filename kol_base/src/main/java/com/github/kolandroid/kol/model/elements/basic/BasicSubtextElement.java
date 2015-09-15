package com.github.kolandroid.kol.model.elements.basic;

import com.github.kolandroid.kol.model.elements.interfaces.SubtextElement;

public class BasicSubtextElement extends BasicElement implements SubtextElement {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 4028114518332065225L;

    private final String subtext;

    public BasicSubtextElement(String text) {
        this(text, "");
    }

    public BasicSubtextElement(String text, String img) {
        this(text, img, "");
    }

    public BasicSubtextElement(String text, String img, String subtext) {
        super(text, img);
        this.subtext = subtext;
    }

    @Override
    public String getSubtext() {
        return this.subtext;
    }
}
