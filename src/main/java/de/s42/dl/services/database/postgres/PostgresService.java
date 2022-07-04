// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2022 Studio 42 GmbH ( https://www.s42m.de ).
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
//</editor-fold>
package de.s42.dl.services.database.postgres;

import de.s42.dl.services.AbstractService;
import de.s42.dl.services.database.DatabaseService;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Benjamin Schiller
 */
public class PostgresService extends AbstractService implements DatabaseService
{

	private final static Logger log = LogManager.getLogger(PostgresService.class.getName());

	protected final static AtomicInteger connectionsCreated = new AtomicInteger(0);
	protected final static AtomicInteger dbCalls = new AtomicInteger(0);

	protected String user;
	protected String password;
	protected String connectionURL;
	protected boolean autoCloseConnection = false;

	protected final Map<Thread, Connection> perThreadConnections = Collections.synchronizedMap(new HashMap<>());

	protected void initConnection() throws Exception
	{
		log.info("Config:", getUser(), getConnectionURL());
	}

	@Override
	protected void initService()
	{
		try {
			log.info("initService");

			initConnection();

			// Get db connection to test if all works
			Connection connection = getConnection();
			DatabaseMetaData databaseMetaData = connection.getMetaData();

			log.info("Server:", databaseMetaData.getDatabaseProductName(), databaseMetaData.getDatabaseProductVersion());
			log.info("Driver:", databaseMetaData.getDriverName(), databaseMetaData.getDriverVersion());
			log.info("User:", getUser());
		} catch (Exception ex) {
			throw new RuntimeException("Error initing service - " + ex.getMessage(), ex);
		}
	}

	@Override
	public void exitService()
	{
		try {
			log.info("exitService");

			closeAllConnections();
		} catch (SQLException ex) {
			throw new RuntimeException("Error exiting service - " + ex.getMessage(), ex);
		}
	}

	protected Connection getNewConnection() throws SQLException
	{
		Properties connectionProps = new Properties();
		connectionProps.put("user", getUser());
		connectionProps.put("password", getPassword());
		//connectionProps.put("ssl", "true"); 

		return DriverManager.getConnection(getConnectionURL(), connectionProps);
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		Thread currentThread = Thread.currentThread();
		Connection con = perThreadConnections.get(currentThread);

		if (con != null && !con.isClosed()) {
			return con;
		} else {
			synchronized (currentThread) {

				con = perThreadConnections.get(currentThread);

				if (con == null || con.isClosed()) {

					con = getNewConnection();

					perThreadConnections.put(currentThread, con);
				}

				return con;
			}
		}
	}

	@Override
	public void closeConnection(Connection connection) throws SQLException
	{
		connection.close();
	}

	@Override
	public synchronized void closeAllConnections() throws SQLException
	{
		for (Connection con : perThreadConnections.values()) {
			con.close();
		}
		perThreadConnections.clear();
	}

	@Override
	public boolean isAutoCloseConnection()
	{
		return autoCloseConnection;
	}

	@Override
	public void setAutoCloseConnection(boolean autoCloseConnection)
	{
		this.autoCloseConnection = autoCloseConnection;
	}

	public int incrementAndGetDbCalls()
	{
		return dbCalls.incrementAndGet();
	}

	public int getDbCalls()
	{
		return dbCalls.get();
	}

	@Override
	public void startTransaction() throws SQLException
	{
		Connection con = getConnection();
		con.setAutoCommit(false);
		//con.setTransactionIsolation();
	}

	@Override
	public void commitTransaction() throws SQLException
	{
		Connection con = getConnection();
		con.commit();
		con.setAutoCommit(true);
	}

	@Override
	public void rollbackTransaction() throws SQLException
	{
		Connection con = getConnection();
		con.rollback();
		con.setAutoCommit(true);
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getConnectionURL()
	{
		return connectionURL;
	}

	public void setConnectionURL(String connectionURL)
	{
		this.connectionURL = connectionURL;
	}
}
