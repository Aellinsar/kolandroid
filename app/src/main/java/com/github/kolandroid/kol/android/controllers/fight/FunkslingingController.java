package com.github.kolandroid.kol.android.controllers.fight;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.ElementBinder;
import com.github.kolandroid.kol.android.controller.BinderController;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.screen.ViewScreen;
import com.github.kolandroid.kol.android.util.searchlist.ListSelector;
import com.github.kolandroid.kol.android.util.searchlist.SearchListController;
import com.github.kolandroid.kol.model.models.fight.FightItem;

import java.util.ArrayList;

public class FunkslingingController implements Controller {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 2042614583620187701L;

    private final ArrayList<FightItem> base;

    private final BinderController<FightItem> itemSlot1, itemSlot2;
    private BinderController<FightItem> selected;
    private View selectedView;

    public FunkslingingController(ArrayList<FightItem> base) {
        this.base = base;

        this.itemSlot1 = new BinderController<FightItem>(
                ElementBinder.ONLY, FightItem.NONE);
        this.itemSlot2 = new BinderController<FightItem>(
                ElementBinder.ONLY, FightItem.NONE);
    }

    @Override
    public int getView() {
        return R.layout.dialog_funkslinging;
    }

    private void swapSelected(View view1, View view2) {
        if (selected == itemSlot1)
            setSelected(itemSlot2, view2);
        else
            setSelected(itemSlot1, view1);
    }

    private void setSelected(BinderController<FightItem> controller,
                             View view) {
        if (selectedView != null) {
            selectedView.setPressed(false);
        }

        this.selected = controller;
        this.selectedView = view;
        view.setPressed(true);
    }

    @Override
    public void connect(View view, final Screen host) {
        final ViewScreen itemScreen1 = (ViewScreen) view
                .findViewById(R.id.funksling_item1);
        itemScreen1.display(itemSlot1, host);
        itemScreen1.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1) {
                setSelected(itemSlot1, view);
                return true;
            }
        });

        final ViewScreen itemScreen2 = (ViewScreen) view
                .findViewById(R.id.funksling_item2);
        itemScreen2.display(itemSlot2, host);
        itemScreen2.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1) {
                setSelected(itemSlot2, view);
                return true;
            }
        });

        this.setSelected(itemSlot1, itemScreen1);

        ListSelector<FightItem> selector = new ListSelector<FightItem>() {
            @Override
            public boolean selectItem(Screen host, FightItem item) {
                selected.updateModel(item);
                swapSelected(itemScreen1, itemScreen2);
                return false;
            }

        };
        SearchListController<FightItem> list = new SearchListController<FightItem>(
                base, ElementBinder.ONLY, selector);
        ViewScreen listscreen = (ViewScreen) view
                .findViewById(R.id.funkslinging_list);
        listscreen.display(list, host);

        Button submit = (Button) view.findViewById(R.id.funksling_submit);
        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                FightItem item1 = itemSlot1.getValue();
                FightItem item2 = itemSlot2.getValue();

                boolean submitted = item1.useWith(host.getViewContext(), item2);
                if (submitted)
                    host.close();
            }
        });
    }

    @Override
    public void disconnect(Screen host) {
        // do nothing
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayDialog(this);
    }

}
