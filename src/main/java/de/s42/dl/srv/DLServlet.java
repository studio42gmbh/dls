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

import de.s42.base.collections.MappedList;
import de.s42.dl.services.Service;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Benjamin Schiller
 */
@MultipartConfig()
public class DLServlet extends HttpServlet
{

	private final static Logger log = LogManager.getLogger(DLServlet.class.getName());

	protected ServletRemoteService remoteService;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		assert remoteService != null;

		try {
			remoteService.call(request, response);
		} catch (Error | Exception ex) {

			if (ex instanceof DLServletException) {
				log.error(((DLServletException)ex).getErrorCode(), ex.getMessage(), request.getRequestURL());
			}
			else {
				log.error(ex, ex.getMessage(), request.getRequestURL());
			}

			// Return unhandled exception as JSON errors
			if (!response.isCommitted()) {
				String errorMessage = ex.getMessage();
				String errorClass = ex.getClass().getName();

				String errorCode;
				if (ex instanceof ErrorCode) {
					errorCode = ((ErrorCode) ex).getErrorCode();
					response.setStatus(((ErrorCode) ex).getHttpStatus());
				} else {
					errorCode = ex.getClass().getSimpleName().toUpperCase();
					response.setStatus(500);
				}

				response.setHeader("Cache-Control", "private");
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				try ( PrintWriter out = response.getWriter()) {

					out.print(
						"{\"error\":\""
						+ (errorMessage != null ? errorMessage.replaceAll("\"", "\\\\\"").replaceAll("\n", "") : "") + "\""
						+ ", \"errorClass\":\"" + errorClass + "\""
						+ ", \"errorCode\":\"" + errorCode + "\""
						+ "}");
					out.flush();
				}
			}
		}
	}

	@Override
	public void init() throws ServletException
	{
		log.info("init");

		super.init();

		MappedList<String, Service> services = (MappedList<String, Service>) getServletContext().getAttribute(DLServlet.class.getName() + ".services");

		if (services == null) {
			throw new ServletException("Missing valid services in servlet context '" + DLServlet.class.getName() + ".services'");
		}

		for (Service service : services.values()) {
			if (service instanceof ServletRemoteService) {
				remoteService = (ServletRemoteService) service;
				break;
			}
		}

		if (remoteService == null) {
			throw new ServletException("Missing valid remoteService in services");
		}
	}

	@Override
	public void destroy()
	{
		log.info("destroy");

		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		processRequest(request, response);
	}

	@Override
	public String getServletInfo()
	{
		return getClass().getName();
	}
}
