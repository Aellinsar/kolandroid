package com.starfish.kol.model;

import com.starfish.kol.connection.Connection.ServerReply;
import com.starfish.kol.connection.Session;

public abstract class ParentModel<Callback> extends Model<Callback> {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = -5900739439863981210L;
	
	public ParentModel(Session s, ServerReply reply) {
		super(s, reply);
	}
	
	protected abstract Model<?>[] getChildren();
}
