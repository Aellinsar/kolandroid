package com.github.kolandroid.kol.android.controllers.inventory;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.ColoredGroupBinder;
import com.github.kolandroid.kol.android.binders.ElementBinder;
import com.github.kolandroid.kol.android.binders.SubtextBinder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.controller.LinkedModelController;
import com.github.kolandroid.kol.android.screen.DialogScreen;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.HandlerCallback;
import com.github.kolandroid.kol.android.util.searchlist.GroupSearchListController;
import com.github.kolandroid.kol.android.util.searchlist.ListSelector;
import com.github.kolandroid.kol.model.LiveModel.LiveMessage;
import com.github.kolandroid.kol.model.models.inventory.EquipmentPocketModel;
import com.github.kolandroid.kol.model.models.inventory.ItemModel;

public class EquipmentPocketController extends LinkedModelController<LiveMessage, EquipmentPocketModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 5593217234616379830L;
    private final int groupColor;
    private transient GroupSearchListController<ItemModel> list;
    private transient HandlerCallback<ItemModel> displayModel;

    public EquipmentPocketController(EquipmentPocketModel model, int groupColor) {
        super(model);
        this.groupColor = groupColor;
    }

    @Override
    public int getView() {
        return R.layout.equipment_pocket_view;
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        // do nothing
    }

    @Override
    public void receiveProgress(View view, EquipmentPocketModel model, LiveMessage message, Screen host) {
        if (list != null)
            list.setItems(model.getItems());
    }

    @Override
    public void connect(View view, EquipmentPocketModel model, final Screen host) {
        displayModel = new HandlerCallback<ItemModel>() {
            @Override
            protected void receiveProgress(ItemModel message) {
                ItemController controller = new ItemController(message);
                host.getViewContext().getPrimaryRoute().execute(controller);
            }
        };
    }

    @Override
    public void disconnect(Screen host) {
        super.disconnect(host);
        displayModel.close();
    }

    @Override
    public void attach(View view, final EquipmentPocketModel model, final Screen host) {
        ViewScreen screen = (ViewScreen) view.findViewById(R.id.inventory_list);
        ListSelector<ItemModel> displayPossibleActions = new ListSelector<ItemModel>() {
            /**
             * Autogenerated by eclipse.
             */
            private static final long serialVersionUID = 6502795666816716450L;

            @Override
            public boolean selectItem(Screen host, ItemModel item) {
                ItemController controller = new ItemController(item);
                host.getViewContext().getPrimaryRoute().execute(controller);
                return false;
            }
        };
        list = new GroupSearchListController<>(model.getItems(), "ItemPocketController:CollapsedGroups:" + model.getTitle(), new ColoredGroupBinder(groupColor), SubtextBinder.ONLY, new ListSelector<ItemModel>() {
            @Override
            public boolean selectItem(Screen host, ItemModel item) {
                item.attachView(host.getViewContext());
                if (displayModel != null) {
                    item.loadDescription(displayModel.weak());
                }
                return true;
            }
        });
        screen.display(list, host);

        Button equipOutfit = (Button) view.findViewById(R.id.equipment_equip_outfit);
        equipOutfit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Controller controller = GroupSearchListController.create(model.getOutfits(), new ColoredGroupBinder(groupColor), ElementBinder.ONLY);
                DialogScreen.display(controller, host, "Equip outfit:");
            }
        });

        Button saveOutfit = (Button) view.findViewById(R.id.equipment_save_outfit);
        saveOutfit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller controller = new CustomOutfitController(model.saveOutfit());
                DialogScreen.display(controller, host, "Save outfit as:");
            }
        });
    }
}
