// <editor-fold desc="The GCP License" defaultstate="collapsed">
/*
 * Copyright Golding Capital Partners GmbH 2022. All rights reserved.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For details to the License read https://www.goldingcapital.com/license
 */
//</editor-fold>
package de.s42.dl.srv;

import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 *
 * @author Benjamin Schiller
 */
@SuppressWarnings("deprecation")
public class SimpleHttpSession implements HttpSession
{

	protected long creationTime = (new Date()).getTime();
	protected long lastAccessedTime = (new Date()).getTime();
	protected UUID id = UUID.randomUUID();
	protected Map<String, Object> attributes = Collections.synchronizedMap(new HashMap<>());
	protected Map<String, Object> values = Collections.synchronizedMap(new HashMap<>());

	@Override
	public long getCreationTime()
	{
		return creationTime;
	}

	@Override
	public String getId()
	{
		return id.toString();
	}

	@Override
	public long getLastAccessedTime()
	{
		return lastAccessedTime;
	}

	@Override
	public ServletContext getServletContext()
	{
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int i)
	{
	}

	@Override
	public int getMaxInactiveInterval()
	{
		return 1;
	}

	@Override
	public HttpSessionContext getSessionContext()
	{
		return null;
	}

	@Override
	public Object getAttribute(String string)
	{
		lastAccessedTime = (new Date()).getTime();
		return attributes.get(string);
	}

	@Override
	public Object getValue(String string)
	{
		lastAccessedTime = (new Date()).getTime();
		return attributes.get(string);
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		lastAccessedTime = (new Date()).getTime();
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String[] getValueNames()
	{
		lastAccessedTime = (new Date()).getTime();
		return values.keySet().toArray(new String[0]);
	}

	@Override
	public void setAttribute(String string, Object o)
	{
		lastAccessedTime = (new Date()).getTime();
		attributes.put(string, o);
	}

	@Override
	public void putValue(String string, Object o)
	{
		lastAccessedTime = (new Date()).getTime();
		values.put(string, o);
	}

	@Override
	public void removeAttribute(String string)
	{
		lastAccessedTime = (new Date()).getTime();
		attributes.remove(string);
	}

	@Override
	public void removeValue(String string)
	{
		lastAccessedTime = (new Date()).getTime();
		values.remove(string);
	}

	@Override
	public void invalidate()
	{
		attributes.clear();
		values.clear();
		creationTime = (new Date()).getTime();
		id = UUID.randomUUID();
	}

	@Override
	public boolean isNew()
	{
		return (attributes.isEmpty() && values.isEmpty());
	}
}
