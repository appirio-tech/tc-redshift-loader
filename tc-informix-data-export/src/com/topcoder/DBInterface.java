/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

public class DBInterface {

	/**
	 * DB name
	 */
	private final String name;

	/**
	 * db endpoint
	 */
	private final String endPoint;

	/**
	 * DB server
	 */
	private final String server;

	/**
	 * Db user
	 */
	private final String username;

	/**
	 * Db password
	 */
	private final String password;

	/**
	 * DB port
	 */
	private final int port;

	/**
	 * Getter for name
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for endPoint
	 * @return endPoint
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * Getter for username
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getter for password
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Getter for port
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Getter for server
	 * @return server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Constructor
	 * @param name the name
	 * @param endPoint the endpoint
	 * @param username the username
	 * @param password the password
	 * @param port the port
	 */
	public DBInterface(String name, String endPoint, String username, String password, int port) {
		super();
		this.name = name;
		this.endPoint = endPoint;
		this.server = null;
		this.username = username;
		this.password = password;
		this.port = port;
	}

	/**
	 * DbServer constructor
	 * @param name the name
	 * @param endPoint the endpoint
	 * @param dbServer the server
	 * @param username the username
	 * @param password the password
	 * @param port the port
	 */
	public DBInterface(String name, String endPoint, String dbServer, String username, String password, int port) {
		super();
		this.name = name;
		this.endPoint = endPoint;
		this.server = dbServer;
		this.username = username;
		this.password = password;
		this.port = port;
	}	
}
