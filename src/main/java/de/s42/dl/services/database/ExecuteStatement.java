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

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <ResultType>
 */
public class ExecuteStatement<ResultType> extends AbstractStatement<ResultType>
{

	private final static Logger log = LogManager.getLogger(ExecuteStatement.class.getName());
	protected Supplier<ResultType> factory;

	protected ExecuteStatement(DatabaseService databaseService) throws Exception
	{
		super();

		this.databaseService = databaseService;
	}

	protected ExecuteStatement(DatabaseService databaseService, Supplier<ResultType> factory) throws Exception
	{
		super();

		this.databaseService = databaseService;
		this.factory = factory;
	}

	public ExecuteStatement(DatabaseService databaseService, String statementResource) throws Exception
	{
		super(databaseService, statementResource);
	}

	public ExecuteStatement(DatabaseService databaseService, String statementResource, Supplier<ResultType> factory) throws Exception
	{
		super(databaseService, statementResource);

		this.factory = factory;
	}

	public ExecuteStatement(DatabaseService databaseService, String statementResource, String name) throws Exception
	{
		super(databaseService, statementResource, name);
	}

	public ExecuteStatement(DatabaseService databaseService, String statementResource, String name, Supplier<ResultType> factory) throws Exception
	{
		super(databaseService, statementResource, name);

		this.factory = factory;
	}

	public List<ResultType> executeMany(Object... parameters) throws Exception
	{
		assert factory != null;

		return executeQueryManyEntities(factory, parameters);
	}

	public Optional<ResultType> executeOneOrNone(Object... parameters) throws Exception
	{
		assert factory != null;

		return executeQuerySingleOrNoEntity(factory, parameters);
	}

	public ResultType executeOne(Object... parameters) throws Exception
	{
		assert factory != null;

		return executeQuerySingleEntity(factory, parameters);
	}

	public void execute(Object... parameters) throws Exception
	{
		executeNoResult(parameters);
	}
}
