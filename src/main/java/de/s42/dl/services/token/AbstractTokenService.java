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

import de.s42.dl.services.AbstractService;
import de.s42.dl.services.DLService;
import de.s42.dl.services.permission.InvalidCredentials;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Benjamin Schiller
 */
@DLService
public abstract class AbstractTokenService extends AbstractService implements TokenService
{

	private final static Logger log = LogManager.getLogger(AbstractTokenService.class.getName());

	protected abstract Optional<Token> resolveToken(String token);

	protected abstract boolean invalidateToken(String token);

	@Override
	public void validate(HttpServletRequest request, boolean userLoggedIn, Set<String> permissions) throws Exception
	{
		assert request != null : "request != null";
		assert permissions != null : "permissions != null";

		log.debug("validate", userLoggedIn, permissions);

		// Nothing to validate if not userLoggedIn and permissions are empty
		if (!userLoggedIn && permissions.isEmpty()) {
			return;
		}

		Optional<Token> optToken = getToken(request);

		if (optToken.isEmpty()) {
			throw new InvalidCredentials("Invalid token");
		}

		Token token = optToken.orElseThrow();

		if (!token.permits(permissions)) {
			throw new InvalidCredentials("Invalid token");
		}
	}

	@Override
	public Optional<Token> getToken(HttpServletRequest request)
	{
		assert request != null : "request != null";

		log.debug("getToken");

		// Get auth from header
		String auth = request.getHeader("Authorization");

		// Missing correct prefix in auth
		if (auth == null || !auth.startsWith("Bearer ")) {
			return Optional.empty();
		}

		// Remove "Bearer " from auth -> token without prefix
		String tokenId = auth.substring(7);

		return resolveToken(tokenId);
	}

	@Override
	public Boolean invalidate(String token)
	{
		assert token != null : "token != null";

		log.debug("invalidate");

		// Invalidate token
		return invalidateToken(token);
	}
}
