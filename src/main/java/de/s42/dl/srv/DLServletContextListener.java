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
import de.s42.dl.DLCore;
import de.s42.dl.DLInstance;
import de.s42.dl.DLModule;
import de.s42.dl.DLType;
import de.s42.dl.exceptions.DLException;
import de.s42.dl.services.Service;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.lang.reflect.InvocationTargetException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author Benjamin Schiller
 */
public class DLServletContextListener implements ServletContextListener
{

	private final static Logger log = LogManager.getLogger(DLServletContextListener.class.getName());

	protected final MappedList<String, Service> services = new MappedList<>();

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		try {
			log.debug("contextInitialized");

			String coreClass = sce.getServletContext().getInitParameter(getClass().getName() + ".coreClass");
			String configuration = sce.getServletContext().getInitParameter(getClass().getName() + ".configuration");

			InitialContext cxt = new InitialContext();
			String dlConfiguration = (String) cxt.lookup("java:/comp/env/" + configuration);

			log.info("dlConfiguration", dlConfiguration);

			DLCore core = (DLCore) Class.forName(coreClass).getConstructor().newInstance();
			DLType serviceType = core.getType(Service.class).orElseThrow();

			DLModule module = core.parse(dlConfiguration);

			// Init services
			for (DLInstance child : module.getChildren()) {

				if (serviceType.isAssignableFrom(child.getType())) {

					Service service = child.toJavaObject(core);
					service.init();
				}
			}

			// Map services
			for (DLInstance exported : core.getExported()) {

				if (serviceType.isAssignableFrom(exported.getType())) {

					Service service = exported.toJavaObject(core);
					services.add(service.getName(), service);
				}
			}

			sce.getServletContext().setAttribute(DLServlet.class.getName() + ".services", services);
		} catch (DLException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | NamingException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		log.debug("contextDestroyed");

		sce.getServletContext().removeAttribute(DLServlet.class.getName() + ".services");

		// Exit services
		for (Service service : services.values()) {
			service.exit();
		}

		services.clear();
	}
}
