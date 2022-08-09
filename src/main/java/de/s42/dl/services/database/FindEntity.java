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

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <EntityType>
 */
public class FindEntity<EntityType, IdType> extends AbstractStatement<EntityType>
{

	private final static Logger log = LogManager.getLogger(FindEntity.class.getName());

	protected final Supplier<EntityType> factory;

	public FindEntity(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String columnName, String name) throws Exception
	{
		assert databaseService != null;
		assert factory != null;
		assert name != null;
		assert tableName != null;
		assert columnName != null;

		this.databaseService = databaseService;
		this.factory = factory;
		this.name = name;

		initStatement(tableName, columnName);
	}

	private void initStatement(String tableName, String columnName)
	{
		this.statement = "SELECT * FROM " + tableName + " WHERE " + columnName + " = ? LIMIT 1;";
	}

	public Optional<EntityType> execute(IdType id) throws Exception
	{
		log.debug("execute", getName());

		assertRequired("id", id);

		return this.executeQuerySingleOrNoEntity(factory, id);
	}
}
