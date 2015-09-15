package com.github.kolandroid.kol.android.controllers.inventory;

import android.view.View;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.SubtextBinder;
import com.github.kolandroid.kol.android.controller.LinkedModelController;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.searchlist.GroupSearchListController;
import com.github.kolandroid.kol.android.util.searchlist.SerializableSelector;
import com.github.kolandroid.kol.model.LiveModel.LiveMessage;
import com.github.kolandroid.kol.model.models.inventory.InventoryItem;
import com.github.kolandroid.kol.model.models.inventory.InventoryPocketModel;

public class InventoryPocketController extends
        LinkedModelController<LiveMessage, InventoryPocketModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 6843753318299187981L;
    private static final SerializableSelector<InventoryItem> displayPossibleActions = new SerializableSelector<InventoryItem>() {
        /**
         * Autogenerated by eclipse.
         */
        private static final long serialVersionUID = 6502795666816716450L;

        @Override
        public boolean selectItem(Screen host, InventoryItem item) {
            ItemController controller = new ItemController(item);
            DialogScreen.display(controller, host);
            return false;
        }
    };
    private transient GroupSearchListController<InventoryItem> list;

    public InventoryPocketController(InventoryPocketModel model) {
        super(model);
    }

    @Override
    public int getView() {
        return R.layout.fragment_inventory_pane;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this);
    }

    @Override
    public void receiveProgress(View view, InventoryPocketModel model, LiveMessage message, Screen host) {
        if (list != null)
            list.setItems(model.getItems());
    }

    @Override
    public void connect(View view, InventoryPocketModel model, Screen host) {
        ViewScreen screen = (ViewScreen) view.findViewById(R.id.inventory_list);
        list = new GroupSearchListController<InventoryItem>(model.getItems(), SubtextBinder.ONLY, displayPossibleActions);
        screen.display(list, host);
    }
}
