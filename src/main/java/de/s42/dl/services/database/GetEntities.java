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

import de.s42.dl.services.remote.InvalidParameter;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <EntityType>
 */
public class GetEntities<EntityType> extends AbstractStatement
{

	public static final int DEFAULT_LIMIT = 1000;

	private final static Logger log = LogManager.getLogger(GetEntities.class.getName());

	protected final Supplier<EntityType> factory;

	protected final boolean limit;

	protected final String orderExpression;

	protected final boolean ascending;

	public GetEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String name) throws Exception
	{
		this(databaseService, factory, tableName, name, false, null, true);
	}

	public GetEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String name, boolean limit, String orderExpression, boolean ascending) throws Exception
	{
		assert databaseService != null;
		assert factory != null;
		assert name != null;
		assert tableName != null;

		this.databaseService = databaseService;
		this.factory = factory;
		this.name = name;
		this.limit = limit;
		this.orderExpression = orderExpression;
		this.ascending = ascending;

		initStatement(tableName);
	}

	private void initStatement(String tableName) throws InvalidParameter
	{

		if (hasLimit() && !hasOrderExpression()) {
			throw new InvalidParameter("If the statement has a limit it needs an order expression");
		}

		String limitClause = (hasLimit()) ? " LIMIT ? OFFSET ?" : "";
		String orderByClause = (hasOrderExpression()) ? " ORDER BY " + getOrderExpression() + ((isAscending()) ? " ASC" : " DESC") : "";

		this.statement = "SELECT * FROM " + tableName + orderByClause + limitClause + ";";
	}

	public List<EntityType> execute() throws Exception
	{
		return execute(0, DEFAULT_LIMIT);
	}

	public List<EntityType> execute(int offset, int limitCount) throws Exception
	{
		log.debug("execute", getName());

		if (hasLimit()) {
			return this.executeQueryManyEntities(getFactory(), limitCount, offset);
		} else {
			return this.executeQueryManyEntities(getFactory());
		}

	}

	public boolean hasLimit()
	{
		return limit;
	}

	public Supplier<EntityType> getFactory()
	{
		return factory;
	}

	public boolean hasOrderExpression()
	{
		return orderExpression != null;
	}

	public String getOrderExpression()
	{
		return orderExpression;
	}

	public boolean isAscending()
	{
		return ascending;
	}
}
