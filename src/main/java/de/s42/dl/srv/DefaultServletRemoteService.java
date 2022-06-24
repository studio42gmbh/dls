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
import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.dl.DLCore;
import de.s42.dl.DLInstance;
import de.s42.dl.DLType;
import de.s42.dl.exceptions.DLException;
import de.s42.dl.io.json.JsonWriter;
import de.s42.dl.java.DLContainer;
import de.s42.dl.services.AbstractService;
import de.s42.dl.services.DLMethod;
import de.s42.dl.services.DLParameter;
import de.s42.dl.services.DLService;
import de.s42.dl.services.Service;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Benjamin Schiller
 */
// @todo optimize service method caching (lookup and permissions)
public class DefaultServletRemoteService extends AbstractService implements ServletRemoteService, DLContainer<DynamicServletParameter>
{

	public final static String INVALID_PATH = "INVALID_PATH";
	public final static String UNKNOWN_SERVICE = "UNKNOWN_SERVICE";
	public final static String UNKNOWN_METHOD = "UNKNOWN_METHOD";
	public final static String PARAMETER_REQUIRED = "PARAMETER_REQUIRED";
	public final static String USER_NOT_LOGGED_IN = "USER_NOT_LOGGED_IN";
	public final static String PERMISSION_MISSING = "PERMISSION_MISSING";
	public final static String PARAMETER_TOO_LONG = "PARAMETER_TOO_LONG";

	private final static Logger log = LogManager.getLogger(DefaultServletRemoteService.class.getName());

	@AttributeDL(required = false, defaultValue = "true")
	protected boolean validatePermissions = true;

	@AttributeDL(required = false)
	protected PermissionService permissionService;

	@AttributeDL(required = true)
	protected DLCore core;

	@AttributeDL(ignore = true)
	protected final MappedList<String, Service> services = new MappedList<>();

	@AttributeDL(ignore = true)
	protected final Map<String, DynamicServletParameter> dynamicParameters = new HashMap<>();

	@Override
	public void addChild(String name, DynamicServletParameter child)
	{
		assert name != null;
		assert child != null;

		dynamicParameters.put(name, child);
	}

	@Override
	protected void initService()
	{
		log.info("initService");

		try {

			// Init services
			DLType serviceType = core.getType(Service.class).orElseThrow();
			for (DLInstance exported : core.getExported()) {

				if (serviceType.isAssignableFrom(exported.getType())) {

					Service service = exported.toJavaObject(core);

					// Dont expose itself
					if (service != this) {
						services.add(service.getName(), service);
					}
				}
			}
		} catch (DLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void exitService()
	{
		log.info("exitService");
	}

	protected void sendResponse(HttpServletResponse response, Object result, int ttl) throws IOException, DLException
	{
		assert response != null;
		assert ttl >= 0;

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		if (ttl > 0) {
			response.setHeader("Cache-Control", "public, max-age=" + ttl);
		} else {
			response.setHeader("Cache-Control", "private");
		}

		if (result != null) {
			result = JsonWriter.toJSON(core.convertFromJavaObject(result)).toString();
		}

		if (result != null) {
			try ( PrintWriter out = response.getWriter()) {
				out.print(result);
				out.flush();
			}
		}
	}

	protected Object getParameter(HttpServletRequest request, Parameter parameter) throws ServletException
	{
		assert request != null;
		assert parameter != null;

		DLParameter dlParameter = parameter.getAnnotation(DLParameter.class);

		if (dlParameter == null) {
			throw new DLServletException("Parameter is required to have a DLParameter annotation", PARAMETER_REQUIRED, 400);
		}

		String key = dlParameter.value();

		Object result;

		if (key.startsWith("$")) {

			String dynamicKey = key.substring(1);
			DynamicServletParameter dynamicParameter = dynamicParameters.get(dynamicKey);

			if (dynamicParameter == null) {
				return null;
			}

			result = dynamicParameter.resolve(request, dynamicKey);
		} else {
			result = request.getParameter(key);

			if (result != null && ((String) result).length() > dlParameter.maxLength()) {
				throw new DLServletException("Parameter '" + dlParameter.value() + "' has a max length of " + dlParameter.maxLength() + " but is " + ((String) result).length(), PARAMETER_TOO_LONG, 400);
			}
		}

		if (dlParameter.required() && result == null) {
			throw new DLServletException("Parameter '" + dlParameter.value() + "' is required", PARAMETER_REQUIRED, 400);
		}

		return result;
	}

	protected void validatePermissions(HttpServletRequest request, DLService service, DLMethod method) throws Exception
	{
		assert request != null;

		if (!isValidatePermissions() || (getPermissionService() == null)) {
			return;
		}

		boolean userLoggedIn = false;
		Set<String> permissions = new HashSet<>();

		if (service != null) {

			userLoggedIn |= service.userLoggedIn();

			for (String permission : service.permissions()) {
				if (!permission.isBlank()) {
					permissions.add(permission);
				}
			}
		}

		if (method != null) {

			userLoggedIn |= method.userLoggedIn();

			for (String permission : method.permissions()) {
				if (!permission.isBlank()) {
					permissions.add(permission);
				}
			}
		}

		if (userLoggedIn || !permissions.isEmpty()) {
			getPermissionService().validate(request, userLoggedIn, permissions);
		}
	}

	@Override
	public void call(HttpServletRequest request, HttpServletResponse response) throws Exception, RuntimeException
	{
		assert request != null;
		assert response != null;

		// @todo make service method determination more generic (patterns, subpaths, ...)
		String pathInfo = request.getPathInfo();

		if (pathInfo == null) {
			throw new DLServletException("Pathinfo has to be of structure /<servicename>/<servicemethod>", INVALID_PATH, 400);
		}

		String[] pathParts = pathInfo.split("/");

		if (pathParts.length != 3) {
			throw new DLServletException("Pathinfo has to be of structure /<servicename>/<servicemethod>", INVALID_PATH, 400);
		}

		String serviceName = pathParts[1];

		if (serviceName == null || serviceName.isBlank()) {
			throw new DLServletException("Service is required", INVALID_PATH, 400);
		}

		String methodName = pathParts[2];

		if (methodName == null || methodName.isBlank()) {
			throw new DLServletException("Method is required", INVALID_PATH, 400);
		}

		Optional<Service> serviceOpt = services.get(serviceName);

		if (serviceOpt.isEmpty()) {
			throw new DLServletException("Service " + serviceName + " is not mapped", UNKNOWN_SERVICE, 404);
		}

		Service service = serviceOpt.orElseThrow();

		DLService dlService = service.getClass().getAnnotation(DLService.class);

		for (Method method : service.getClass().getMethods()) {

			DLMethod dlMethod = method.getAnnotation(DLMethod.class);

			if (dlMethod != null) {

				String serviceMethodName = !dlMethod.value().isBlank() ? dlMethod.value() : method.getName();

				if (methodName.equals(serviceMethodName)) {

					validatePermissions(request, dlService, dlMethod);

					Parameter[] parameters = method.getParameters();

					Object[] callParams = new Object[parameters.length];

					for (int i = 0; i < parameters.length; ++i) {

						Parameter parameter = parameters[i];

						callParams[i] = getParameter(request, parameter);
					}

					log.debug("Calling", serviceName, methodName);

					sendResponse(response, method.invoke(service, callParams), dlMethod.ttl());

					return;
				}
			}
		}

		throw new DLServletException("Method " + methodName + " is not mapped", UNKNOWN_METHOD, 404);
	}

	public boolean isValidatePermissions()
	{
		return validatePermissions;
	}

	public void setValidatePermissions(boolean validatePermissions)
	{
		this.validatePermissions = validatePermissions;
	}

	public DLCore getCore()
	{
		return core;
	}

	public void setCore(DLCore core)
	{
		this.core = core;
	}

	public PermissionService getPermissionService()
	{
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}
}
