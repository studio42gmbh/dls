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

import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <EntityType>
 */
public class DropTables<EntityType> extends ExecuteStatement
{

	protected final Supplier<EntityType> factory;

	public DropTables(DatabaseService databaseService, Supplier<EntityType> factory, String tableName, String name) throws Exception
	{
		super(databaseService);
		
		assert factory != null;
		assert name != null;
		assert tableName != null;

		this.factory = factory;
		this.name = name;

		initStatement(tableName);
	}

	private void initStatement(String tableName)
	{
		this.statement = "DROP TABLE IF EXISTS " + tableName + ";";
	}
}
