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
package de.s42.dl.services.remote;

import de.s42.dl.services.permission.PermissionService;
import de.s42.base.collections.MappedList;
import de.s42.base.conversion.ConversionHelper;
import de.s42.base.files.FilesHelper;
import de.s42.base.strings.StringHelper;
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
import de.s42.dl.services.database.DatabaseService;
import de.s42.dl.services.l10n.LocalizationService;
import de.s42.dl.srv.DLServletException;
import de.s42.dl.srv.ErrorCode;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.JSONObject;

/**
 *
 * @author Benjamin Schiller
 */
// @todo optimize service method caching (lookup and permissions)
public class DefaultServletRemoteService extends AbstractService implements ServletRemoteService, DLContainer<DynamicServletParameter>
{

	private final static Logger log = LogManager.getLogger(DefaultServletRemoteService.class.getName());

	@AttributeDL(required = false, defaultValue = "true")
	protected boolean validatePermissions = true;

	@AttributeDL(required = false)
	protected PermissionService permissionService;

	@AttributeDL(required = false)
	protected DatabaseService databaseService;

	@AttributeDL(required = false)
	protected LocalizationService localizationService;

	@AttributeDL(required = true)
	protected DLCore core;

	@AttributeDL(ignore = true)
	protected final MappedList<String, Service> services = new MappedList<>();

	@AttributeDL(ignore = true)
	protected final MappedList<String, ServiceDescriptor> serviceDescriptors = new MappedList<>();

	@AttributeDL(ignore = true)
	protected final Map<String, DynamicServletParameter> dynamicParameters = new HashMap<>();

	protected ServiceDescriptor[] serviceDescriptorsArray;

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

						DLService dlService = service.getClass().getAnnotation(DLService.class);

						// Just add classes with annotation DLService
						if (dlService != null) {
							services.add(service.getName(), service);
							serviceDescriptors.add(service.getName(), new ServiceDescriptor(service, localizationService));
						}
					}
				}
			}

			serviceDescriptorsArray = serviceDescriptors.values().toArray(ServiceDescriptor[]::new);

			Arrays.sort(serviceDescriptorsArray);

		} catch (DLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void exitService()
	{
		log.info("exitService");
	}

	protected void setTTL(HttpServletResponse response, int ttl)
	{
		assert response != null;
		assert ttl >= 0;

		if (ttl > 0) {
			response.setHeader("Cache-Control", "public, max-age=" + ttl);
		} else {
			response.setHeader("Cache-Control", "private");
		}
	}

	protected void sendStreamedResponse(HttpServletResponse response, StreamResult result) throws IOException
	{
		assert response != null;
		assert result != null;

		// TTL
		setTTL(response, result.getTtl());

		// Mime Type
		response.setContentType(result.getMimeType());

		// Encoding
		String encoding = result.getEncoding();

		if (encoding != null) {
			response.setCharacterEncoding(encoding);
		}

		// Disposition inline/attachment
		response.setHeader(
			"Content-Disposition",
			"" + (result.isInline() ? "inline" : "attachment")
			+ "; filename=\"" + result.getFileName() + "\"");

		// Send file to client
		try ( OutputStream out = response.getOutputStream()) {
			long bytesWritten = result.stream(out);
			out.flush();
			out.close();
			log.info("Sent streamed response", StringHelper.toString(result), bytesWritten);
		}
	}

	protected void sendJSONResponse(HttpServletResponse response, Object result, int ttl) throws IOException, DLException
	{
		assert response != null;
		assert ttl >= 0;

		// Send JSON result to client
		setTTL(response, ttl);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

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

	protected void sendResponse(HttpServletResponse response, Object result, int ttl) throws IOException, DLException
	{
		assert response != null;
		assert ttl >= 0;

		// Unpack Optional results
		if (result instanceof Optional) {
			result = ((Optional) result).orElse(null);
		}

		// Send stream results
		if (result instanceof StreamResult) {
			sendStreamedResponse(response, (StreamResult) result);
			return;
		}

		// Default to sending result as JSON
		sendJSONResponse(response, result, ttl);
	}

	protected FileRef getRequestParameterFileRef(HttpServletRequest request, DLParameter dlParameter) throws IOException, ServletException
	{
		if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
			throw new ParameterRequired("File parameter '" + dlParameter.value() + "' needs to be posted as 'multipart/form-data'");
		}

		Part p = request.getPart(dlParameter.value());
		if (p != null) {

			if (p.getSize() > dlParameter.maxLength()) {
				throw new ParameterTooLong("Parameter '" + dlParameter.value() + "' has a max length of " + dlParameter.maxLength() + " but is " + p.getSize());
			}

			try ( InputStream in = p.getInputStream()) {

				byte[] data = new byte[(int) p.getSize()];
				in.read(data);
				File folder = (File) request.getServletContext().getAttribute(ServletContext.TEMPDIR);
				File tempFile = File.createTempFile("dl-", "", folder);
				log.debug("Temp File: " + tempFile.getAbsolutePath());

				FilesHelper.writeByteArrayToFile(tempFile.getAbsolutePath(), data);

				Map<String, Object> attributes = new HashMap();

				return new FileRef(tempFile.getAbsolutePath(), p.getContentType(), p.getSubmittedFileName(), attributes);
			}
		}

		return null;
	}

	protected <DataType> DataType getRequestParameter(HttpServletRequest request, DLParameter dlParameter) throws IOException, ServletException
	{
		String key = dlParameter.value();

		if (request.getContentType() != null
			&& (request.getContentType().startsWith("application/json")
			//see https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS -> Simple Request avoids Preflight
			|| (request.getContentType().startsWith("text/plain")))) {

			JSONObject requestAsJSON = (JSONObject) request.getAttribute("_jsonParameters");

			if (requestAsJSON == null) {

				StringBuilder jb = new StringBuilder();
				String line;

				BufferedReader reader = request.getReader();
				while ((line = reader.readLine()) != null) {
					jb.append(line);
				}

				requestAsJSON = new JSONObject(jb.toString());

				request.setAttribute("_jsonParameters", requestAsJSON);
			}

			if (requestAsJSON.has(key) && !requestAsJSON.isNull(key)) {
				return (DataType) requestAsJSON.get(key).toString();
			}

			return null;
		} else if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {

			Object requestAsPart = request.getAttribute("_partParameter_" + key);

			if (requestAsPart == null) {

				//@todo Handle to big uploads properly - currently nothing is returned to client
				Part p = request.getPart(key);
				if (p != null) {

					if (p.getSubmittedFileName() != null) {

						try ( InputStream in = p.getInputStream()) {
							byte[] data = new byte[(int) p.getSize()];
							in.read(data);
							File folder = (File) request.getServletContext().getAttribute(ServletContext.TEMPDIR);
							//log.debug("Temp Folder: " + folder.getAbsolutePath());
							File tempFile = File.createTempFile("dl-", "", folder);
							log.debug("Temp File: " + tempFile.getAbsolutePath());

							FilesHelper.writeByteArrayToFile(tempFile.getAbsolutePath(), data);

							Map<String, Object> attributes = new HashMap();
							FileRef ref = new FileRef(tempFile.getAbsolutePath(), p.getContentType(), p.getSubmittedFileName(), attributes);

							requestAsPart = ref;
						}
					} else {

						try ( InputStream in = p.getInputStream()) {
							byte[] data = new byte[(int) p.getSize()];
							in.read(data);
							requestAsPart = new String(data, "UTF-8");
						}
					}

					request.setAttribute("_partParameter_" + key, requestAsPart);
				}
			}

			return (DataType) requestAsPart;
		}

		return (DataType) request.getParameter(key);
	}

	protected Object getParameter(HttpServletRequest request, HttpServletResponse response, ParameterDescriptor parameter) throws ServletException, IOException
	{
		assert request != null;
		assert parameter != null;

		DLParameter dlParameter = parameter.getDlParameter();

		String key = dlParameter.value();

		Object result;

		// Dynamic parameters
		if (key.startsWith("$")) {

			String dynamicKey = key.substring(1);
			DynamicServletParameter dynamicParameter = dynamicParameters.get(dynamicKey);

			if (dynamicParameter == null) {

				if (dlParameter.required()) {
					throw new ParameterRequired("Dynamic parameter '" + dlParameter.value() + "' is required");
				}

				return null;
			}

			result = dynamicParameter.resolve(request, response, dynamicKey);
		} // Static parameters
		else {

			if (FileRef.class.isAssignableFrom(parameter.getType())) {
				result = getRequestParameterFileRef(request, dlParameter);
			} else {
				result = getRequestParameter(request, dlParameter);

				if (result != null && ((String) result).length() > dlParameter.maxLength()) {
					throw new ParameterTooLong("Parameter '" + dlParameter.value() + "' has a max length of " + dlParameter.maxLength() + " but is " + ((String) result).length());
				}

				// Set to default value if no value is given
				if (result == null && !dlParameter.defaultValue().isBlank()) {
					result = dlParameter.defaultValue();
				}
			}

			result = ConversionHelper.convert(result, parameter.getType());
		}

		if (dlParameter.required() && result == null) {
			throw new ParameterRequired("Parameter '" + dlParameter.value() + "' is required");
		}

		return result;
	}

	protected void validatePermissions(HttpServletRequest request, DLService service, DLMethod method) throws Exception
	{
		assert request != null;
		assert service != null;
		assert method != null;

		if (!isValidatePermissions() || (permissionService == null)) {
			return;
		}

		boolean userLoggedIn = false;
		Set<String> permissions = new HashSet<>();

		userLoggedIn |= service.userLoggedIn();

		for (String permission : service.permissions()) {
			if (!permission.isBlank()) {
				permissions.add(permission);
			}
		}

		userLoggedIn |= method.userLoggedIn();

		for (String permission : method.permissions()) {
			if (!permission.isBlank()) {
				permissions.add(permission);
			}
		}

		if (userLoggedIn || !permissions.isEmpty()) {
			permissionService.validate(request, userLoggedIn, permissions);
		}
	}

	@Override
	public void call(HttpServletRequest request, HttpServletResponse response) throws Throwable
	{
		assert request != null;
		assert response != null;

		// @todo make service method determination more generic (patterns, subpaths, ...)
		String pathInfo = request.getPathInfo();

		if (pathInfo == null) {
			throw new InvalidPath("Pathinfo has to be of structure /<servicename>/<servicemethod>");
		}

		String[] pathParts = pathInfo.split("/");

		if (pathParts.length != 3) {
			throw new InvalidPath("Pathinfo has to be of structure /<servicename>/<servicemethod>");
		}

		String serviceName = pathParts[1];

		if (serviceName == null || serviceName.isBlank()) {
			throw new InvalidPath("Service is required");
		}

		String methodName = pathParts[2];

		if (methodName == null || methodName.isBlank()) {
			throw new InvalidPath("Method is required");
		}

		Optional<ServiceDescriptor> optService = serviceDescriptors.get(serviceName);

		if (optService.isEmpty()) {
			throw new UnknownService("Service " + serviceName + " is not mapped");
		}

		ServiceDescriptor service = optService.orElseThrow();

		Optional<MethodDescriptor> optMethod = service.getMethod(methodName);

		if (optMethod.isEmpty()) {
			throw new UnknownMethod("Method " + methodName + " is not mapped");
		}

		MethodDescriptor method = optMethod.orElseThrow();

		validatePermissions(request, service.getDlService(), method.getDlMethod());

		ParameterDescriptor[] parameters = method.getParameters();

		Object[] callParams = new Object[parameters.length];

		for (int i = 0; i < parameters.length; ++i) {

			ParameterDescriptor parameter = parameters[i];

			callParams[i] = getParameter(request, response, parameter);
		}

		log.debug("Calling", serviceName, methodName);

		// If the method shall be transactioned and the database service is not already in a transaction
		boolean transaction = method.isTransactioned() & ((databaseService != null) ? !databaseService.isInTransaction() : false);

		if (transaction) {
			databaseService.startTransaction();
		}

		try {
			try {
				Object result = method.call(callParams);

				if (transaction) {
					databaseService.commitTransaction();
				}

				sendResponse(response, result, method.getTtl());
			} catch (InvocationTargetException ex) {

				throw ex.getCause();
			}

		} catch (Throwable ex) {
			if (transaction) {
				databaseService.rollbackTransaction();
			}
			throw ex;
		}
	}

	@Override
	public void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, Throwable ex)
	{
		if (ex instanceof DLServletException) {
			log.error(((DLServletException) ex).getErrorCode(), ex.getMessage(), request.getRequestURL());

			// @todo For debug purposes verbose atm - make this configurable
			log.error(ex);
		} else {
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
			} // Default other execeptions to be 500 and the name of the class as errorCode
			else {
				errorCode = ex.getClass().getSimpleName().toUpperCase();
				response.setStatus(500);
			}

			response.setHeader("Cache-Control", "private");
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			try ( PrintWriter out = response.getWriter()) {

				out.print(
					"{\"error\":\""
					+ (errorMessage != null
						? errorMessage
							.replaceAll("\n", "")
							.replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\"", "\\\\\"") : "") + "\""
					+ ", \"errorClass\":\"" + errorClass + "\""
					+ ", \"errorCode\":\"" + errorCode + "\""
					+ "}");
				out.flush();
			} catch (IOException ex1) {
				log.error(ex, "Error writing error response");
			}
		}

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

	public Set<Service> getServices()
	{
		return services.values();
	}

	public ServiceDescriptor[] getServiceDescriptors()
	{
		return serviceDescriptorsArray;
	}

	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}

	public void setLocalizationService(LocalizationService localizationService)
	{
		this.localizationService = localizationService;
	}

	public DatabaseService getDatabaseService()
	{
		return databaseService;
	}

	public void setDatabaseService(DatabaseService databaseService)
	{
		this.databaseService = databaseService;
	}
}
