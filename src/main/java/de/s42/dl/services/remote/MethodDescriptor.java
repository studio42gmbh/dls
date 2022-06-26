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

import de.s42.dl.services.DLMethod;
import de.s42.dl.services.DLParameter;
import de.s42.dl.srv.FileRef;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Benjamin Schiller
 */
public class MethodDescriptor
{

	protected final Method method;

	protected final DLMethod dlMethod;

	protected final String name;

	protected final ParameterDescriptor[] parameters;

	protected final ParameterDescriptor[] staticParameters;

	public MethodDescriptor(Method method)
	{
		assert method != null;

		this.method = method;

		dlMethod = method.getAnnotation(DLMethod.class);

		if (dlMethod == null) {
			throw new RuntimeException("Method has to contain annotation DLMethod");
		}

		name = !dlMethod.value().isBlank() ? dlMethod.value() : method.getName();

		List<ParameterDescriptor> params = new ArrayList<>();
		List<ParameterDescriptor> staticParams = new ArrayList<>();

		for (Parameter parameter : method.getParameters()) {

			if (parameter.getAnnotation(DLParameter.class) == null) {
				throw new RuntimeException("All parameters in method " + method + " have to contain annotation DLParameter " + parameter);
			}

			ParameterDescriptor paramDesc = new ParameterDescriptor(parameter);

			params.add(paramDesc);

			if (paramDesc.isStatic()) {
				staticParams.add(paramDesc);
			}
		}

		parameters = params.toArray(ParameterDescriptor[]::new);
		staticParameters = staticParams.toArray(ParameterDescriptor[]::new);
	}

	public Method getMethod()
	{
		return method;
	}

	public String getName()
	{
		return name;
	}

	public boolean isUserLoggedIn()
	{
		return dlMethod.userLoggedIn();
	}

	public String[] getPermissions()
	{
		return dlMethod.permissions();
	}

	public ParameterDescriptor[] getParameters()
	{
		return parameters;
	}

	public ParameterDescriptor[] getStaticParameters()
	{
		return staticParameters;
	}

	public boolean hasParameterOfType(Class type)
	{
		for (ParameterDescriptor parameter : getStaticParameters()) {
			if (type.isAssignableFrom(parameter.getType())) {
				return true;
			}
		}

		return false;
	}
	
	public Class getReturnType()
	{
		return method.getReturnType();
	}

	public String getDescription()
	{
		// @todo add l10n description
		return "";
	}

	public boolean isNeedsMultiPartUpload()
	{
		return hasParameterOfType(FileRef.class);
	}

	public DLMethod getDlMethod()
	{
		return dlMethod;
	}
	
	public int getTtl()
	{
		return dlMethod.ttl();
	}
}
