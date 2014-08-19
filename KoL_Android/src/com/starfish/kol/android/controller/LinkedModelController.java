package com.starfish.kol.android.controller;

import android.view.View;

import com.starfish.kol.android.screen.Screen;
import com.starfish.kol.android.util.HandlerCallback;
import com.starfish.kol.model.LinkedModel;

public abstract class LinkedModelController<C, M extends LinkedModel<C>> extends ModelController<M> {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 5761904333216584549L;
	
	private transient HandlerCallback<C> callback;
	
	public LinkedModelController(M model) {
		super(model);
	}
	
	@Override
	public void connect(final View view, final Screen host) {
		if(callback != null) {
			callback.close();
		}
		
		this.callback = new HandlerCallback<C>() {
			@Override
			public void recieveProgress(C message) {
				LinkedModelController.this.recieveProgress(view, getModel(), message, host);
			}
		};
		
		getModel().attachCallback(callback);
		super.connect(view, host);
	}
	
	public abstract void recieveProgress(View view, M model, C message, Screen host);
	
	@Override
	public void disconnect(Screen host) {
		callback.close();
	}
}