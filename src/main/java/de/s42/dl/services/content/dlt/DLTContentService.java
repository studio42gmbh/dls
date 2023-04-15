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
package de.s42.dl.services.content.dlt;

import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.dl.services.AbstractService;
import de.s42.dl.services.DLMethod;
import de.s42.dl.services.DLParameter;
import de.s42.dl.services.DLService;
import de.s42.dl.services.EntityNotFound;
import de.s42.dl.services.content.ContentService;
import de.s42.dl.services.remote.StreamResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Benjamin Schiller
 */
@DLService
public class DLTContentService extends AbstractService implements ContentService
{

	@AttributeDL(required = false, defaultValue = "")
	protected String basePath = "";

	@AttributeDL(required = false)
	protected Map<String, Object> bindings = Collections.synchronizedMap(new HashMap<>());

	@AttributeDL(required = false)
	protected String filter;

	@AttributeDL(ignore = true)
	protected Pattern filterPattern;

	@Override
	protected void initService() throws Exception
	{
		super.initService();

		// Loading filter regex
		if (filter != null) {
			filterPattern = Pattern.compile(filter);
		}
	}

	@Override
	@DLMethod
	public StreamResult stream(
		@DLParameter(value = "id", required = true) String id
	) throws Exception
	{
		// Filter path if the filter is set -> If it does not match -> return a 404
		if (filterPattern != null) {
			if (!filterPattern.matcher(id).matches()) {
				throw new EntityNotFound("Resource '" + id + "' was not found");
			}
		}

		DLTResult result = new DLTResult(getBasePath() + "/" + id, getBindings());

		return result;
	}

	public String getBasePath()
	{
		return basePath;
	}

	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}

	public Map<String, Object> getBindings()
	{
		return Collections.unmodifiableMap(bindings);
	}

	public void setBindings(Map<String, Object> bindings)
	{
		assert bindings != null;

		this.bindings.clear();
		this.bindings.putAll(bindings);
	}

	public String getFilter()
	{
		return filter;
	}

	public void setFilter(String filter)
	{
		this.filter = filter;
	}
}
