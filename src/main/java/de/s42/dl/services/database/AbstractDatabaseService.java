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
package de.s42.dl.services.database;

import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.dl.services.AbstractService;
import de.s42.log.LogManager;
import de.s42.log.Logger;

/**
 *
 * @author Benjamin Schiller
 */
public abstract class AbstractDatabaseService extends AbstractService
{

	protected interface Transactioned<ReturnType>
	{

		public ReturnType perform() throws Exception;
	}

	private final static Logger log = LogManager.getLogger(AbstractDatabaseService.class.getName());

	@AttributeDL(required = true)
	protected DatabaseService databaseService;

	@AttributeDL(required = false, defaultValue = "false")
	protected boolean createDatabase = false;

	public void createDatabase() throws Exception
	{
		// Create tables etc
	}

	public void dropDatabase() throws Exception
	{
		// Drop tables etc		
	}

	@Override
	public synchronized void init() throws Exception
	{
		if (isInited()) {
			return;
		}

		assertRequired("databaseService", databaseService);

		initService();

		setInited(true);

		// Drop and create database if flag is set
		if (isCreateDatabase()) {
			dropDatabase();
			createDatabase();
		}
	}

	protected <ReturnType> ReturnType transactioned(Transactioned<ReturnType> func) throws Exception
	{
		log.debug("transactioned");

		boolean inTransaction = databaseService.isInTransaction();

		if (!inTransaction) {
			databaseService.startTransaction();
		}

		try {

			ReturnType result = func.perform();

			if (!inTransaction) {
				databaseService.commitTransaction();
			}

			return result;
		} catch (Exception ex) {
			if (!inTransaction) {
				databaseService.rollbackTransaction();
			}
			throw ex;
		}
	}

	public DatabaseService getDatabaseService()
	{
		return databaseService;
	}

	public void setDatabaseService(DatabaseService databaseService)
	{
		this.databaseService = databaseService;
	}

	public boolean isCreateDatabase()
	{
		return createDatabase;
	}

	public void setCreateDatabase(boolean createDatabase)
	{
		this.createDatabase = createDatabase;
	}
}
