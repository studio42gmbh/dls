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

	@Override
	public synchronized void init() throws Exception
	{
		if (isInited()) {
			return;
		}

		assertRequired("databaseService", databaseService);

		initService();

		setInited(true);
	}

	@SuppressWarnings("UseSpecificCatch")
	protected <ReturnType> ReturnType transactioned(Transactioned<ReturnType> func) throws Exception
	{
		return transactioned(databaseService, func);
	}

	@SuppressWarnings("UseSpecificCatch")
	public static <ReturnType> ReturnType transactioned(DatabaseService databaseService, Transactioned<ReturnType> func) throws Exception
	{
		log.debug("transactioned");

		boolean transaction = !databaseService.isInTransaction();

		if (transaction) {
			databaseService.startTransaction();
		}

		try {

			ReturnType result = func.perform();

			if (transaction) {
				databaseService.commitTransaction();
			}

			return result;
		} catch (Throwable ex) {
			if (transaction) {
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
}
