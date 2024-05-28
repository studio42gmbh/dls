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

import de.s42.dl.services.remote.ServletRemoteService;
import de.s42.base.collections.MappedList;
import de.s42.dl.services.Service;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Benjamin Schiller
 */
@MultipartConfig
public class DLServlet extends HttpServlet
{

	private final static Logger log = LogManager.getLogger(DLServlet.class.getName());

	protected ServletRemoteService remoteService;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		assert request != null;
		assert response != null;
		assert remoteService != null;

		//log.debug("processRequest");

		try {
			remoteService.call(request, response);
		} catch (Throwable ex) {
			remoteService.sendErrorResponse(request, response, ex);
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

		// Scan for the first service of type ServletRemoteService -> use as remote bridge
		for (Service service : services.values()) {
			if (service instanceof ServletRemoteService) {
				remoteService = (ServletRemoteService) service;
				break;
			}
		}

		if (remoteService == null) {
			throw new ServletException("Missing valid remote service of type ServletRemoteService in services (defined in configuration DL)");
		}
	}

	@Override
	public void destroy()
	{
		log.info("destroy");

		super.destroy();

		remoteService = null;
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
