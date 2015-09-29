package com.github.kolandroid.kol.model.models.fight;

import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.elements.OptionElement;
import com.github.kolandroid.kol.model.models.inventory.ItemModel;
import com.github.kolandroid.kol.request.Request;

public class FightItem extends ItemModel implements FightAction {
    public static final FightItem NONE = new FightItem(new Session(), new OptionElement("(select an item below)", "", "0", true), FightActionHistory.NONE) {
        @Override
        public void use() {
            // Do nothing
        }
    };

    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -2222125085936302631L;

    private final String identifier;
    private final FightActionHistory<? super FightItem> storage;

    public FightItem(Session session, OptionElement base, FightActionHistory<? super FightItem> storage) {
        super(session, "", base);
        this.storage = storage;
        this.identifier = "item:" + this.id;
    }

    public static boolean funksling(FightItem item1, FightItem item2) {
        if (item1 == NONE && item2 == NONE) {
            return false;
        }

        if (item2 == NONE) {
            item1.use();
            return true;
        }

        if (item1 == NONE) {
            item2.use();
            return true;
        }

        item1.storage.store(item1, item1.getSettings());
        item2.storage.store(item2, item2.getSettings());
        item1.makeRequest(new Request("fight.php?action=useitem&whichitem=" + item1.id + "&whichitem2=" + item2.id));
        return true;
    }

    public void use() {
        storage.store(this, getSettings());
        this.makeRequest(new Request("fight.php?action=useitem&whichitem=" + this.id));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}