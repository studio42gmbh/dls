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

import de.s42.dl.services.DLParameter;
import de.s42.dl.services.DLParameter.Validation;
import de.s42.dl.services.l10n.LocalizationService;
import java.lang.reflect.Parameter;

/**
 *
 * @author Benjamin Schiller
 */
public class ParameterDescriptor
{

	protected final ServiceDescriptor service;

	protected final MethodDescriptor method;

	protected final Parameter parameter;

	protected final DLParameter dlParameter;

	protected final LocalizationService localizationService;

	protected final String name;

	protected final boolean isStatic;

	public ParameterDescriptor(ServiceDescriptor service, MethodDescriptor method, Parameter parameter, LocalizationService localizationService)
	{
		assert service != null;
		assert method != null;
		assert parameter != null;

		this.service = service;
		this.method = method;
		this.parameter = parameter;
		this.localizationService = localizationService;

		dlParameter = parameter.getAnnotation(DLParameter.class);

		if (dlParameter == null) {
			throw new RuntimeException("Parameter has to contain annotation DLParameter");
		}

		name = dlParameter.value();

		isStatic = !name.startsWith("$");
	}

	public boolean isStatic()
	{
		return isStatic;
	}

	public Parameter getParameter()
	{
		return parameter;
	}

	public DLParameter getDlParameter()
	{
		return dlParameter;
	}

	public String getName()
	{
		return name;
	}

	public int getMaxLength()
	{
		return dlParameter.maxLength();
	}

	public Object getDefaultValue()
	{
		return dlParameter.defaultValue();
	}

	public boolean isRequired()
	{
		return dlParameter.required();
	}

	public Validation getValidation()
	{
		return dlParameter.validation();
	}

	public String getPattern()
	{
		return dlParameter.pattern();
	}

	public Class getType()
	{
		return parameter.getType();
	}

	public String getDescription()
	{
		if (localizationService == null) {
			return "";
		}

		return localizationService.localize(getService().getName() + "." + getMethod().getName() + "." + getName() + ".description");
	}

	public ServiceDescriptor getService()
	{
		return service;
	}

	public MethodDescriptor getMethod()
	{
		return method;
	}

	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}
}
