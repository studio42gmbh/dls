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

import de.s42.dl.services.ServiceResult;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Benjamin Schiller
 */
public interface ErrorCode
{

	public String getMessage();

	public String getErrorCode();

	public int getHttpStatus();

	public static ErrorCode create(String message, String errorCode, int httpStatus)
	{
		return new DefaultErrorCode(message, errorCode, httpStatus);
	}

	public static ServiceResult serviceError(Exception ex)
	{
		return ServiceResult.ofError(new DefaultErrorCode(ex.getMessage(), ex.getClass().getSimpleName().toUpperCase(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
	}
	
	public static ServiceResult serviceError(String message, String errorCode, int httpStatus)
	{
		return ServiceResult.ofError(new DefaultErrorCode(message, errorCode, httpStatus));
	}
	
	public static ServiceResult serviceError403Forbidden(String message)
	{
		return ServiceResult.ofError(new DefaultErrorCode(message, "FORBIDDEN", HttpServletResponse.SC_FORBIDDEN));
	}
	
	public static ServiceResult serviceError404NotFound(String message)
	{
		return ServiceResult.ofError(new DefaultErrorCode(message, "NOT_FOUND", HttpServletResponse.SC_NOT_FOUND));
	}
}
