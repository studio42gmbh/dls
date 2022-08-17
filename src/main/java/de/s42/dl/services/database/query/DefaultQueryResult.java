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
import java.util.List;

/**
 *
 * @author Benjamin.Schiller
 * @param <ResultType>
 */
public class DefaultQueryResult<ResultType> implements QueryResult<ResultType>
{

	@AttributeDL(required = false, defaultValue = "-1")
	protected int totalCount = -1;

	@AttributeDL(required = true)
	protected List<ResultType> result;

	@AttributeDL(required = false, defaultValue = "-1")
	protected int limit = -1;

	@AttributeDL(required = false, defaultValue = "0")
	protected int offset = 0;

	@Override
	public boolean isEmpty()
	{
		return (result == null) || result.isEmpty();
	}

	@Override
	public List<ResultType> getResult()
	{
		return result;
	}

	public void setResult(List<ResultType> result)
	{
		this.result = result;
	}

	@Override
	public String toString()
	{
		return StringHelper.toString(this);
	}

	@Override
	public int getTotalCount()
	{
		return totalCount;
	}

	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
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
}
