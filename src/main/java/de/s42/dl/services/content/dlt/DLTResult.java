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

import de.s42.base.resources.ResourceHelper;
import de.s42.dl.srv.DLServletException;
import de.s42.dl.services.remote.StreamResult;
import de.s42.dlt.DLT;
import de.s42.dlt.parser.DefaultTemplateContext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 * @author Benjamin Schiller
 */
public class DLTResult implements StreamResult
{

	protected final String resourceName;

	protected final byte[] evaluated;

	protected final int ttl;
	protected final boolean inline;
	protected final String encoding;
	protected final String fileName;
	protected final String mimeType;

	public DLTResult(String resourceName) throws DLServletException
	{
		this(resourceName, null);
	}

	@SuppressWarnings("null")
	public DLTResult(String resourceName, Map<String, Object> bindings) throws DLServletException
	{
		assert resourceName != null;

		this.resourceName = resourceName;

		if (!ResourceHelper.hasResource(resourceName)) {
			throw new DLServletException("Resource '" + resourceName + "' does not exist", "RESOURCE_NOT_FOUND", 404);
		}

		String dlt;
		try {
			dlt = ResourceHelper.getResourceAsString(resourceName).orElseThrow();
		} catch (IOException ex) {
			// This should not happen
			throw new DLServletException("Resource '" + resourceName + "' does not exist", "RESOURCE_NOT_FOUND", 404);
		}

		DefaultTemplateContext context = new DefaultTemplateContext();

		if (bindings != null) {
			context.addBindings(bindings);
		}

		String evaluatedString;
		try {
			evaluatedString = DLT.evaluate(dlt, context);
		} catch (Exception ex) {
			throw new DLServletException("Error evaluating template '" + resourceName + "' - " + ex.getMessage(), ex, "ERROR_PARSING_TEMPLATE", 500);
		}

		evaluated = evaluatedString.getBytes();

		mimeType = context.hasBinding("mimeType") ? (String) context.getBinding("mimeType") : "text/plain";
		encoding = context.hasBinding("encoding") ? (String) context.getBinding("encoding") : null;
		fileName = context.hasBinding("fileName") ? (String) context.getBinding("fileName") : resourceName;
		inline = context.hasBinding("inline") ? (Boolean) context.getBinding("inline") : true;
		ttl = context.hasBinding("ttl") ? (Integer) context.getBinding("ttl") : 0;
	}

	@Override
	public long stream(OutputStream out) throws IOException
	{
		out.write(evaluated);

		return evaluated.length;
	}

	@Override
	public int getTtl()
	{
		return ttl;
	}

	@Override
	public String getMimeType()
	{
		return mimeType;
	}

	@Override
	public String getEncoding()
	{
		return encoding;
	}

	@Override
	public String getFileName()
	{
		return fileName;
	}

	@Override
	public boolean isInline()
	{
		return inline;
	}

	public String getResourceName()
	{
		return resourceName;
	}
}
