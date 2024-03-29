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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <ResultType>
 */
public interface Statement<ResultType>
{

	public final static Date NULL_DATE = new Date(-1);

	public void executeNoResult(Object... parameters) throws Exception;
	
	public void executeNoResult(String statement, Object... parameters) throws Exception;
	
	public <ResultType> ResultType executeQuerySingleEntity(Supplier<ResultType> factory, Object... parameters) throws Exception;
	
	public <ResultType> ResultType executeQuerySingleEntity(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception;
		
	public <ResultType> Optional<ResultType> executeQuerySingleOrNoEntity(Supplier<ResultType> factory, Object... parameters) throws Exception;
	
	public <ResultType> Optional<ResultType> executeQuerySingleOrNoEntity(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception;
	
	public <ResultType> List<ResultType> executeQueryManyEntities(Supplier<ResultType> factory, Object... parameters) throws Exception;
	
	public <ResultType> List<ResultType> executeQueryManyEntities(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception;

	public String getName();
}
