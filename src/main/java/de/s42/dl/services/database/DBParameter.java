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
package de.s42.dl.services.database;

import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Benjamin Schiller
 */
public class DBParameter
{

	protected String name;
	protected Class type;
	protected String customSQLValue;
	protected Object defaultValue;

	public DBParameter()
	{
	}

	public DBParameter(String name, Class type)
	{
		this.name = name;
		this.type = type;
	}

	public DBParameter(String name, Class type, Object defaultValue)
	{
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public DBParameter(String name, Class type, Object defaultValue, String customSQLValue)
	{
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.customSQLValue = customSQLValue;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Class getType()
	{
		return type;
	}

	public void setType(Class type)
	{
		this.type = type;
	}

	public String getSQLName()
	{
		return name.toLowerCase();
	}

	public String getSQLValue()
	{
		if (customSQLValue != null) {
			return customSQLValue;
		}

		if (UUID.class.isAssignableFrom(type)) {
			return "uuid(?::text)";
		} else if (Map.class.isAssignableFrom(type)) {
			return "jsonb(?::text)";
		} else if (String.class.isAssignableFrom(type)) {
			return "?::text";
		}

		return "?";
	}

	public String getCustomSQLValue()
	{
		return customSQLValue;
	}

	public void setCustomSQLValue(String customSQLValue)
	{
		this.customSQLValue = customSQLValue;
	}

	public Object getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue)
	{
		this.defaultValue = defaultValue;
	}
}
