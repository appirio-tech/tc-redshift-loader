/*
 * Copyright (C) 2016 TopCoder Inc., All Rights Reserved.
 */
package com.topcoder;

import com.informix.jdbcx.IfxConnectionPoolDataSource;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * HAndles Informix connections
 */
public class InformixDBConnect implements DBConnect, AutoCloseable {
	private PooledConnection pooledConnection;

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
		Connection connection = pooledConnection.getConnection();
		connection.setReadOnly(true);

		return connection;
	}


	@Override
	public void close() throws Exception {
		System.out.println("Closing connection");
		if (pooledConnection != null) {
			pooledConnection.close();
		}
	}
}
