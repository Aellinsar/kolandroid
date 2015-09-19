package com.github.kolandroid.kol.model.models.skill;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.GroupModel;

public class SkillsModel extends GroupModel<SkillsSubmodel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 295714863597L;
    private final SkillsListModel skills;
    private final ItemRestorersModel items;

    public SkillsModel(Session s, ServerReply text) {
        super(s);

        this.skills = new SkillsListModel(s, text);
        this.items = new ItemRestorersModel(s, text);
    }

    @Override
    public int getActiveChild() {
        return 0;
    }

    @Override
    public SkillsSubmodel[] getChildren() {
        return new SkillsSubmodel[]{skills, items};
    }
}
