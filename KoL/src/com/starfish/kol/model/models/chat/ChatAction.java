package com.starfish.kol.model.models.chat;

import java.io.Serializable;

import com.starfish.kol.model.ProgressHandler;
import com.starfish.kol.model.interfaces.DeferredAction;
import com.starfish.kol.request.Request;

public class ChatAction implements Serializable {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -8557324235833386443L;
	
	public static final ChatAction SHOWPROFILE = new ChatAction("Show Profile", "showplayer.php", "who", 1);
	
	private String entry; // key of action
	
	private String title;
	private int action;
	private boolean useid;
	private boolean submit;
	
	private String arg;
	
	private ChatAction(String title, String entry, String arg, int action) {
		this.title = title;
		this.entry = entry;
		this.arg = arg;
		this.action = action;
	}
	
	protected void setEntry(String key) {
		this.entry = key;
	}
	
	public ChatActionSubmission getPartialSubmission(ChatText message, ProgressHandler<String> toChatField) {
		return new ChatActionSubmission(message, toChatField);
	}
	
	@Override
	public String toString() {
		if(title == null) {
			if(entry == null) return "[null]";
			return entry;
		}
		return title;
	}
	
	public class ChatActionSubmission implements DeferredAction<ChatModel> {
		/**
		 * Autogenerated by eclipse.
		 */
		private static final long serialVersionUID = -4024598091182967536L;
		private final ChatText baseMessage;
		private final ProgressHandler<String> toChatField;
		
		public ChatActionSubmission(ChatText message, ProgressHandler<String> toChatField) {
			this.baseMessage = message;
			this.toChatField = toChatField;
		}
		
		@Override
		public void submit(ChatModel context) {
			if(baseMessage.getUser() == null)
				return; //cannot submit with no user
			
			String player = "";
						
			if (useid) {
				player = baseMessage.getUser().getId() + "";
			} else {
				player = baseMessage.getUser().getName();
			}

			switch (action) {
			case 1:
				Request webReq = new Request(entry + "?" + arg + "=" + baseMessage.getUser().getId(), context.getGameHandler());
				context.makeRequest(webReq);
				break;
			case 2:
				if (submit) {
					// submit in chat
					context.submitChat(entry + " " + player);
					break;
				} else {
					// fallthrough to filling up textview
				}
			case 3: // prompt for text
			case 4: // confirm action
				// in either case, we'll default to filling up the textview
				toChatField.reportProgress(entry + " " + player);
				break;
			case 5:
				Request chatReq = new Request(entry + baseMessage.getUser().getId(), context.getGameHandler());
				context.makeRequest(chatReq);
			}
		}
	}
}