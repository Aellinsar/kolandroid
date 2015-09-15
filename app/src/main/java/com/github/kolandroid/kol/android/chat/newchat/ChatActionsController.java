package com.github.kolandroid.kol.android.chat.newchat;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.kolandroid.kol.android.R;
import com.github.kolandroid.kol.android.binders.TextBinder;
import com.github.kolandroid.kol.android.controller.Controller;
import com.github.kolandroid.kol.android.screen.Screen;
import com.github.kolandroid.kol.android.screen.ScreenSelection;
import com.github.kolandroid.kol.android.util.HandlerCallback;
import com.github.kolandroid.kol.android.util.adapters.ListAdapter;
import com.github.kolandroid.kol.model.models.chat.ChatAction;
import com.github.kolandroid.kol.model.models.chat.ChatText;
import com.github.kolandroid.kol.util.Callback;

import java.util.ArrayList;

public class ChatActionsController implements Controller {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 7974380804392024382L;

    private final ChatText base;

    private transient ChatConnection connection;

    public ChatActionsController(ChatText base) {
        this.base = base;
    }

    @Override
    public int getView() {
        return R.layout.dialog_chat_screen;
    }

    @Override
    public void connect(View view, final Screen host) {
        ArrayList<ChatAction> actions = base.getActions();
        ListAdapter<ChatAction> adapter = new ListAdapter<ChatAction>(host.getActivity(), actions, TextBinder.ONLY);

        final Callback<String> fillChatText = new HandlerCallback<String>() {
            @Override
            protected void receiveProgress(String message) {
                ChatActionsControllerHost activity = (ChatActionsControllerHost) host.getActivity();
                activity.fillChatText(message);
            }
        };

        ListView list = (ListView) view.findViewById(R.id.dialog_chat_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> ad, View list, int pos,
                                    long arg3) {
                ChatAction select = (ChatAction) ad.getItemAtPosition(pos);
                if (select == null)
                    return;
                if (connection == null)
                    return;
                if (connection.getModel() == null)
                    return;

                select.submit(base, fillChatText, connection.getModel());
                host.close();
            }
        });

        this.connection = ChatConnection.create(this.getClass().getSimpleName());
        connection.connect(host.getActivity());
    }

    @Override
    public void disconnect(Screen host) {
        if (connection != null)
            connection.close(host.getActivity());
    }

    @Override
    public void chooseScreen(ScreenSelection choice) {
        choice.displayDialog(this);
    }


    public interface ChatActionsControllerHost {
        void fillChatText(String text);
    }
}