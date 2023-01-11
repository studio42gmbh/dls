// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2023 Studio 42 GmbH ( https://www.s42m.de ).
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
package de.s42.dl.services;

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Benjamin Schiller
 * @param <ResultType>
 * @param <ErrorType>
 */
public final class ServiceResult<ResultType, ErrorType>
{

	private final static Logger log = LogManager.getLogger(ServiceResult.class.getName());

	public static <ResultType, ErrorType> ServiceResult<ResultType, ErrorType> of(ResultType result)
	{
		if (result == null) {
			throw new NullPointerException("Result may not be null");
		}

		return new ServiceResult<>(result, null);
	}

	public static <ResultType, ErrorType> ServiceResult<ResultType, ErrorType> ofNullable(ResultType result)
	{
		return new ServiceResult<>(result, null);
	}

	public static <ResultType, ErrorType> ServiceResult<ResultType, ErrorType> ofError(ErrorType error)
	{
		if (error == null) {
			throw new NullPointerException("Error may not be null");
		}

		return new ServiceResult<>(null, error);
	}

	protected final ResultType result;
	protected final ErrorType error;

	protected ServiceResult(ResultType result, ErrorType error)
	{
		this.result = result;
		this.error = error;
	}

	public void ifResult(Consumer<ResultType> consumer)
	{
		assert consumer != null;

		if (isResult()) {
			consumer.accept(result);
		}
	}

	public void ifError(Consumer<ErrorType> consumer)
	{
		assert consumer != null;

		if (isResult()) {
			consumer.accept(error);
		}
	}

	public void ifResultOrElse(Consumer<ResultType> consumer, Consumer<ErrorType> errorHandler)
	{
		assert consumer != null;
		assert errorHandler != null;

		if (isResult()) {
			consumer.accept(result);
		} else {
			errorHandler.accept(error);
		}
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public boolean isResult()
	{
		return error == null;
	}

	public boolean isError()
	{
		return error != null;
	}

	public ResultType getResult()
	{
		if (isError()) {
			throw new AssertionError("Result is an Error");
		}

		return result;
	}

	public Optional<ResultType> getResultIfPresent()
	{
		return Optional.ofNullable(result);
	}

	public ErrorType getError()
	{
		if (isResult()) {
			throw new AssertionError("Result is not an Error");
		}

		return error;
	}

	public Optional<ErrorType> getErrorIfPresent()
	{
		return Optional.ofNullable(error);
	}
	//</editor-fold>
}
