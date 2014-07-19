package com.starfish.kol.connection;

import java.io.Serializable;

public class Session implements Serializable {
	/**
	 * Autogenerated by eclipse.
	 */
	private static final long serialVersionUID = 7771874452940822920L;
	
	private String server = "www";
	private String cookie = null;
	

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getCookie() {
		return cookie;
	}
	
	public String getServer() {
		return server;
	}
}
