package com.github.kolandroid.kol.model;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.gamehandler.DataContext;
import com.github.kolandroid.kol.gamehandler.EmptyViewContext;
import com.github.kolandroid.kol.gamehandler.LoadingContext;
import com.github.kolandroid.kol.gamehandler.SettingsContext;
import com.github.kolandroid.kol.gamehandler.ViewContext;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.request.ResponseHandler;

import java.io.Serializable;

/**
 * This class interfaces between kol pages and the corresponding views. It is
 * responsible for extracting relevant information from the server reply into
 * data objects. It also generally provides several actions which the view can
 * trigger, which update the model or trigger new requests.
 */
public abstract class Model implements Serializable {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -7378509328924319346L;

    // The current user session information.
    private final Session session;
    // The current context this model is displayed in.
    private transient ViewContext context = EmptyViewContext.ONLY;

    /**
     * Create a new model in the provided session.
     *
     * @param s Session to use in all future requests by this model.
     */
    public Model(Session s) {
        this.session = s;
    }

    /**
     * Connect a newly generated view to this model by including new context.
     *
     * @param context The context of the connecting view.
     */
    public void attachView(ViewContext context) {
        this.context = context;
    }

    /**
     * Make a new request in the context of this model.
     *
     * @param req The request to make.
     */
    protected void makeRequest(Request req) {
        this.makeRequest(req, session, getGameHandler());
    }

    /**
     * Make a new request in the context of this model.
     *
     * @param req The request to make.
     * @param override The session to use.
     */
    protected void makeRequest(Request req, Session override) {
        this.makeRequest(req, override, getGameHandler());
    }

    /**
     * Make a new request in the context of this model.
     *
     * @param req      The request to make.
     * @param listener Response handler to use for the result.
     */
    protected void makeRequest(Request req, ResponseHandler listener) {
        this.makeRequest(req, session, listener);
    }


    /**
     * Make a new request in the context of this model.
     *
     * @param req      The request to make.
     * @param override The session to use.
     * @param listener Response handler to use for the result.
     */
    protected void makeRequest(Request req, Session override, ResponseHandler listener) {
        req.makeAsync(override, context.createLoadingContext(), listener);
    }

    /**
     * Make a new blocking request in the context of this model.
     *
     * @param req The request to make.
     * @return A ServerReply, the result of the request
     */
    protected ServerReply makeBlockingRequest(Request req) {
        return req.makeBlocking(session, context.createLoadingContext());
    }

    /**
     * Make a new request in the context of this model, without immediately
     * informing the view of the request.
     *
     * @param req The request to make.
     */
    protected void makeRequestBackground(Request req, ResponseHandler listener) {
        req.makeAsync(session, LoadingContext.NONE, listener);
    }

    /**
     * Get the default handler for any requests which cannot be directly handled
     * by the new model.
     *
     * @return A handler
     */
    protected ResponseHandler getGameHandler() {
        if (context == null) context = EmptyViewContext.ONLY;
        return context.getPrimaryRoute();
    }

    /**
     * Get the session for the model.
     *
     * @return The session this model uses to make requests.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get a context which allows the model to get and set app settings.
     *
     * @return A context for changing app global settings.
     */
    protected SettingsContext getSettings() {
        if (context == null) context = EmptyViewContext.ONLY;
        return context.getSettingsContext();
    }

    /**
     * Get a context which allows the model to check the app data cache.
     *
     * @return A context for accessing the app data cache.
     */
    protected DataContext getData() {
        if (context == null) context = EmptyViewContext.ONLY;
        return context.getDataContext();
    }
}
