/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * HAndles Informix connections
 */
public class InformixDBConnect implements DBConnect {

	/**
	 * Driver name
	 */
	static String DRIVER_CLASS = "com.informix.jdbc.IfxDriver";

	/**
	 * JDBC url
	 */
	static String JDBC_URL;

	/**
	 * Default constructor
	 * @param dbInterface the configuration
	 * @throws Exception if any error occurs
	 */
	public InformixDBConnect(DBInterface dbInterface) throws Exception {
		JDBC_URL = formJDBCUrl(dbInterface);
		registerDriver();
	}

	/**
	 * Returns new connection
	 * @return the connection
	 * @throws Exception if any error occurs
	 */
	@Override
	public Connection getNewConnection() throws Exception {
		Connection conn = DriverManager.getConnection(JDBC_URL);
		return conn;
	}

	/**
	 * Creates JDBC url
	 * @param dbInterface -the configuration
	 * @return jdbc url
	 */
	@Override
	public String formJDBCUrl(DBInterface dbInterface) {
		return String.format("jdbc:informix-sqli://%s:%d/%s:INFORMIXSERVER=%s;user=%s;password=%s", dbInterface.getEndPoint(),dbInterface.getPort(),dbInterface.getName(),dbInterface.getServer(),dbInterface.getUsername(),dbInterface.getPassword());
	}

	/**
	 * Registers driver
	 * @throws Exception if any error occurs
	 */
	@Override
	public void registerDriver() throws Exception {
		try {
			Class.forName(DRIVER_CLASS);
		} catch (Exception ex) {
			throw new Exception("ERROR: failed to load Informix JDBC driver.",ex);
		}
		try {
			DriverManager.registerDriver((com.informix.jdbc.IfxDriver) Class.forName(DRIVER_CLASS).newInstance());
		} catch (Exception ex) {
			throw new Exception("Driver is not Registered",ex);
		}		
	}
}
