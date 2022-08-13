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

import de.s42.base.resources.ResourceHelper;
import de.s42.dlt.DLT;
import de.s42.dlt.parser.CompiledTemplate;
import de.s42.dlt.parser.TemplateContext;
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
public class DynamicStatement<ResultType> extends AbstractStatement<ResultType>
{

	private final static Logger log = LogManager.getLogger(DynamicStatement.class.getName());

	protected CompiledTemplate template;
	protected Supplier<ResultType> factory;

	public DynamicStatement(DatabaseService databaseService, Supplier<ResultType> factory, String templateResourceName) throws Exception
	{
		super();

		this.databaseService = databaseService;
		this.factory = factory;

		template = DLT.compile(ResourceHelper.getResourceAsString(templateResourceName).orElseThrow());
	}

	public List<ResultType> executeMany(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		String stat = template.evaluate(context);

		log.debug("executeMany", stat);

		return executeQueryManyEntities(stat, factory, parameters);
	}

	public Optional<ResultType> executeOneOrNone(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		String stat = template.evaluate(context);

		log.debug("executeOneOrNone", stat);

		return executeQuerySingleOrNoEntity(stat, factory, parameters);
	}

	public ResultType executeOne(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		String stat = template.evaluate(context);

		log.debug("executeOne", stat);

		return executeQuerySingleEntity(stat, factory, parameters);
	}
}
