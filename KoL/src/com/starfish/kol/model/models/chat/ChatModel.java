package com.starfish.kol.model.models.chat;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.starfish.kol.connection.Connection.ServerReply;
import com.starfish.kol.connection.Session;
import com.starfish.kol.gamehandler.GameHandler;
import com.starfish.kol.gamehandler.ViewContext;
import com.starfish.kol.model.Model;
import com.starfish.kol.model.models.chat.ChatModel.ChatStatus;
import com.starfish.kol.request.ResponseHandler;
import com.starfish.kol.request.Request;
import com.starfish.kol.request.SimulatedRequest;
import com.starfish.kol.request.TentativeRequest;
import com.starfish.kol.util.Regex;

public class ChatModel extends Model<ChatStatus> implements ResponseHandler {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 4608102489454078461L;

	private static final Regex INITIAL_MESSAGES = new Regex(
			"handleMessage\\((\\{.*?\\})(, true)?\\);", 1);
	private static final Regex ACTIONS = new Regex(
			"var actions ?= ?(\\{.*?\\});\n", 1);

	private static final Regex PLAYER_ID = new Regex(
			"playerid ?= ?[\"']?(\\d+)[\"']?[,;]", 1);
	private static final Regex PWD = new Regex(
			"pwdhash  ?= ?[\"']?([0-9a-fA-F]+)[\"']?[,;]", 1);
	private static final Regex BASEROOM = new Regex("active: \"([^\"]*)\"", 1);

	private static final Regex CHANNEL = new Regex("<br>&nbsp;&nbsp;(.*?)(?=<br>|$)", 1);

	private boolean hasChat;
	private HashSet<Integer> seenMessages;
	private String lasttime;

	private String playerid;
	private String pwd;

	private ArrayList<ChatText> messages;

	private Map<String, ChatChannel> channelsByName;
	private ArrayList<ChatChannel> channels;

	private ArrayList<ChatAction> baseActions;

	private String visibleChannel;

	private final Gson parser;

	public ChatModel(Session s) {
		super(s);
		
		hasChat = false;
		seenMessages = new HashSet<Integer>();
		messages = new ArrayList<ChatText>();

		channels = new ArrayList<ChatChannel>();
		channelsByName = new HashMap<String, ChatChannel>();

		lasttime = "0";
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(RawActionList.class,
				new RawActionListDeserializer());
		parser = builder.setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	}

	public void reset() {
		this.seenMessages.clear();
		messages.clear();
		channels.clear();
		channelsByName.clear();

		lasttime = "0";
	}

	public void refreshChannels() {
		this.submitCommand("/channels");
		this.submitCommand("/l");
	}

	protected void notifyChange() {
		notifyView(ChatStatus.UPDATE);
	}

	@Override
	public boolean handle(Session session, Request request, ServerReply response) {
		if (!response.url.contains("newchatmessages.php")
				&& !response.url.contains("submitnewchat.php")) {
			notifyView(ChatStatus.STOPPED);
			return false;
		}

		if (response.html.length() < 5) {
			notifyView(ChatStatus.STOPPED);
			return true;
		}

		RawMessageList update = parser.fromJson(response.html,
				RawMessageList.class);

		//System.out.println(response.html);
		boolean updated = false;
		for (ChatText message : update.msgs) {
			if (this.processMessage(message))
				updated = true;
		}
		
		if (update.output != null) {
			boolean commandHandled = this.processCommand(update.output);
			if(!commandHandled || !request.hasTag("hidden")) {
				if (this.processMessage(new ChatText(update.output)))
					updated = true;
			}
		}

		if (updated)
			notifyChange();

		if(update.last != null)
			lasttime = update.last;
		return true;
	}

	protected ChatChannel getChannel(String name) {
		return channelsByName.get(name);
	}
	
	private ChatChannel getOrCreateChannel(String name) {
		ChatChannel channel;
		if (channelsByName.containsKey(name)) {
			channel = channelsByName.get(name);
		} else {
			channel = new ChatChannel(name);
			channels.add(channel);
			channelsByName.put(name, channel);
			System.out.println("Added new chat channel: " + name);
		}
		return channel;
	}

	private boolean processCommand(String output) {
		output = output.replace("</font>", "");
		
		if(output.contains("<font color=green>Available channels:")) {
			//Loading result of /channel
			for (String channel : CHANNEL.extractAllSingle(output)) {
				getOrCreateChannel(channel);
			}
			return true;
		}
		else if(output.contains("<font color=green>Currently listening to channels:")) {
			//Loading result of /listen
			ArrayList<ChatChannel> active = new ArrayList<ChatChannel>();
			for (String channel : CHANNEL.extractAllSingle(output)) {
				if (channel.contains("<b>")) {
					channel = channel.replace("<b>", "")
							.replace("</b>", "");
				}

				ChatChannel c = getOrCreateChannel(channel);
				c.setActive(true);
				active.add(c);
			}

			for (ChatChannel c : channels) {
				c.setActive(active.contains(c));
			}
			return true;
		}
		
		System.out.println("Unknown command: " + output);
		return false;
	}
	
	private boolean processMessage(ChatText message) {
		if (message.getID() != 0) {
			if (seenMessages.contains(message.getID()))
				return false;
			seenMessages.add(message.getID());
		}

		message.prepare(baseActions, visibleChannel);

		String channelName = message.getChannel();
		ChatChannel channel = getOrCreateChannel(channelName);		
		messages.add(message);
		channel.addMessage(message);
		return true;
	}

	private void processInitial(ServerReply response) {
		for (String message : INITIAL_MESSAGES.extractAllSingle(response.html)) {
			if (message.contains("<span class=\"welcome\">"))
				continue;
			processMessage(parser.fromJson(message, ChatText.class));
		}

		this.playerid = PLAYER_ID.extractSingle(response.html);
		this.pwd = PWD.extractSingle(response.html);
		this.visibleChannel = BASEROOM.extractSingle(response.html);

		String actionList = ACTIONS.extractSingle(response.html);
		RawActionList rawactions = parser.fromJson(actionList,
				RawActionList.class);
		this.baseActions = rawactions.actions;

		baseActions.add(0, ChatAction.SHOWPROFILE);
	}

	public void triggerUpdate() {
		this.makeRequest(new Request("newchatmessages.php?j=1&lasttime="
				+ lasttime, this));
	}

	public void start() {
		Request req = new TentativeRequest("mchat.php", new ResponseHandler() {
			@Override
			public boolean handle(Session session, Request request,
					ServerReply response) {
				if (response == null) {
					notifyView(ChatStatus.NOCHAT);
					hasChat = false;
					return true;
				}

				if (!response.url.contains("mchat.php"))
					return false;

				notifyView(ChatStatus.LOADED);
				hasChat = true;

				processInitial(response);
				return true;
			}
		});
		this.makeRequest(req);
		this.refreshChannels();
	}

	public void setCurrentRoom(String room) {
		this.visibleChannel = room;
	}

	public void submitChat(String msg) {
		submitChat(msg, false);
	}

	public void submitChat(String channel, String msg) {
		System.out.println("Submitting " + msg + " to " + channel);
		if (!msg.startsWith("/")) {
			if (channel.startsWith("@")) {
				// private messaging channel
				msg = String
						.format("/msg %s %s", channel.replace("@", ""), msg);
			} else {
				msg = String.format("/c %s %s", channel, msg);
			}
		}
		submitChat(msg);
	}

	private void submitCommand(String msg) {
		if (!msg.startsWith("/"))
			msg = "/" + msg;
		submitChat(msg, true);
	}

	@SuppressWarnings("deprecation")
	private void submitChat(String msg, boolean hiddencommand) {
		String baseurl = "submitnewchat.php?playerid=%s&pwd=%s&graf=%s&j=1";
		String encodedmsg;

		try {
			encodedmsg = URLEncoder.encode(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			encodedmsg = URLEncoder.encode(msg);
		}

		String url = String.format(baseurl, playerid, pwd, encodedmsg);
		System.out.println("Submitting chat for " + url);

		Request req = new Request(url, this);
		if(hiddencommand)
			req.addTag("hidden");
		this.makeRequest(req);
	}

	public static enum ChatStatus {
		LOADED, NOCHAT, STOPPED, UPDATE;
	}

	public boolean getChatExists() {
		return hasChat;
	}

	public void displayRejectionMessage(ViewContext context) {
		String html = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"https://images.kingdomofloathing.com/styles.css\"></head><body><span class=small>You may not enter the chat until you have proven yourself literate. You can do so at the <a target=mainpane href=\"town_altar.php\">Temple of Literacy</a> in the Big Mountains.</body></html>";
		ServerReply reject = new ServerReply(200, "", "", html,
				"small_chatreject.php", "");
		this.makeRequest(new SimulatedRequest(reject, new GameHandler(context)));
	}

	public ArrayList<ChatChannel> getChannels() {
		return new ArrayList<ChatChannel>(channels);
	}

	public ArrayList<ChatText> getMessages() {
		return new ArrayList<ChatText>(messages);
	}

	@Override
	protected ResponseHandler getGameHandler() {
		return super.getGameHandler();
	}
	
	public static class RawMessageList {
		public ChatText[] msgs;
		public String last;
		public String output;
	}

	public static class RawActionList {
		public ArrayList<ChatAction> actions;

		public RawActionList(ArrayList<ChatAction> actions) {
			this.actions = actions;
		}
	}

	public static class RawActionListDeserializer implements
			JsonDeserializer<RawActionList> {
		@Override
		public RawActionList deserialize(JsonElement element, Type type,
				JsonDeserializationContext context) throws JsonParseException {
			ArrayList<ChatAction> actions = new ArrayList<ChatAction>();

			JsonObject jsonObject = element.getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				// For individual City objects, we can use default
				// deserialisation:
				ChatAction action = context.deserialize(entry.getValue(),
						ChatAction.class);
				action.setEntry(entry.getKey());
				actions.add(action);
			}

			return new RawActionList(actions);
		}

	}
}
