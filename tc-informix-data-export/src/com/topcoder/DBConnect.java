/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

import java.sql.Connection;

/**
 * Interface for DB connections
 */
public interface DBConnect {

	/**
	 * Creates a new connection
	 * @return the connection
	 * @throws Exception if any error occurs
	 */
	public Connection getNewConnection() throws Exception;

	/**
	 * Create db url from configuration parameters
	 * @param dbInterface -the configuration
	 * @return jdbc url
	 */
	public String formJDBCUrl(DBInterface dbInterface);

	/**
	 * Register db driver
	 * @throws Exception
	 */
	public void registerDriver() throws Exception;

}
