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

import de.s42.base.resources.ResourceHelper;
import de.s42.dlt.DLT;
import de.s42.dlt.parser.CompiledTemplate;
import de.s42.dlt.parser.TemplateContext;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author Benjamin Schiller
 * @param <ResultType>
 */
public class DynamicStatement<ResultType> extends AbstractStatement<ResultType>
{

	private final static Logger log = LogManager.getLogger(DynamicStatement.class.getName());

	protected CompiledTemplate template;
	protected Supplier<ResultType> factory;

	public DynamicStatement(DatabaseService databaseService, Supplier<ResultType> factory, String templateResourceName) throws Exception
	{
		super();

		this.databaseService = databaseService;
		this.factory = factory;		

		template = DLT.compile(ResourceHelper.getResourceAsString(templateResourceName).orElseThrow());
	}

	public List<ResultType> executeMany(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		setStatement(template.evaluate(context));
		
		log.debug("executeMany", getStatement());		

		return executeQueryManyEntities(factory, parameters);
	}
	
	public Optional<ResultType> executeOneOrNone(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		setStatement(template.evaluate(context));
		
		log.debug("executeOneOrNone", getStatement());		

		return executeQuerySingleOrNoEntity(factory, parameters);
	}	
	
	public ResultType executeOne(TemplateContext context, Object... parameters) throws Exception
	{
		// @todo ATTTENTION: THIS is NOT threadsafe ATM
		setStatement(template.evaluate(context));
		
		log.debug("executeOne", getStatement());		

		return executeQuerySingleEntity(factory, parameters);
	}	
}
