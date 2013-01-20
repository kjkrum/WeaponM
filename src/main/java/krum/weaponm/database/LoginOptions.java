package krum.weaponm.database;

import java.io.Serializable;

/**
 * Server login settings.
 */
public class LoginOptions implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private volatile String host = "";
	private volatile int port = 23;
	private volatile char game = 'A';
	private volatile String name = "";
	private volatile String password = "";
	private volatile boolean autoLogin = false;
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public char getGame() {
		return game;
	}
	
	public void setGame(char game) {
		this.game = game;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public boolean isAutoLogin() {
		return autoLogin;
	}
	
	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
	}
	
}
