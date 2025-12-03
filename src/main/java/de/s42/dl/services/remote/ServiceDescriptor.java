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

import de.s42.base.strings.StringHelper;
import de.s42.dl.services.DLMethod;
import de.s42.dl.services.DLService;
import de.s42.dl.services.Service;
import de.s42.dl.services.l10n.LocalizationService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Benjamin Schiller
 */
public class ServiceDescriptor implements Comparable<ServiceDescriptor>
{

	protected final Service service;

	protected final DLService dlService;

	protected final LocalizationService localizationService;

	protected final String name;
	protected final String description;
	protected final String className;

	protected final MethodDescriptor[] methods;
	protected final Map<String, MethodDescriptor> methodsByName;

	public ServiceDescriptor(Service service, LocalizationService localizationService)
	{
		assert service != null;

		this.service = service;
		this.localizationService = localizationService;

		dlService = service.getClass().getAnnotation(DLService.class);

		name = service.getName();
		description = dlService.description();
		className = service.getClass().getSimpleName() + StringHelper.upperCaseFirst(name);

		List<MethodDescriptor> meths = new ArrayList<>();

		for (Method method : service.getClass().getMethods()) {

			if (method.getAnnotation(DLMethod.class) != null) {
				meths.add(new MethodDescriptor(this, method, localizationService));
			}
		}

		// Quick iteration over methods
		methods = meths.toArray(MethodDescriptor[]::new);
		Arrays.sort(methods);

		// Lookup methods by name
		methodsByName = new HashMap<>();

		for (MethodDescriptor method : methods) {

			// Prevent double names
			if (methodsByName.containsKey(method.getName())) {
				throw new RuntimeException("Method with name '" + method.getName() + "' is already mapped in service '" + name + "'");
			}

			methodsByName.put(method.getName(), method);
		}

	}

	public Service getService()
	{
		return service;
	}

	public String getName()
	{
		return name;
	}

	public String getClassName()
	{
		return className;
	}

	public MethodDescriptor[] getMethods()
	{
		return methods;
	}

	public Optional<MethodDescriptor> getMethod(String name)
	{
		assert name != null;

		return Optional.ofNullable(methodsByName.get(name));
	}

	public String getDescription()
	{
		if (description != null && !description.isBlank()) {
			return description;
		}

		if (localizationService == null) {
			return "";
		}

		return localizationService.localize(getName() + ".description");
	}

	public boolean isUserLoggedIn()
	{
		return dlService.userLoggedIn();
	}

	public String[] getPermissions()
	{
		return dlService.permissions();
	}

	public DLService getDlService()
	{
		return dlService;
	}

	@Override
	public int compareTo(ServiceDescriptor o)
	{
		if (o == null) {
			return -1;
		}

		return getName().compareTo(o.getName());
	}

	public LocalizationService getLocalizationService()
	{
		return localizationService;
	}
}
