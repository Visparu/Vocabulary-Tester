package com.visparu.vocabularytrial.model.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import com.visparu.vocabularytrial.model.db.Queries;
import com.visparu.vocabularytrial.model.db.VPS;

public final class Language
{
	private static final Map<String, Language>	cache	= new HashMap<>();
	private String								language_code;
	private String								name;
	
	private Language(final String language_code, final String name)
	{
		this.language_code	= language_code;
		this.name			= name;
		
		LogItem.debug("Initialized new language '" + name + "'");
	}
	
	@Override
	public final String toString()
	{
		return this.name;
	}
	
	public final static void createTable()
	{
		String query = "CREATE TABLE IF NOT EXISTS language (" + "language_code VARCHAR(2) PRIMARY KEY, " + "name VARCHAR(30)" + ")";
		
		VPS.execute(query);
		
		LogItem.debug("Language table created");
	}
	
	public final static void clearCache()
	{
		Language.cache.clear();
		
		LogItem.debug("Cleared language cache");
	}
	
	public final static Language get(final String language_code)
	{
		if (Language.cache.containsKey(language_code))
		{
			Language l = Language.cache.get(language_code);
			return l;
		}
		Language l = Language.readEntity(language_code);
		return l;
	}
	
	public final static List<Language> getAll()
	{
		List<Language> languages = Queries.queryAllLanguages();
		return languages;
	}
	
	public final static Language createLanguage(final String language_code, final String name)
	{
		if (Language.get(language_code) != null)
		{
			throw new IllegalArgumentException("Language with language_code '" + language_code + "' already exists!");
		}
		Language l = Language.get(language_code);
		if (l == null)
		{
			l = new Language(language_code, name);
			Language.writeEntity(l);
			Language.cache.put(language_code, l);
		}
		LanguageComponent.repopulateAllLanguages();
		return l;
	}
	
	public final static void removeLanguage(final String language_code)
	{
		Language.cache.remove(language_code);
		final String query = "DELETE FROM language " + "WHERE language_code = ?";
		
		VPS.execute(query, language_code);
		
		LanguageComponent.repopulateAllLanguages();
		LogItem.debug("Language with code '" + language_code + "' removed");
	}
	
	public final static void removeAllLanguages()
	{
		String query = "DELETE FROM language";
		
		VPS.execute(query);
		Language.clearCache();
		LogItem.debug("All languages removed");
		
		LanguageComponent.repopulateAllLanguages();
	}
	
	private final static Language readEntity(final String language_code)
	{
		final String query = "SELECT * " + "FROM language " + "WHERE language_code = ?";
		
		try (final VPS vps = new VPS(query); ResultSet rs = vps.query(language_code))
		{
			if (rs.next())
			{
				final String	name	= rs.getString("name");
				final Language	l		= new Language(language_code, name);
				Language.cache.put(language_code, l);
				
				return l;
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private final static void writeEntity(final Language language)
	{
		final String query = "INSERT INTO language " + "VALUES(?, ?)";
		
		final String	language_code	= language.getLanguage_code();
		final String	name			= language.getName();
		
		VPS.execute(query, language_code, name);
		
		LogItem.debug("Inserted new language entity " + language.getName());
	}
	
	public final String getLanguage_code()
	{
		return this.language_code;
	}
	
	public final void setLanguage_code(final String language_code)
	{
		final String query = "UPDATE language " + "SET language_code = ? " + "WHERE language_code = ?";
		
		VPS.execute(query, language_code, this.language_code);
		Language.cache.remove(this.language_code);
		Language.cache.put(language_code, this);
		this.language_code = language_code;
		
		LogItem.debug("Updated language_code for language " + this.getName());
	}
	
	public final String getName()
	{
		return this.name;
	}
	
	public final void setName(final String name)
	{
		final String query = "UPDATE language " + "SET name = ? " + "WHERE language_code = ?";
		
		VPS.execute(query, name, this.language_code);
		this.name = name;
		
		LogItem.debug("Updated name for language " + this.getName());
	}
	
	public final List<Word> getWords()
	{
		final String query = "SELECT * " + "FROM word " + "WHERE language_code = ? " + "ORDER BY word.name";
		
		try (final VPS vps = new VPS(query); ResultSet rs = vps.query(this.language_code))
		{
			final List<Word> words = new ArrayList<>();
			while (rs.next())
			{
				final String	word_name	= rs.getString("name");
				final Word		word		= Word.get(word_name, this);
				if (word != null)
				{
					words.add(word);
				}
			}
			return words;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
}
