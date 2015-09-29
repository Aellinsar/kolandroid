package com.github.kolandroid.kol.android.util.searchlist;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.Binder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.model.elements.ActionElement;

import java.util.ArrayList;

public class SearchListController<E> implements Controller {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -327422277247085374L;

    private final ArrayList<E> initial;
    private final Binder<? super E> binder;
    private final ListSelector<? super E> selector;
    private transient HighlightableListAdapter<E> adapter;

    public SearchListController(ArrayList<E> initial,
                                Binder<? super E> binder, ListSelector<? super E> selector) {
        this.binder = binder;
        this.initial = initial;
        this.selector = selector;
    }

    public static <E extends ActionElement> SearchListController<E> create(ArrayList<E> items, Binder<? super E> binder) {
        return new SearchListController<>(items, binder, new ActionSelector<E>());
    }

    @Override
    public int getView() {
        return R.layout.search_list_view;
    }

    @Override
    public void attach(View view, final Screen host) {
        adapter = new HighlightableListAdapter<>(view.getContext(), initial, binder);

        ListView list = (ListView) view.findViewById(R.id.search_list_base);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long myLong) {
                @SuppressWarnings("unchecked")
                E choice = (E) myAdapter.getItemAtPosition(myItemInt);
                if (selector != null)
                    if (selector.selectItem(host, choice)) {
                        host.close();
                    }
            }
        });
        list.setAdapter(adapter);

        final EditText text = (EditText) view.findViewById(R.id.search_list_input);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                adapter.changeFilter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                //ignored
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                //ignored
            }
        });
    }

    @Override
    public void connect(View view, Screen host) {
        // do nothing
    }

    @Override
    public void disconnect(Screen host) {
        // do nothing
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayPrimary(this);
    }

}
