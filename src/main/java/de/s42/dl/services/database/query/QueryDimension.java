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

import de.s42.base.strings.StringHelper;
import de.s42.dl.DLAttribute.AttributeDL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Benjamin.Schiller
 */
public class QueryDimension
{

	protected String name;

	@AttributeDL(required = true)
	protected Class type;
	
	@AttributeDL(required = false, defaultValue = "equal")
	protected QueryDimensionMatchType matchType = QueryDimensionMatchType.equal;
	
	@AttributeDL(required = false)
	protected Map<String, Object> hints = new HashMap<>();

	public QueryDimension()
	{

	}

	public QueryDimension(String name, Class type, QueryDimensionMatchType matchType, Map<String, Object> hints)
	{
		assert name != null;
		assert type != null;

		this.name = name;
		this.type = type;
		this.matchType = matchType;

		if (hints != null) {
			this.hints.putAll(hints);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Class getType()
	{
		return type;
	}

	public void setType(Class type)
	{
		this.type = type;
	}

	public Map<String, Object> getHints()
	{
		return Collections.unmodifiableMap(hints);
	}

	public void setHints(Map<String, Object> hints)
	{
		this.hints.clear();
		this.hints.putAll(hints);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.name);
		hash = 79 * hash + Objects.hashCode(this.type);
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final QueryDimension other = (QueryDimension) obj;
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		return Objects.equals(this.type, other.type);
	}

	@Override
	public String toString()
	{
		return StringHelper.toString(this);
	}

	public QueryDimensionMatchType getMatchType()
	{
		return matchType;
	}

	public void setMatchType(QueryDimensionMatchType matchType)
	{
		this.matchType = matchType;
	}
}
