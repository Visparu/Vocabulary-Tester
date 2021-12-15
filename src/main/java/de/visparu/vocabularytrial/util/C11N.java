package de.visparu.vocabularytrial.util;

import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.log.Severity;

public final class C11N
{
	private static final String		DEFAULT_CONFIG_FILE		= "config.json";
	private static final String		DEFAULT_DRIVER			= "jdbc";
	private static final String		DEFAULT_PROTOCOL		= "sqlite";
	private static final String		DEFAULT_FILENAME		= IOUtil.DATA_PATH + "temp.db";
	private static final String		DEFAULT_SEPARATORS		= ",/;";
	private static final Severity	DEFAULT_LOGGING_LEVEL	= Severity.INFO;
	
	public static final String getDriver()
	{
		final String driver = C11N.getValue("driver");
		if (driver == null)
		{
			return C11N.DEFAULT_DRIVER;
		}
		return driver;
	}
	
	public static final void setDriver(String driver)
	{
		C11N.setValue("driver", driver);
		LogItem.debug("Database driver changed to " + driver);
	}
	
	public static final String getProtocol()
	{
		final String protocol = C11N.getValue("protocol");
		if (protocol == null)
		{
			return C11N.DEFAULT_PROTOCOL;
		}
		return protocol;
	}
	
	public static final void setProtocol(String protocol)
	{
		C11N.setValue("protocol", protocol);
		LogItem.debug("Database protocol changed to " + protocol);
	}
	
	public static final File getDatabasePath()
	{
		final String dbPath = C11N.getValue("dbPath");
		if (dbPath == null)
		{
			File f = Paths.get(C11N.DEFAULT_FILENAME).toFile();
			return f;
		}
		File f = Paths.get(dbPath).toFile();
		return f;
	}
	
	public static final void setDatabasePath(String databasePath)
	{
		C11N.setValue("dbPath", databasePath);
		LogItem.debug("Database path changed to " + databasePath);
	}
	
	public static final Locale getLocale()
	{
		final String localeString = C11N.getValue("locale");
		if (localeString == null)
		{
			Locale l = I18N.getDefaultLocale();
			return l;
		}
		Locale l = Locale.forLanguageTag(localeString);
		return l;
	}
	
	public static final void setLocale(Locale locale)
	{
		C11N.setValue("locale", locale.toLanguageTag());
		I18N.localeProperty().set(locale);
		LogItem.debug("Locale changed to " + locale.getDisplayName());
	}
	
	public static final String getSeparators()
	{
		final String separatorsString = C11N.getValue("separators");
		if (separatorsString == null)
		{
			return C11N.DEFAULT_SEPARATORS;
		}
		return separatorsString;
	}
	
	public static final void setSeparators(final String separators)
	{
		C11N.setValue("separators", separators);
		LogItem.debug("Separators changed to \"" + separators + "\"");
	}
	
	public static final Severity getLoggingLevel()
	{
		final String severityString = C11N.getValue("logging_level");
		if (severityString == null)
		{
			Severity s = C11N.DEFAULT_LOGGING_LEVEL;
			return s;
		}
		Severity s = Severity.valueOf(severityString);
		return s;
	}
	
	@SuppressWarnings("unchecked")
	private static final <V> V getValue(String key)
	{
		final String jsonString = IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		if (jsonString == null)
		{
			return null;
		}
		final JSONParser parser = new JSONParser();
		try
		{
			final JSONObject	obj		= (JSONObject) parser.parse(jsonString);
			final V				value	= (V) obj.get(key);
			return value;
		}
		catch (ParseException | ClassCastException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static final void setValue(Object key, Object value)
	{
		final String		jsonString	= IOUtil.readString(C11N.DEFAULT_CONFIG_FILE);
		final JSONObject	obj;
		if (jsonString == null)
		{
			obj = new JSONObject();
		}
		else
		{
			try
			{
				obj = (JSONObject) new JSONParser().parse(jsonString);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
				return;
			}
		}
		obj.put(key, value);
		IOUtil.writeString(obj.toJSONString(), C11N.DEFAULT_CONFIG_FILE);
	}
}
