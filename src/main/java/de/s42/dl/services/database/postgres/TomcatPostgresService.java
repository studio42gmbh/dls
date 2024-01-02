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

import de.s42.base.strings.StringHelper;
import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Benjamin Schiller
 */
public class TomcatPostgresService extends PostgresService
{

	private final static Logger log = LogManager.getLogger(TomcatPostgresService.class.getName());

	@AttributeDL(required = true)
	protected String resourceName;

	@AttributeDL(ignore = true)
	protected DataSource dataSource;

	@Override
	protected void initConnection() throws NamingException
	{
		log.info("initConnection", getResourceName());

		InitialContext cxt = new InitialContext();
		dataSource = (DataSource) cxt.lookup("java:/comp/env/" + getResourceName());
	}

	@Override
	@AttributeDL(ignore = true)
	public Connection getNewConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	public String getResourceName()
	{
		return resourceName;
	}

	public void setResourceName(String resourceName)
	{
		this.resourceName = resourceName;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}
	
	// <editor-fold desc="Hashcode/Equals/ToString/Compare" defaultstate="collapsed">
	@Override
	public String toString()
	{
		return StringHelper.toString(this, Set.of(
			"password", 
			"openConnections",
			"connections",
			"connection",
			"dataSource",
			"newConnection"
		));
	}
	//</editor-fold>			
}
