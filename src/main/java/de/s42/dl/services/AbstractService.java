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
package de.s42.dl.services;

import de.s42.dl.services.remote.InvalidParameter;
import de.s42.dl.services.remote.ParameterRequired;

/**
 *
 * @author Benjamin Schiller
 */
public abstract class AbstractService implements Service
{

	protected String name;
	protected boolean inited;

	protected void initService() throws Exception
	{
		// implement in your service
	}

	protected void exitService() throws Exception
	{
		// implement in your service
	}

	protected void assertRequired(String name, Object value) throws ParameterRequired
	{
		if (value == null) {
			throw new ParameterRequired("Parameter " + name + " is required");
		}
	}

	protected void assertInited() throws ServiceNotInited
	{
		if (!isInited()) {
			throw new ServiceNotInited(this);
		}
	}

	protected void assertTrue(boolean condition, String message) throws InvalidParameter
	{
		if (!condition) {
			throw new InvalidParameter(message);
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean isInited()
	{
		return inited;
	}

	public void setInited(boolean inited)
	{
		this.inited = inited;
	}

	@Override
	public synchronized void init() throws Exception
	{
		if (isInited()) {
			return;
		}

		initService();

		setInited(true);
	}

	@Override
	public synchronized void exit() throws Exception
	{
		if (!isInited()) {
			return;
		}

		exitService();

		setInited(false);
	}
}
