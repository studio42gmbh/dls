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
 * @param <FilterType>
 */
public class FindEntities<EntityType, FilterType> extends AbstractStatement
{

	public static final int DEFAULT_LIMIT = 1000;

	private final static Logger log = LogManager.getLogger(FindEntities.class.getName());

	protected final Supplier<EntityType> factory;

	protected final boolean limit;

	protected final DatabaseMatchType match;

	protected final String orderExpression;

	protected final boolean ascending;

	public FindEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String columnName, String name) throws Exception
	{
		this(databaseService, factory, tableName, columnName, name, DatabaseMatchType.iLike, false, null, true);
	}

	public FindEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String columnName, String name, DatabaseMatchType match, boolean limit, String orderExpression, boolean ascending) throws Exception
	{
		assert databaseService != null;
		assert factory != null;
		assert name != null;
		assert tableName != null;
		assert columnName != null;
		assert match != null;

		this.databaseService = databaseService;
		this.factory = factory;
		this.name = name;
		this.limit = limit;
		this.match = match;
		this.orderExpression = orderExpression;
		this.ascending = ascending;

		initStatement(tableName, columnName);
	}

	private void initStatement(String tableName, String columnName) throws InvalidParameter
	{

		if (hasLimit() && !hasOrderExpression()) {
			throw new InvalidParameter("If the statement has a limit it needs an order expression");
		}

		String limitClause = (hasLimit()) ? " LIMIT ? OFFSET ?" : "";
		String orderByClause = (hasOrderExpression()) ? " ORDER BY " + getOrderExpression() + ((isAscending()) ? " ASC" : " DESC") : "";

		this.statement = "SELECT * FROM " + tableName + " WHERE " + columnName + " " + getMatch().clause + " ?" + orderByClause + limitClause + ";";
	}

	public List<EntityType> execute(FilterType filter) throws Exception
	{
		return execute(filter, 0, DEFAULT_LIMIT);
	}

	public List<EntityType> execute(FilterType filter, int offset, int limitCount) throws Exception
	{
		log.debug("execute", getName());

		assertRequired("filter", filter);

		if (hasLimit()) {
			return this.executeQueryManyEntities(getFactory(), filter, limitCount, offset);
		} else {
			return this.executeQueryManyEntities(getFactory(), filter);
		}

	}

	public boolean hasLimit()
	{
		return limit;
	}

	public DatabaseMatchType getMatch()
	{
		return match;
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
