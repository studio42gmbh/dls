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

import de.s42.base.arrays.ArrayHelper;
import de.s42.base.sql.SQLHelper;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author Benjamin Schiller
 * @param <EntityType>
 */
public class UpdateEntity<EntityType> extends AbstractStatement
{

	private final static Logger log = LogManager.getLogger(UpdateEntity.class.getName());

	protected final Supplier<EntityType> factory;

	public UpdateEntity(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String name, String matchColumn, List<DBParameter> parameters) throws Exception
	{
		assert databaseService != null;
		assert factory != null;
		assert name != null;
		assert tableName != null;
		assert parameters != null;

		this.databaseService = databaseService;
		this.factory = factory;
		this.name = name;

		initStatement(tableName, matchColumn, parameters);
	}

	private void initStatement(String tableName, String matchColumn, List<DBParameter> parameters)
	{
		String columns = parameters.stream().map((param) -> {
			return param.getSQLName();
		}).collect(Collectors.joining(", "));
		String values = parameters.stream().map((param) -> {
			return param.getSQLValue();
		}).collect(Collectors.joining(", "));

		this.statement = "UPDATE " + tableName + " SET (" + columns + ") =\n(" + values + ") WHERE " + matchColumn + " = ?;";
	}

	public Optional<EntityType> execute(Object match, Object... parameters) throws Exception
	{
		log.debug("execute", getName());

		Object[] params = ArrayHelper.concatenate(parameters, new Object[]{match});

		try {
			return this.executeQuerySingleOrNoEntity(factory.get(), params);
		} catch (SQLException ex) {
			// Handle unqie violation to be a special error messaged
			if (SQLHelper.isUniquenessViolated(ex)) {
				throw new UniqueViolation(ex.getMessage(), ex);
			}
			throw ex;
		}
	}
}
