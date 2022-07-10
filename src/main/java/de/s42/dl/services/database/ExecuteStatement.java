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

/**
 *
 * @author Benjamin Schiller
 */
public class ExecuteStatement extends AbstractStatement
{

	private final static Logger log = LogManager.getLogger(ExecuteStatement.class.getName());

	public ExecuteStatement(DatabaseService databaseService, String statementResource) throws Exception
	{
		super(databaseService, statementResource);
	}

	public void execute() throws Exception
	{
		log.debug("execute", getName());

		executeNoResult();
	}
}
