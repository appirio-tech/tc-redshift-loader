/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

import com.informix.jdbcx.IfxConnectionPoolDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * HAndles Informix connections
 */
public class InformixDBConnect implements DBConnect, AutoCloseable {
	private static final Logger logger = LogManager.getLogger(InformixDBConnect.class);

	private final PooledConnection pooledConnection;

	/**
	 * Default constructor
	 * @param dbInterface the configuration
	 * @throws Exception if any error occurs
	 */
	public InformixDBConnect(DBInterface dbInterface) throws Exception {
		IfxConnectionPoolDataSource cpds = new IfxConnectionPoolDataSource();

		cpds.setIfxIFXHOST(dbInterface.getEndPoint());
		cpds.setPortNumber(dbInterface.getPort());
		cpds.setServerName(dbInterface.getServer());
		cpds.setDatabaseName(dbInterface.getName());
		cpds.setUser(dbInterface.getUsername());
		cpds.setPassword(dbInterface.getPassword());

		cpds.setMinPoolSize(1);
		cpds.setMaxPoolSize(10);
//		cpds.setMaxIdleTime(0);

		pooledConnection = cpds.getPooledConnection();
	}

	/**
	 * Returns new connection
	 * @return the connection
	 * @throws Exception if any error occurs
	 */
	@Override
	public Connection getNewConnection() throws Exception {
		logger.info("Generating a new connection...");
		Connection connection = pooledConnection.getConnection();
		connection.setReadOnly(true);
		connection.setAutoCommit(true);

		return connection;
	}


	@Override
	public void close() throws Exception {
		logger.info("Closing connection");
		if (pooledConnection != null) {
			pooledConnection.close();
		}
	}
}
