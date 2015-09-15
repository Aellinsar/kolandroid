package com.github.kolandroid.kol.model.models.fight;

import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.gamehandler.ViewContext;
import com.github.kolandroid.kol.model.elements.ActionElement;

public class FightItem extends ActionElement {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -2222125085936302631L;
    public static FightItem NONE = new FightItem(null, "(select an item below)", "", "") {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = 6441158699205312769L;

        @Override
        protected void submit(ViewContext context, String urloverride) {
            //do nothing
        }

        @Override
        public void submit(ViewContext context) {
            //do nothing
        }

        @Override
        public boolean useWith(ViewContext context, FightItem second) {
            if (second == NONE)
                return false;
            second.submit(context);
            return true;
        }
    };
    private final String val;

    public FightItem(Session session, String text, String img, String val) {
        super(session, text, img, "fight.php?action=useitem&whichitem=" + val);

        this.val = val;
    }

    public boolean useWith(ViewContext context, FightItem second) {
        if (second == NONE) {
            this.submit(context);
            return true;
        }

        String action = "fight.php?action=useitem";
        action += "&whichitem=" + val;
        action += "&whichitem2=" + second.val;
        this.submit(context, action);
        return true;
    }
}