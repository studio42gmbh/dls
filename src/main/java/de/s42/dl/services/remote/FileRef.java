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
package de.s42.dl.services.remote;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Benjamin Schiller
 */
public class FileRef
{

	private String originalName;
	private String filePath;
	private String mimeType;
	private final Map<String, Object> attributes = Collections.synchronizedMap(new HashMap());
	protected boolean moveFile;

	public FileRef()
	{
	}

	public FileRef(Path filePath, String mimeType)
	{
		this(filePath, mimeType, null, null, false);
	}
	
	public FileRef(String filePath, String mimeType)
	{
		this(filePath, mimeType, null, null, false);
	}

	public FileRef(Path filePath, String mimeType, Map<String, Object> attributes)
	{
		this(filePath, mimeType, null, attributes, false);
	}
	
	public FileRef(String filePath, String mimeType, Map<String, Object> attributes)
	{
		this(filePath, mimeType, null, attributes, false);
	}

	public FileRef(String filePath, String mimeType, String originalName, Map<String, Object> attributes, boolean moveFile)
	{
		assert filePath != null;
		
		this.moveFile = moveFile;
		this.filePath = filePath;
		this.mimeType = mimeType;
		this.originalName = originalName;
		if (attributes != null) {
			this.attributes.putAll(attributes);
		}
	}
	
	public FileRef(Path filePath, String mimeType, String originalName, Map<String, Object> attributes, boolean moveFile)
	{
		assert filePath != null;
		
		this.moveFile = moveFile;
		this.filePath = filePath.toAbsolutePath().toString();
		this.mimeType = mimeType;
		this.originalName = originalName;
		if (attributes != null) {
			this.attributes.putAll(attributes);
		}
	}

	public String getOriginalEnding()
	{
		String o = getOriginalName();
		return o.substring(o.lastIndexOf(".") + 1);
	}

	public Path toPath()
	{
		return Path.of(getFilePath());
	}

	public File toFile()
	{
		return new File(getFilePath());
	}

	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public Map<String, Object> getAttributes()
	{
		return Collections.unmodifiableMap(attributes);
	}

	public void setAttributes(Map<String, Object> attributes)
	{
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}

	public String getOriginalName()
	{
		return originalName;
	}

	public void setOriginalName(String originalName)
	{
		this.originalName = originalName;
	}

	public boolean isMoveFile()
	{
		return moveFile;
	}

	public void setMoveFile(boolean moveFile)
	{
		this.moveFile = moveFile;
	}
}
