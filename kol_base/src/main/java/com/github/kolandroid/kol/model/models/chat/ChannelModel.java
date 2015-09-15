package com.github.kolandroid.kol.model.models.chat;

import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.Model;

import java.util.ArrayList;

public class ChannelModel extends Model {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -7441483376336509694L;

    private final transient ChatModel host;

    private final ArrayList<ChatText> messages;
    private final String name;
    private boolean active;

    public ChannelModel(ChatModel host, String name, Session session) {
        super(session);

        this.host = host;

        this.name = name;
        this.active = name.contains("@");

        this.messages = new ArrayList<ChatText>();
    }

    public void enter() {
        if (host == null)
            return;
        if (active)
            return;
        if (name.contains("@"))
            return;
        host.submitChat("/listen " + name);
    }

    public void leave() {
        if (host == null)
            return;
        if (!active)
            return;
        if (name.contains("@")) {
            setActive(false);
            host.notifyChange();
            return;
        }
        host.submitChat("/listen " + name);
    }

    public String getName() {
        return this.name;
    }

    public boolean isActive() {
        return this.active;
    }

    protected void setActive(boolean active) {
        System.out.println("Setting channel " + this.name + " active to "
                + active);
        this.active = active;
    }

    protected void addMessage(ChatText message) {
        messages.add(message);
        if (this.name.startsWith("@"))
            setActive(true);

        if (message.isEvent()) {
            String text = message.getText();
            if (text.contains("Now listening to channel:"))
                setActive(true);
            if (text.contains("No longer listening to channel:"))
                setActive(false);
            if (text.contains("You are now talking in channel:"))
                setActive(true);
        }
    }

    public ArrayList<ChatText> getMessages() {
        return messages;
    }
}
