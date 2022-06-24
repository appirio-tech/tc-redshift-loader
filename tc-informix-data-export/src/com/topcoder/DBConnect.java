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

}
