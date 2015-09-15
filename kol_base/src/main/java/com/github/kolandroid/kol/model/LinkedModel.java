package com.github.kolandroid.kol.model;

import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.util.Callback;

public class LinkedModel<CType> extends Model {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -252356181477372889L;

    // Callback from the model back to the view.
    private transient Callback<CType> view;

    /**
     * Create a new model in the provided session.
     *
     * @param s Session to use in all future requests by this model.
     */
    public LinkedModel(Session s) {
        super(s);
    }

    /**
     * Connect a newly generated view to this model by including new callbacks.
     *
     * @param view A callback back to the connecting view.
     */
    public void attachCallback(Callback<CType> view) {
        this.view = view;
    }

    /**
     * Send the callback to any connected view. Note this does nothing if no
     * view is connected.
     *
     * @param message The message to send to the connected view.
     */
    protected void notifyView(CType message) {
        if (this.view != null)
            this.view.execute(message);
    }
}