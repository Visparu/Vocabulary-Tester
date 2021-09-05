package com.visparu.vocabularytrial.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

public final class IOUtil
{
	public static final String DATA_PATH = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Visparu" + File.separator + "VocabularyTrial" + File.separator;
	static
	{
		try
		{
			Files.createDirectories(Paths.get(IOUtil.DATA_PATH));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static final void writeString(String data, String file)
	{
		final String	absolutePathString	= IOUtil.DATA_PATH + file;
		final Path		absolutePath		= Paths.get(absolutePathString);
		try
		{
			Files.write(absolutePath, data.getBytes(Charset.defaultCharset()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			LogItem.debug("Wrote string to " + file, "Wrote the following string to " + file + ":\n\n" + data);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static final String readString(String file)
	{
		final String	absolutePathString	= IOUtil.DATA_PATH + file;
		final Path		absolutePath		= Paths.get(absolutePathString);
		try
		{
			final String data = new String(Files.readAllBytes(absolutePath), Charset.defaultCharset());
			return data;
		}
		catch (IOException e)
		{
			return null;
		}
	}
}
