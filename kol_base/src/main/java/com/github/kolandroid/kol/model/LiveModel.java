package com.github.kolandroid.kol.model;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.LiveModel.LiveMessage;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.request.ResponseHandler;

/**
 * A model which can update its contents with a single base url.
 */
public abstract class LiveModel extends LinkedModel<LiveMessage> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 6439988316319232465L;

    //If true, redirect failed updates to the main game loop
    private final boolean foreground;
    //The url to consult for an update.
    private String updateUrl;
    //If true, the first request has already been made for content.
    private boolean filling;

    /**
     * Create a new LiveModel with the provided info.
     *
     * @param s          The session to use for any requests.
     * @param updateUrl  The url to consult for content.
     * @param foreground If true, redirect failed updates to the main game loop.
     */
    public LiveModel(Session s, String updateUrl, boolean foreground) {
        super(s);

        this.filling = false;
        this.updateUrl = updateUrl;
        this.foreground = foreground;
    }

    /**
     * Process a reply from the server, filling the model and notifying the view of an update.
     * @param response  The reply from the server.
     */
    public final void process(ServerReply response) {
        this.filling = true;
        this.loadContent(response);
        this.notifyView(LiveMessage.REFRESH);
    }

    /**
     * Load this model with actual content.
     * @param content   The content to parse and use to fill the model.
     */
    protected abstract void loadContent(ServerReply content);

    /**
     * Request the model to refresh its content.
     */
    public void update() {
        Request update = new Request(this.updateUrl);

        ResponseHandler listener = new ResponseHandler() {
            @Override
            public void handle(Session session, ServerReply response) {
                if (canHandle(response.url)) {
                    process(response);
                } else {
                    if (foreground)
                        getGameHandler().handle(session, response);
                    else
                        System.out.println("LiveModel expected " + updateUrl
                                + " but was redirected to " + response.url);
                }
            }
        };

        if (foreground)
            this.makeRequest(update, listener);
        else
            this.makeRequestBackground(update, listener);
    }

    /**
     * If the model is completely empty, trigger an update.
     * This allows models to load in a lazy fashion.
     */
    public void access() {
        if (this.filling)
            return;
        this.filling = true;

        this.update();
    }

    /**
     * Change the url to consult for content.
     * @param url   The new url to consult for content.
     */
    protected void setUpdateUrl(String url) {
        this.updateUrl = url;
    }

    /**
     * Determine if the provided url is a valid server response.
     * By default, the url must contain the requested url.
     *
     * @param url   The url the server responded with
     * @return True if the url should be parsed normally.
     */
    protected boolean canHandle(String url) {
        return url.contains(updateUrl);
    }

    /**
     * A simple enum used for LiveModels to notify their view when they receive new
     * content.
     */
    public enum LiveMessage {
        REFRESH
    }
}