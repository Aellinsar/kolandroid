package com.starfish.kol.model.models.chat;

import java.util.ArrayList;

import com.starfish.kol.connection.Session;
import com.starfish.kol.model.LiveMessage;
import com.starfish.kol.model.Model;
import com.starfish.kol.model.elements.interfaces.DeferredAction;

public class ChannelModel extends Model<LiveMessage> {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -7441483376336509694L;

	private final ArrayList<ChatText> messages;
	private final String name;
	private boolean changed;
	private boolean active;
	
	public ChannelModel(String name, Session session) {
		super(session);
		
		this.changed = false;
		this.name = name;
		this.active = name.contains("@");
		
		this.messages = new ArrayList<ChatText>();
	}

	public DeferredAction<ChatModel> enter() {
		return new DeferredAction<ChatModel>() {
			/**
			 * Autogenerated by eclipse.
			 */
			private static final long serialVersionUID = 2674994050453021306L;

			@Override
			public void submit(ChatModel context) {
				ChannelModel me = context.getChannel(name);
				if(me.name.contains("@")) return;
				if(me.active) return;
				context.submitChat("/listen " + me.name);
			}			
		};
	}
	
	public DeferredAction<ChatModel> leave() {
		return new DeferredAction<ChatModel>() {
			/**
			 * Autogenerated by eclipse.
			 */
			private static final long serialVersionUID = 372556037834320419L;

			@Override
			public void submit(ChatModel context) {
				ChannelModel me = context.getChannel(name);
				if(me.name.contains("@")) {
					me.setActive(false);
					context.notifyChange();
					return;
				}
				if(!me.active) return;
				context.submitChat("/listen " + me.name);
			}			
		};
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	protected void setActive(boolean active) {
		System.out.println("Setting channel " + this.name + " active to " + active);
		this.active = active;
	}
	
	protected void addMessage(ChatText message) {
		messages.add(message);
		changed = true;
		
		if(this.name.startsWith("@"))
			setActive(true);
		
		if(message.isEvent()) {
			String text = message.getText();
			if(text.contains("Now listening to channel:"))
				setActive(true);
			if(text.contains("No longer listening to channel:"))
				setActive(false);
			if(text.contains("You are now talking in channel:"))
				setActive(true);
		}
	}
	
	protected void triggerUpdate() {
		if(changed)
			this.notifyView(LiveMessage.REFRESH);
	}
	
	public ArrayList<ChatText> getMessages() {
		return messages;
	}
}