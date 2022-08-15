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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

					if (parameter instanceof String) {
						statement.setString(c, (String) parameter);
					} else if (parameter instanceof Integer) {
						statement.setInt(c, (Integer) parameter);
					} else if (parameter instanceof Float) {
						statement.setFloat(c, (Float) parameter);
					} else if (parameter instanceof Boolean) {
						statement.setBoolean(c, (Boolean) parameter);
					} else if (parameter instanceof Long) {
						statement.setLong(c, (Long) parameter);
					} else if (parameter instanceof Double) {
						statement.setDouble(c, (Double) parameter);
						//} else if (parameter instanceof UUID) {
						//	statement.setObject(c, parameter);
					} else if (parameter instanceof Class) {
						statement.setString(c, ((Class) parameter).getName());
					} else if (parameter instanceof Date) {

						//@todo Is it possible to get rid of NULL_DATE to fix wrongmapping of dates ?
						if (((Date) parameter).getTime() == -1) {
							statement.setNull(c, java.sql.Types.DATE);
						} else {
							statement.setTimestamp(c, new java.sql.Timestamp(((Date) parameter).getTime()));
						}
					} else if (parameter instanceof Map) {
						statement.setString(c, (new JSONObject((Map) parameter)).toString());
					} else if (parameter instanceof Enum) {
						statement.setString(c, parameter.toString());
					} else {
						if (parameter != null) {
							//log.trace("SQL Parameter Data Type:", parameter.getClass().toString(), parameter.toString());
							//statement.setString(c, parameter.toString());
							statement.setObject(c, parameter);
						} //unknown parameter null - set null string
						else {
							statement.setNull(c, Types.NULL);
						}
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

		for (BeanProperty<ResultType> property : info.getWriteProperties()) {

			String propertyName = property.getName().toLowerCase();
			Class paramType = property.getPropertyClass();

			try {

				// This is a workaround for supporting different class loaders in J2EE scenario
				Object value = result.getObject(propertyName);
				if (value != null && "org.postgresql.util.PGobject".equals(value.getClass().getName())) {
					value = value.getClass().getMethod("getValue").invoke(value);
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
		//log.trace("Called executeNoResult");

		//log.startTimer(Log.Level.TRACE, "executeNoResult.durationDbCall");
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement);

			setParameters(stat, parameters);

			stat.execute();
			//getDatabaseService().incrementAndGetDbCalls();

			//stat.close();

			/*if (getDatabaseService().isAutoCloseConnection()) {
				con.close();
			}*/
			//log.stopTimer(Log.Level.TRACE, "executeNoResult.durationDbCall", "DB Call duration");
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
						con.close();
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
		//log.trace("Called executeQuerySingleEntity");

		//log.startTimer(Log.Level.TRACE, "executeQuerySingleEntity.durationDbCall");
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS);

			setParameters(stat, parameters);

			stat.execute();
			//getDatabaseService().incrementAndGetDbCalls();

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

			//stat.close();

			/*if (getDatabaseService().isAutoCloseConnection()) {
				con.close();
			}*/
			//log.stopTimer(Log.Level.TRACE, "executeQuerySingleEntity.durationDbCall", "DB Call duration");
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
						con.close();
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
		//log.trace("Called executeQueryManyEntities");

		//log.startTimer(Log.Level.TRACE, "executeQuerySingleEntity.durationDbCall");
		Connection con = null;
		PreparedStatement stat = null;
		try {
			con = getDatabaseService().getConnection();

			stat = con.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS);

			setParameters(stat, parameters);

			stat.execute();
			//getDatabaseService().incrementAndGetDbCalls();

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

			//stat.close();

			/*if (getDatabaseService().isAutoCloseConnection()) {
				con.close();
			}*/
			//log.stopTimer(Log.Level.TRACE, "executeQuerySingleEntity.durationDbCall", "DB Call duration");
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
						con.close();
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

		return new Date(r.getTime());
	}

	protected Date getAsDate(ResultSet res, String columnName) throws SQLException
	{
		Timestamp r = res.getTimestamp(columnName);

		if (r == null) {
			return null;
		}

		return new Date(r.getTime());
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
