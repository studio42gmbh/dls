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

import de.s42.dl.services.Service;
import de.s42.dl.services.permission.PermissionService;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides a lean token service for example to allow tokenbased permission handling in DLS.
 *
 * @author Benjamin Schiller
 */
public interface TokenService extends Service, PermissionService
{

	/**
	 * Invalidates the token if possible or returns false
	 *
	 * @param token
	 *
	 * @return true if the token was invalidated, false if it could not get invalidated
	 */
	Boolean invalidate(String token);

	/**
	 * Is the way for internal services to get token from a given request (i.e. Bearer token)
	 *
	 * @param request
	 *
	 * @return
	 */
	Optional<Token> getToken(HttpServletRequest request);
}
