// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2024 Studio 42 GmbH ( https://www.s42m.de ).
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
package de.s42.dl.services.token;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Benjamin Schiller
 */
public class BaseToken implements Token
{

	protected String token;

	protected long until;

	protected final Set<String> permissions;

	public BaseToken()
	{
		permissions = new HashSet<>();
		until = -1;
	}

	public BaseToken(String token, long until, Set<String> permissions)
	{
		assert token != null : "token != null";
		assert until > 0 : "until > 0";
		assert permissions != null : "permissions != null";

		this.permissions = new HashSet<>(permissions);
		this.token = token;
		this.until = until;
	}

	@Override
	public boolean permits(String permission)
	{
		assert permission != null : "permission != null";

		return this.permissions.contains(permission);
	}

	@Override
	public boolean permits(Set<String> permissions)
	{
		assert permissions != null : "permissions != null";

		return this.permissions.containsAll(permissions);
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	@Override
	public Set<String> getPermissions()
	{
		return Collections.unmodifiableSet(permissions);
	}

	public void setPermissions(Set<String> permissions)
	{
		assert permissions != null : "permissions != null";

		this.permissions.clear();
		this.permissions.addAll(permissions);
	}

	@Override
	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		assert token != null : "token != null";

		this.token = token;
	}

	@Override
	public long getUntil()
	{
		return until;
	}

	public void setUntil(long until)
	{
		assert until > 0 : "until > 0";

		this.until = until;
	}
	//</editor-fold>
}
