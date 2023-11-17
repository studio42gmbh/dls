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
package de.s42.dl.srv;

import javax.servlet.ServletException;

/**
 *
 * @author Benjamin Schiller
 */
public class DLServletException extends ServletException implements ErrorCode
{
	
	protected final String errorCode;
	protected final int httpStatus;

	public DLServletException()
	{
		errorCode = getClass().getSimpleName().toUpperCase();
		httpStatus = 500;
	}

	public DLServletException(String msg)
	{
		super(msg);
		
		errorCode = getClass().getSimpleName().toUpperCase();
		httpStatus = 500;
	}
	
	public DLServletException(String msg, String errorCode)
	{
		super(msg);
		
		this.errorCode = errorCode;
		httpStatus = 500;
	}
	
	public DLServletException(String msg, String errorCode, int httpStatus)
	{
		super(msg);
		
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
	
	public DLServletException(String msg, Throwable cause)
	{
		super(msg, cause);
		
		errorCode = getClass().getSimpleName().toUpperCase();
		httpStatus = 500;
	}

	public DLServletException(String msg, Throwable cause, String errorCode)
	{
		super(msg, cause);
		
		this.errorCode = errorCode;
		httpStatus = 500;
	}

	public DLServletException(String msg, Throwable cause, String errorCode, int httpStatus)
	{
		super(msg, cause);
		
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}
	
	@Override
	public String getErrorCode()
	{
		return errorCode;
	}

	@Override
	public int getHttpStatus()
	{
		return httpStatus;
	}
}
