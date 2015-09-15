package com.github.kolandroid.kol.model.models.inventory;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;

public class InventoryModel extends ItemStorageModel {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 634354430160L;

    public InventoryModel(Session s, ServerReply text) {
        super(s, text, "inventory.php", true);
    }
}
