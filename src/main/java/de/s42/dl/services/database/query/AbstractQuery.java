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
package de.s42.dl.services.database.query;

import de.s42.dl.DLAttribute.AttributeDL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Benjamin.Schiller
 * @param <QueryType>
 */
public abstract class AbstractQuery<QueryType extends AbstractQuery> implements Query
{

	@AttributeDL(required = false, defaultValue = "-1")
	protected int limit = -1;

	@AttributeDL(required = false, defaultValue = "0")
	protected int offset = 0;

	@AttributeDL(required = false, defaultValue = "true")
	protected boolean ascending = true;

	@AttributeDL(required = false, defaultValue = "false")
	protected boolean withTotalCount = false;

	@AttributeDL(required = false)
	protected Map<String, Object> dimensionValues = new HashMap<>();

	@AttributeDL(required = false)
	protected String orderBy;
	
	protected static void exclusiveDimensions(QueryDimension dimension1, QueryDimension dimension2)
	{
		dimension1.addExclusiveDimension(dimension2.getName());
		dimension2.addExclusiveDimension(dimension1.getName());
	}

	protected static void requiredDimensions(QueryDimension dimension1, QueryDimension dimension2)
	{
		dimension1.addRequiredDimension(dimension2.getName());
		dimension2.addRequiredDimension(dimension1.getName());
	}
	
	protected AbstractQuery(String defaultOrderBy)
	{
		assert defaultOrderBy != null;

		orderBy = defaultOrderBy;
	}

	protected AbstractQuery(Set<QueryDimension> dimensions, Map<String, Object> query, String defaultOrderBy) throws InvalidDimension, ExclusiveDimensionPresent, RequiredDimensionMissing
	{
		init(dimensions, query, defaultOrderBy);
	}

	@SuppressWarnings("ConvertToStringSwitch")
	private void init(Set<QueryDimension> dimensions, Map<String, Object> query, String defaultOrderBy) throws InvalidDimension, ExclusiveDimensionPresent, RequiredDimensionMissing
	{
		assert dimensions != null;
		assert defaultOrderBy != null;
		assert query != null;

		orderBy = defaultOrderBy;

		for (Map.Entry<String, Object> entry : query.entrySet()) {

			String key = entry.getKey();

			if (key.equals("limit")) {
				limit = ((Number) entry.getValue()).intValue();
			} else if (key.equals("offset")) {
				offset = ((Number) entry.getValue()).intValue();
			} else if (key.equals("ascending")) {
				ascending = (Boolean) entry.getValue();
			} else if (key.equals("withTotalCount")) {
				withTotalCount = (Boolean) entry.getValue();
			} else if (key.equals("orderBy")) {
				orderBy = (String) entry.getValue();
			} else {
				QueryDimension foundDimension = null;
				for (QueryDimension dimension : dimensions) {
					if (dimension.getName().equals(entry.getKey())) {
						foundDimension = dimension;
						break;
					}
				}

				if (foundDimension == null) {
					throw new InvalidDimension("Dimension " + entry.getKey() + " is not contained");
				}

				and(foundDimension, entry.getValue());
			}
		}
	}

	public QueryType orderBy(String orderBy)
	{
		assert orderBy != null;

		this.orderBy = orderBy;

		return (QueryType) this;
	}

	public <QueryType extends AbstractQuery> QueryType limit(int limit)
	{
		this.limit = limit;

		return (QueryType) this;
	}

	public <QueryType extends AbstractQuery> QueryType offset(int offset)
	{
		this.offset = offset;

		return (QueryType) this;
	}

	public Map<String, Object> getDimensionValues()
	{
		return Collections.unmodifiableMap(dimensionValues);
	}

	public void setDimensionValues(Map<String, Object> dimensionValues) throws InvalidDimension
	{
		assert dimensionValues != null;

		this.dimensionValues.clear();

		for (Map.Entry<String, Object> entry : dimensionValues.entrySet()) {
			setDimensionValue(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Optional<QueryDimension> getDimension(String name)
	{
		assert name != null;

		for (QueryDimension dimension : getAllDimensions()) {
			if (dimension.getName().equals(name)) {
				return Optional.of(dimension);
			}
		}

		return Optional.empty();
	}

	@Override
	public <ReturnType> Optional<ReturnType> getDimensionValue(String name)
	{
		assert name != null;

		return Optional.ofNullable((ReturnType) dimensionValues.get(name));
	}

	@Override
	public <ReturnType> Optional<ReturnType> getDimensionValue(QueryDimension dimension)
	{
		assert dimension != null;

		return getDimensionValue(dimension.getName());
	}

	@Override
	public boolean hasDimensionValue(QueryDimension dimension)
	{
		assert dimension != null;

		return hasDimensionValue(dimension.getName());
	}

	@Override
	public boolean hasDimensionValue(String name)
	{
		assert name != null;

		return dimensionValues.containsKey(name);
	}

	public void setDimensionValue(String name, Object value) throws InvalidDimension
	{
		assert name != null;

		if (!isAvailableDimension(name)) {
			throw new InvalidDimension("Dimension " + name + " is not contained");
		}

		dimensionValues.put(name, value);
	}

	@Override
	@AttributeDL(ignore = true)
	public boolean isAvailableDimension(String name)
	{
		assert name != null;

		for (QueryDimension dimension : getAllDimensions()) {
			if (dimension.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	@Override
	@AttributeDL(ignore = true)
	public boolean isAvailableDimension(QueryDimension dimension)
	{
		assert dimension != null;

		return getAllDimensions().contains(dimension);
	}

	public <QueryType> QueryType and(QueryDimension dimension, Object value) throws InvalidDimension, ExclusiveDimensionPresent, RequiredDimensionMissing
	{
		assert dimension != null;

		if (!isAvailableDimension(dimension)) {
			throw new InvalidDimension(dimension);
		}

		// Make sure the exclusive dimensions are not contained
		for (String exclusiveDimension : dimension.getExclusiveDimensions()) {
			if (hasDimensionValue(exclusiveDimension)) {
				throw new ExclusiveDimensionPresent(dimension.getName(), exclusiveDimension);
			}
		}

		// Make sure the exclusive dimensions are not contained
		for (String requiredDimension : dimension.getRequiredDimensions()) {
			if (!hasDimensionValue(requiredDimension)) {
				throw new RequiredDimensionMissing(dimension.getName(), requiredDimension);
			}
		}

		if (value != null) {
			dimensionValues.put(dimension.getName(), value);
		}

		return (QueryType) this;
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	@Override
	public boolean isAscending()
	{
		return ascending;
	}

	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	@Override
	public String getOrderBy()
	{
		return orderBy;
	}

	public void setOrderBy(String orderBy) throws InvalidDimension
	{
		this.orderBy = orderBy;
	}

	@Override
	public boolean isWithTotalCount()
	{
		return withTotalCount;
	}

	public void setWithTotalCount(boolean withTotalCount)
	{
		this.withTotalCount = withTotalCount;
	}
}
