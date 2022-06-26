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
package de.s42.dl.srv;

import de.s42.base.files.FilesHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Benjamin Schiller
 */
public class FileResult implements StreamResult
{

	protected final boolean inline;
	protected final int ttl;
	protected final String mimeType;
	protected final String fileName;
	protected final String fileEnding;
	protected final String encoding;
	protected final Path file;

	public FileResult(Path file, int ttl) throws IOException
	{
		this(file, ttl, true);
	}

	public FileResult(Path file, int ttl, boolean inline) throws IOException
	{
		assert file != null;
		assert ttl >= 0;

		if (!Files.isRegularFile(file)) {
			throw new IOException("File '" + file + "' does not exist");
		}

		this.file = file;
		this.ttl = ttl;
		this.inline = inline;

		fileName = file.getFileName().toString();

		// File ending
		int lastDot = fileName.lastIndexOf(".");

		if (lastDot > -1) {
			fileEnding = fileName.substring(lastDot + 1).toLowerCase();
		} else {
			fileEnding = "";
		}

		mimeType = FilesHelper.getMimeType(file);

		if (mimeType.startsWith("text")) {
			this.encoding = "UTF-8";
		} else {
			this.encoding = null;
		}
	}

	public FileResult(Path file, int ttl, boolean inline, String fileName) throws IOException
	{
		assert file != null;
		assert ttl >= 0;

		if (!Files.isRegularFile(file)) {
			throw new IOException("File '" + file + "' does not exist");
		}

		this.file = file;
		this.ttl = ttl;
		this.inline = inline;

		this.fileName = fileName;

		// File ending
		int lastDot = fileName.lastIndexOf(".");

		if (lastDot > -1) {
			fileEnding = fileName.substring(lastDot + 1).toLowerCase();
		} else {
			fileEnding = "";
		}

		mimeType = FilesHelper.getMimeType(file);

		if (mimeType.startsWith("text")) {
			this.encoding = "UTF-8";
		} else {
			this.encoding = null;
		}
	}

	public FileResult(Path file, int ttl, boolean inline, String fileName, String mimeType) throws IOException
	{
		assert file != null;
		assert ttl >= 0;

		if (!Files.isRegularFile(file)) {
			throw new IOException("File '" + file + "' does not exist");
		}

		this.file = file;
		this.ttl = ttl;
		this.inline = inline;

		this.fileName = fileName;

		// File ending
		int lastDot = fileName.lastIndexOf(".");

		if (lastDot > -1) {
			fileEnding = fileName.substring(lastDot + 1).toLowerCase();
		} else {
			fileEnding = "";
		}

		this.mimeType = mimeType;

		if (mimeType.startsWith("text")) {
			this.encoding = "UTF-8";
		} else {
			this.encoding = null;
		}
	}

	@Override
	public int getTtl()
	{
		return ttl;
	}

	@Override
	public String getMimeType()
	{
		return mimeType;
	}

	@Override
	public String getEncoding()
	{
		return encoding;
	}

	@Override
	public String getFileName()
	{
		return fileName;
	}

	@Override
	public boolean isInline()
	{
		return inline;
	}

	@Override
	public long stream(OutputStream out) throws IOException
	{
		return Files.copy(file, out);
	}
}
