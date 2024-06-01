// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2023 Studio 42 GmbH ( https://www.s42m.de ).
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

import de.s42.log.LogManager;
import de.s42.log.Logger;

/**
 *
 * @author Benjamin Schiller
 */
public class DefaultErrorCode implements ErrorCode
{

	private final static Logger log = LogManager.getLogger(DefaultErrorCode.class.getName());

	protected final String message;
	protected final String errorCode;
	protected final int httpStatus;

	public DefaultErrorCode(String message, String errorCode, int httpStatus)
	{
		assert message != null;
		assert errorCode != null;
		assert httpStatus >= 0;

		this.message = message;
		this.errorCode = errorCode;
		this.httpStatus = httpStatus;
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	@Override
	public String getMessage()
	{
		return message;
	}
	
	// Needed for DL serialization
	public void setMessage(String message)
	{
		throw new UnsupportedOperationException("May not write this value");
	}
	
	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
	
	// Needed for DL serialization
	public void setErrorCode(String errorCode)
	{
		throw new UnsupportedOperationException("May not write this value");
	}
	
	@Override
	public int getHttpStatus()
	{
		return httpStatus;
	}
	
	// Needed for DL serialization
	public void getHttpStatus(int httpStatus)
	{
		throw new UnsupportedOperationException("May not write this value");
	}
	//</editor-fold>
}
