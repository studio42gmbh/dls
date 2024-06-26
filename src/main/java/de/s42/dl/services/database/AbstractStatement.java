// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2022 Studio 42 GmbH ( https://www.s42m.de ).
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
package de.s42.dl.services.database;

import de.s42.base.beans.BeanHelper;
import de.s42.base.beans.BeanInfo;
import de.s42.base.beans.BeanProperty;
import de.s42.base.conversion.ConversionHelper;
import de.s42.base.resources.ResourceHelper;
import de.s42.dl.DLAttribute.AttributeDL;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Benjamin Schiller
 * @param <ResultType>
 *
 * @todo AbstractStatement optimize garbage pressure
 */
public abstract class AbstractStatement<ResultType> implements Statement<ResultType>
{

	private final static Logger log = LogManager.getLogger(AbstractStatement.class.getName());

	protected String name;

	@AttributeDL(required = false)
	protected String statement;

	@AttributeDL(required = true)
	protected DatabaseService databaseService;

	public AbstractStatement()
	{

	}

	public AbstractStatement(DatabaseService databaseService) throws IOException
	{
		assert databaseService != null;

		this.databaseService = databaseService;
		name = getClass().getName();

		statement = ResourceHelper.getResourceAsString(getClass(), getClass().getSimpleName() + ".sql").orElseThrow();

		log.trace("Statement", statement);
	}

	public AbstractStatement(DatabaseService databaseService, String statementResource) throws IOException
	{
		assert databaseService != null;
		assert statementResource != null;

		this.databaseService = databaseService;
		name = statementResource;

		statement = ResourceHelper.getResourceAsString(statementResource).orElseThrow();

		log.trace("Statement", statement);
	}

	public AbstractStatement(DatabaseService databaseService, String statementResource, String name) throws IOException
	{
		assert databaseService != null;
		assert statementResource != null;

		this.databaseService = databaseService;
		this.name = name;

		statement = ResourceHelper.getResourceAsString(statementResource).orElseThrow();

		log.trace("Statement", statement);
	}

	protected void assertRequired(String name, Object value)
	{
		if (value == null) {
			throw new IllegalArgumentException("Argument " + name + " is required");
		}
	}

	protected void setParameters(PreparedStatement statement, Object... parameters) throws SQLException
	{
		if (parameters.length > 0) {
			int c = 1;
			for (Object parameter : parameters) {

				try {

					if (parameter instanceof String string) {
						statement.setString(c, string);
					} else if (parameter instanceof Integer integer) {
						statement.setInt(c, integer);
					} else if (parameter instanceof Float float1) {
						statement.setFloat(c, float1);
					} else if (parameter instanceof Boolean boolean1) {
						statement.setBoolean(c, boolean1);
					} else if (parameter instanceof Long long1) {
						statement.setLong(c, long1);
					} else if (parameter instanceof Double double1) {
						statement.setDouble(c, double1);
						//} else if (parameter instanceof UUID) {
						//	statement.setObject(c, parameter);
					} else if (parameter instanceof Class class1) {
						statement.setString(c, class1.getName());
					} else if (parameter instanceof Date date) {

						//@todo Is it possible to get rid of NULL_DATE to fix wrongmapping of dates ?
						if (date.getTime() == -1) {
							statement.setNull(c, java.sql.Types.DATE);
						} else {
							statement.setTimestamp(c, new java.sql.Timestamp(date.getTime()));
						}
					} else if (parameter instanceof Map map) {
						statement.setString(c, (new JSONObject(map)).toString());
					} else if (parameter instanceof Enum) {
						statement.setString(c, parameter.toString());
					} // Handle list and set types
					else if (parameter instanceof List
						|| parameter instanceof Set) {
						Array array = statement.getConnection().createArrayOf("text", ((Collection) parameter).toArray());
						statement.setArray(c, array);
					} // Handle array types
					else if (parameter != null && parameter.getClass().isArray()) {
						String sqlType = getSqlTypeFromClass(parameter.getClass().getComponentType());
						Array array = statement.getConnection().createArrayOf(sqlType, (Object[]) parameter);
						statement.setArray(c, array);
					} else if (parameter != null) {
						//log.trace("SQL Parameter Data Type:", parameter.getClass().toString(), parameter.toString());
						//statement.setString(c, parameter.toString());
						statement.setObject(c, parameter);
					} //unknown parameter null - set null string
					else {
						statement.setNull(c, Types.NULL);
					}

					c++;
				} catch (SQLException ex) {
					throw new SQLException("Error setting parameter " + c + " : " + parameter + " - " + ex.getMessage(), ex);
				}
			}
		}
	}

	// @todo AbstractStatement.mapCurrentRowToEntity optimize loading of objects with missing columns / properties
	protected <ResultType> ResultType mapCurrentRowToEntity(ResultSet result, ResultType fillTarget) throws Exception
	{
		// Using the Introspector has the advantage of being cached
		BeanInfo<ResultType> info = BeanHelper.getBeanInfo((Class<ResultType>) fillTarget.getClass());

		for (BeanProperty<ResultType, ?> property : info.getWriteProperties()) {

			String propertyName = property.getName().toLowerCase();
			Class paramType = property.getPropertyClass();

			try {

				// This is a workaround for supporting different class loaders in J2EE scenario
				Object value = result.getObject(propertyName);
				if (value != null && "org.postgresql.util.PGobject".equals(value.getClass().getName())) {
					value = value.getClass().getMethod("getValue").invoke(value);
				}

				// Handle implicit timestamp offset of local java system
				if (Date.class.isAssignableFrom(paramType) && value instanceof Timestamp) {
					LocalDateTime ldt = LocalDateTime.ofInstant(((Timestamp) value).toInstant(), ZoneId.systemDefault());
					value = Date.from(ldt.atZone(ZoneId.of("Etc/UTC")).toInstant());
				}

				property.write(fillTarget, ConversionHelper.convert(value, paramType));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new Exception("Error writing " + propertyName + " - " + ex.getMessage(), ex);
			} catch (SQLException ex2) {
				// Do nothing
				// log.warn("Missing result column for property " + propertyName);
			}
		}

		return fillTarget;
	}

	protected <ResultType> ResultType mapResultToSingleEntity(ResultSet result, ResultType fillTarget) throws Exception
	{
		return mapCurrentRowToEntity(result, fillTarget);
	}

	@Override
	public void executeNoResult(Object... parameters) throws Exception
	{
		executeNoResult(statement, parameters);
	}

	@Override
	public void executeNoResult(String statement, Object... parameters) throws Exception
	{
		log.trace("Called executeNoResult");

		log.start("executeNoResult.durationDbCall");

		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement);

			setParameters(stat, parameters);

			stat.execute();
			getDatabaseService().incrementAndGetDbCalls();

			log.stopTrace("executeNoResult.durationDbCall");

		} catch (SQLException ex) {
			throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}

			if (con != null) {
				try {
					if (getDatabaseService().isAutoCloseConnection()) {
						getDatabaseService().closeConnection(con);
					}
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public <ResultType> ResultType executeQuerySingleEntity(Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		return executeQuerySingleEntity(getStatement(), factory, parameters);
	}

	@Override
	public <ResultType> ResultType executeQuerySingleEntity(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		return executeQuerySingleOrNoEntity(statement, factory, parameters).orElseThrow();
	}

	@Override
	public <ResultType> Optional<ResultType> executeQuerySingleOrNoEntity(Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		return executeQuerySingleOrNoEntity(getStatement(), factory, parameters);
	}

	@Override
	public <ResultType> Optional<ResultType> executeQuerySingleOrNoEntity(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		log.trace("Called executeQuerySingleEntity");

		log.start("executeQuerySingleEntity.durationDbCall");

		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS);

			setParameters(stat, parameters);

			stat.execute();
			getDatabaseService().incrementAndGetDbCalls();

			ResultSet resultSet = stat.getResultSet();

			if (resultSet == null) {
				resultSet = stat.getGeneratedKeys();
			}

			ResultType entity = null;

			if (resultSet != null) {

				if (resultSet.next()) {
					entity = mapResultToSingleEntity(resultSet, factory.get());
				}
			}

			log.stopTrace("executeQuerySingleEntity.durationDbCall");
			return Optional.ofNullable(entity);

		} catch (SQLException ex) {
			throw new SQLException("Error in query " + getName() + " - " + ex.getMessage(), ex.getSQLState(), ex);
		} catch (Exception ex) {
			throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}

			if (con != null) {
				try {
					if (getDatabaseService().isAutoCloseConnection()) {
						getDatabaseService().closeConnection(con);
					}
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public <ResultType> List<ResultType> executeQueryManyEntities(Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		return executeQueryManyEntities(statement, factory, parameters);
	}

	@Override
	public <ResultType> List<ResultType> executeQueryManyEntities(String statement, Supplier<ResultType> factory, Object... parameters) throws Exception
	{
		log.trace("executeQueryManyEntities");

		log.start("executeQuerySingleEntity.durationDbCall");

		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS);

			setParameters(stat, parameters);

			stat.execute();
			getDatabaseService().incrementAndGetDbCalls();

			ResultSet resultSet = stat.getResultSet();

			if (resultSet == null) {
				resultSet = stat.getGeneratedKeys();
			}

			List<ResultType> entities = new ArrayList();

			if (resultSet != null) {

				while (resultSet.next()) {
					entities.add(mapResultToSingleEntity(resultSet, factory.get()));
				}
			}

			log.stopTrace("executeQuerySingleEntity.durationDbCall");

			return entities;

		} catch (Exception ex) {
			throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}

			if (con != null) {
				try {
					if (getDatabaseService().isAutoCloseConnection()) {
						getDatabaseService().closeConnection(con);
					}
				} catch (SQLException ex) {
					throw new Exception("Error in query " + getName() + " - " + ex.getMessage(), ex);
				}
			}
		}
	}

	protected UUID getAsUUID(ResultSet res, int columnIndex) throws SQLException
	{
		String r = res.getString(columnIndex);

		if (r == null) {
			return null;
		}

		return UUID.fromString(r);
	}

	protected UUID getAsUUID(ResultSet res, String columnName) throws SQLException
	{
		String r = res.getString(columnName);

		if (r == null) {
			return null;
		}

		return UUID.fromString(r);
	}

	protected Date getAsDate(ResultSet res, int columnIndex) throws SQLException
	{
		Timestamp r = res.getTimestamp(columnIndex);

		if (r == null) {
			return null;
		}

		// Handle implicit timestamp offset of local java system
		LocalDateTime ldt = LocalDateTime.ofInstant(r.toInstant(), ZoneId.systemDefault());
		return Date.from(ldt.atZone(ZoneId.of("Etc/UTC")).toInstant());		
	}

	protected Date getAsDate(ResultSet res, String columnName) throws SQLException
	{
		Timestamp r = res.getTimestamp(columnName);

		if (r == null) {
			return null;
		}

		// Handle implicit timestamp offset of local java system
		LocalDateTime ldt = LocalDateTime.ofInstant(r.toInstant(), ZoneId.systemDefault());
		return Date.from(ldt.atZone(ZoneId.of("Etc/UTC")).toInstant());		
	}

	protected JSONObject getAsJSON(ResultSet res, int columnIndex) throws SQLException, JSONException
	{
		String r = res.getString(columnIndex);

		if (r == null) {
			return null;
		}

		return new JSONObject(r);
	}

	protected JSONObject getAsJSON(ResultSet res, String columnName) throws SQLException, JSONException
	{
		String r = res.getString(columnName);

		if (r == null) {
			return null;
		}

		return new JSONObject(r);
	}

	/**
	 * See also https://docwiki.embarcadero.com/InterBase/2020/en/Java-to-SQL_Type_Conversion and
	 * https://www.instaclustr.com/blog/postgresql-data-types-mappings-to-sql-jdbc-and-java-data-types/ and
	 * https://stackoverflow.com/questions/50083516/jdbc-createarrayof-from-arraylist
	 *
	 * @param javaType
	 *
	 * @return
	 */
	protected String getSqlTypeFromClass(Class javaType)
	{
		if (UUID.class.equals(javaType)) {
			return "uuid";
		}

		if (String.class.equals(javaType)) {
			return "text";
		}

		if (int.class.equals(javaType)) {
			return "integer";
		}

		if (Integer.class.equals(javaType)) {
			return "integer";
		}

		if (float.class.equals(javaType)) {
			return "real";
		}

		if (Float.class.equals(javaType)) {
			return "real";
		}

		if (double.class.equals(javaType)) {
			return "double";
		}

		if (Double.class.equals(javaType)) {
			return "double";
		}

		if (short.class.equals(javaType)) {
			return "short";
		}

		if (Short.class.equals(javaType)) {
			return "short";
		}

		if (java.sql.Timestamp.class.equals(javaType)) {
			return "timestamp";
		}

		return "text";
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStatement()
	{
		return statement;
	}

	public void setStatement(String statement)
	{
		this.statement = statement;
	}

	public DatabaseService getDatabaseService()
	{
		return databaseService;
	}

	public void setDatabaseService(DatabaseService databaseService)
	{
		this.databaseService = databaseService;
	}
}
