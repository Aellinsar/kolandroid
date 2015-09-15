package com.github.kolandroid.kol.model.models.skill;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.Model;
import com.github.kolandroid.kol.model.elements.OptionElement;
import com.github.kolandroid.kol.model.elements.OptionElement.OptionElementParser;
import com.github.kolandroid.kol.model.models.skill.SkillModelElement.RestorerItem;
import com.github.kolandroid.kol.util.Regex;

import java.util.ArrayList;

public class ItemsListModel extends Model implements SkillsSubmodel {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 2345497919395304279L;

    private static final Regex ITEMS_FORM = new Regex(
            "<form[^>]*restorerform[^>]*>.*?</form>", 0);

    private static final Regex PWD = new Regex("<input[^>]*pwd[^>]*>", 0);
    private static final Regex EXTRACT_VALUE = new Regex(
            "value=[\"']?([0-9a-fA-F]*)", 1);

    private ArrayList<SkillModelElement> items;

    public ItemsListModel(Session s, ServerReply base) {
        super(s);

        this.items = processItems(base.html);
    }

    private ArrayList<SkillModelElement> processItems(String html) {
        String all_items = ITEMS_FORM.extractSingle(html);
        final String pwd = EXTRACT_VALUE.extractSingle(PWD
                .extractSingle(all_items));

        OptionElementParser<SkillModelElement> itemsparser = new OptionElementParser<SkillModelElement>(
                "(select a skill)") {
            @Override
            public SkillModelElement make(OptionElement base) {
                return new RestorerItem(getSession(), base, pwd);
            }
        };
        return OptionElement.extractObjects(all_items, itemsparser);
    }

    public ArrayList<SkillModelElement> getItems() {
        return items;
    }

    @Override
    public String getTitle() {
        return "Items";
    }

    @Override
    public <Result> Result execute(SkillsVisitor<Result> visitor) {
        return visitor.execute(this);
    }

}
