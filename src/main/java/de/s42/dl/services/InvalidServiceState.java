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

import de.s42.dl.services.permission.*;
import de.s42.dl.srv.DLServletException;

/**
 *
 * @author Benjamin Schiller
 */
public class InvalidServiceState extends DLServletException
{

	public final static String DEFAULT_MESSAGE = "Service is in an invalid state";
	public final static String ERROR_CODE = "INVALID_SERVICE_STATE";
	public final static int HTTP_STATUS = 500;

	public InvalidServiceState()
	{
		super(DEFAULT_MESSAGE, ERROR_CODE, HTTP_STATUS);
	}

	public InvalidServiceState(String msg)
	{
		super(msg, ERROR_CODE, HTTP_STATUS);
	}

	public InvalidServiceState(Throwable cause)
	{
		super(DEFAULT_MESSAGE, cause, ERROR_CODE, HTTP_STATUS);
	}

	public InvalidServiceState(String msg, Throwable cause)
	{
		super(msg, cause, ERROR_CODE, HTTP_STATUS);
	}
	
	public InvalidServiceState(Service service)
	{
		super("Service " + service.getName() + " of type " + service.getClass().getName() + " is in an invalid state", ERROR_CODE, HTTP_STATUS);
	}	
}
