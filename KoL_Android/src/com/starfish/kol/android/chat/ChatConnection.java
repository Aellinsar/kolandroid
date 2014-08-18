package com.starfish.kol.android.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.starfish.kol.android.chat.newchat.ChatActivity;
import com.starfish.kol.android.util.AndroidProgressHandler;
import com.starfish.kol.connection.Session;
import com.starfish.kol.gamehandler.ViewContext;
import com.starfish.kol.model.elements.interfaces.DeferredAction;
import com.starfish.kol.model.models.chat.ChatModel;
import com.starfish.kol.model.models.chat.ChatState;

public abstract class ChatConnection {
	private final ServiceConnection service;
	private final Session session;
	
	private ChatManager base;
	private AndroidProgressHandler<ChatState> callback;

	public ChatConnection(Session session) {
		this.session = session;

		this.service = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				Log.i("ChatService", "Chat Service Bound");
				base = (ChatManager) service;

				base.addListener(callback);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// do nothing
			}
		};
	}

	public void stop(Activity context) {
		callback.close();
		try {
			context.getApplicationContext().unbindService(service);
			Log.i("ChatService", "Chat Service Unbound from " + context.getLocalClassName());
		} catch (Exception e) {
			Log.i("ChatService", "Unable to unbind chat service: " + e);
			// do nothing
		}
	}

	public void start(Activity context) {
		if(callback == null || callback.isClosed()) {
			callback = new AndroidProgressHandler<ChatState>() {
				@Override
				public void recieveProgress(ChatState messages) {
					updateMessages(messages);
				}
			};

			Log.i("ChatService", "Chat Service binding to " + context.getLocalClassName() + "...");
			Intent intent = new Intent(context, ChatService.class);
			intent.putExtra("session", session);
			context.getApplicationContext().bindService(intent, service,
					Context.BIND_AUTO_CREATE);
		}
	}

	public boolean execute(DeferredAction<ChatModel> action) {
		if (base != null) {
			base.execute(action);
			return true;
		}
		return false;
	}

	public void openChat(final Context appContext, final ViewContext context) {
		if (base != null) {
			DeferredAction<ChatModel> doOpen = new DeferredAction<ChatModel>() {
				/**
				 * Autogenerated by eclipse.
				 */
				private static final long serialVersionUID = 7202422856789288134L;

				@Override
				public void submit(ChatModel model) {
					if (model.getChatExists()) {
						Intent intent = new Intent(appContext,
								ChatActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("session", session);
						appContext.startActivity(intent);
					} else {
						model.displayRejectionMessage(context);
					}
				}

			};

			base.start(doOpen); // ensure chat has started

		}
	}

	public abstract void updateMessages(ChatState status);
}