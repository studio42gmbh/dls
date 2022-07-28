// <editor-fold desc="The Jenomics License" defaultstate="collapsed">
/*
 * Copyright Jenomics GmbH 2022. All rights reserved.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For details to the License read https://www.jenomics.de/license
 */
//</editor-fold>
package de.s42.dl.services.database;

import de.s42.dl.services.remote.InvalidParameter;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <EntityType>
 * @param <FilterType>
 */
public class FindEntities<EntityType, FilterType> extends AbstractStatement
{

	public static final int DEFAULT_LIMIT = 1000;

	public enum MatchType
	{
		like("LIKE"),
		notLike("NOT LIKE"),
		similar("SIMILAR TO"),
		notSimilar("NOT SIMILAR TO"),
		regex("~"),
		iLike("ILIKE"),
		notILike("NOT ILIKE"),
		less("<"),
		lessEqual("<="),
		greater(">"),
		greaterEqual(">="),
		equal("="),
		notEqual("!=");

		public final String clause;

		MatchType(String clause)
		{
			this.clause = clause;
		}
	}

	private final static Logger log = LogManager.getLogger(FindEntities.class.getName());

	protected final Supplier<EntityType> factory;

	protected final boolean limit;

	protected final MatchType match;

	protected final String orderExpression;

	protected final boolean ascending;

	public FindEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String columnName, String name) throws Exception
	{
		this(databaseService, factory, tableName, columnName, name, MatchType.iLike, false, null, true);
	}

	public FindEntities(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String columnName, String name, MatchType match, boolean limit, String orderExpression, boolean ascending) throws Exception
	{
		assert databaseService != null;
		assert factory != null;
		assert name != null;
		assert tableName != null;
		assert columnName != null;
		assert match != null;

		this.databaseService = databaseService;
		this.factory = factory;
		this.name = name;
		this.limit = limit;
		this.match = match;
		this.orderExpression = orderExpression;
		this.ascending = ascending;

		initStatement(tableName, columnName);
	}

	private void initStatement(String tableName, String columnName) throws InvalidParameter
	{

		if (hasLimit() && !hasOrderExpression()) {
			throw new InvalidParameter("If the statement has a limit it needs an order expression");
		}

		String limitClause = (hasLimit()) ? " LIMIT ? OFFSET ?" : "";
		String orderByClause = (hasOrderExpression()) ? " ORDER BY " + getOrderExpression() + ((isAscending()) ? " ASC" : " DESC") : "";

		this.statement = "SELECT * FROM " + tableName + " WHERE " + columnName + " " + getMatch().clause + " ?" + orderByClause + limitClause + ";";
	}

	public List<EntityType> execute(FilterType filter) throws Exception
	{
		return execute(filter, 0, DEFAULT_LIMIT);
	}

	public List<EntityType> execute(FilterType filter, int offset, int limitCount) throws Exception
	{
		log.debug("execute", getName());

		assertRequired("filter", filter);

		if (hasLimit()) {
			return this.executeQueryManyEntities(getFactory(), filter, limitCount, offset);
		} else {
			return this.executeQueryManyEntities(getFactory(), filter);
		}

	}

	public boolean hasLimit()
	{
		return limit;
	}

	public MatchType getMatch()
	{
		return match;
	}

	public Supplier<EntityType> getFactory()
	{
		return factory;
	}

	public boolean hasOrderExpression()
	{
		return orderExpression != null;
	}

	public String getOrderExpression()
	{
		return orderExpression;
	}

	public boolean isAscending()
	{
		return ascending;
	}
}
