package de.visparu.vocabularytrial.model.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.visparu.vocabularytrial.model.db.VPS;

public final class Word
{
	private static final Map<Integer, Word>	cache	= new HashMap<>();
	private Integer							word_id;
	private String							name;
	private Language						language;
	
	private Word(final Integer word_id, final String name, final Language language)
	{
		this.word_id	= word_id;
		this.name		= name;
		this.language	= language;
		LogItem.debug("Initialized new word '" + name + "'");
	}
	
	public static final void createTable()
	{
		final String query = "CREATE TABLE IF NOT EXISTS word(" + "word_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name VARCHAR(100), " + "language_code VARCHAR(2), "
			+ "FOREIGN KEY(language_code) REFERENCES language(language_code) ON UPDATE CASCADE" + ")";
		
		VPS.execute(query);
		
		LogItem.debug("Word table created");
	}
	
	public static final void clearCache()
	{
		Word.cache.clear();
		
		LogItem.debug("Cleared word cache");
	}
	
	public static final Word get(final Integer word_id)
	{
		if (Word.cache.containsKey(word_id))
		{
			Word w = Word.cache.get(word_id);
			return w;
		}
		Word w = Word.readEntity(word_id);
		return w;
	}
	
	public static final Word get(final String name, final Language l)
	{
		Word w = Word.readEntity(name, l.getLanguage_code());
		return w;
	}
	
	public static final Word createWord(final String name, final Language l)
	{
		Word w = Word.get(name, l);
		if (w == null)
		{
			w = new Word(-1, name, l);
			final Integer word_id = Word.writeEntity(w);
			w.setWord_id(word_id);
			Word.cache.put(word_id, w);
		}
		return w;
	}
	
	public static final void removeWord(final Integer word_id)
	{
		final String query = "DELETE FROM word " + "WHERE word_id = ?";
		
		VPS.execute(query, word_id);
		Word.cache.remove(word_id);
		
		LogItem.debug("Word with id '" + word_id + "' removed");
	}
	
	public static final void removeWord(final String name, final Language l)
	{
		final String query = "DELETE FROM word " + "WHERE word_id = ?";
		
		Word word = Word.get(name, l);
		if (word == null)
		{
			LogItem.error("Word '" + name + "' doesn't exist!");
			return;
		}
		
		Integer word_id = word.getWord_id();
		
		VPS.execute(query, word_id);
		Word.cache.remove(Word.get(name, l).getWord_id());
		
		LogItem.debug("Word '" + name + "' removed");
	}
	
	public static final void removeAllWords()
	{
		String query = "DELETE FROM word";
		
		VPS.execute(query);
		Word.clearCache();
		
		LogItem.debug("All words removed");
	}
	
	private static final Word readEntity(Integer word_id)
	{
		final String query = "SELECT * " + "FROM word " + "WHERE word_id = ?";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(word_id))
		{
			if (rs.next())
			{
				final String	name			= rs.getString("name");
				final String	language_code	= rs.getString("language_code");
				final Language	l				= Language.get(language_code);
				final Word		w				= new Word(word_id, name, l);
				
				Word.cache.put(word_id, w);
				return w;
			}
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static final Word readEntity(final String name, final String language_code)
	{
		final String query = "SELECT * " + "FROM word " + "WHERE name = ? " + "AND language_code = ?";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(name, language_code))
		{
			if (rs.next())
			{
				final Integer	word_id	= rs.getInt("word_id");
				final Language	l		= Language.get(language_code);
				final Word		w		= new Word(word_id, name, l);
				
				Word.cache.put(word_id, w);
				return w;
			}
			return null;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static final Integer writeEntity(final Word word)
	{
		final String query = "INSERT INTO word(name, language_code) " + "VALUES(?, ?)";
		
		final String	name			= word.getName();
		final String	language_code	= word.getLanguage().getLanguage_code();
		
		final List<Integer> keys = VPS.execute(query, name, language_code);
		if (keys.isEmpty())
		{
			return -1;
		}
		final Integer word_id = keys.get(0);
		
		LogItem.debug("Inserted new word entity " + word.getName());
		
		return word_id;
	}
	
	public final Integer getWord_id()
	{
		return this.word_id;
	}
	
	private final void setWord_id(final Integer word_id)
	{
		this.word_id = word_id;
		LogItem.debug("Updated word_id for word " + this.getName());
	}
	
	public final String getName()
	{
		return this.name;
	}
	
	public final void setName(final String name)
	{
		final String query = "UPDATE word " + "SET name = ? " + "WHERE word_id = ?";
		
		VPS.execute(query, name, this.word_id);
		this.name = name;
		
		LogItem.debug("Updated name for word " + this.getName());
	}
	
	public final Language getLanguage()
	{
		return this.language;
	}
	
	public final void setLanguage(final Language l)
	{
		final String query = "UPDATE word " + "SET language_code = ? " + "WHERE word_id = ?";
		
		final String language_code = l.getLanguage_code();
		
		VPS.execute(query, language_code, this.word_id);
		this.language = l;
		
		LogItem.debug("Updated language for word " + this.getName());
	}
	
	public final List<Translation> getTranslations(final Language l)
	{
		final String query = "SELECT word1_id, word2_id " + "FROM translation t " + "JOIN word w2 " + "ON t.word2_id = w2.word_id " + "WHERE t.word1_id = ? " + "AND w2.language_code = ? " + "UNION "
			+ "SELECT word1_id, word2_id " + "FROM translation t " + "JOIN word w1 " + "ON t.word1_id = w1.word_id " + "WHERE t.word2_id = ? " + "AND w1.language_code = ?";
		
		final String language_code = l.getLanguage_code();
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(this.word_id, language_code, this.word_id, language_code))
		{
			final List<Translation> translations = new ArrayList<>();
			while (rs.next())
			{
				final Integer		word1_id	= rs.getInt("word1_id");
				final Integer		word2_id	= rs.getInt("word2_id");
				final Word			w1			= Word.get(word1_id);
				final Word			w2			= Word.get(word2_id);
				final Translation	t			= Translation.get(w1, w2);
				
				translations.add(t);
			}
			return translations;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public final List<WordCheck> getWordChecks(final Language l)
	{
		final String query = "SELECT c.trial_id " + "FROM wordcheck c " + "JOIN trial t " + "ON c.trial_id = t.trial_id " + "WHERE c.word_id = ? " + "AND t.language_code_to = ?";
		
		final String language_code = l.getLanguage_code();
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(this.word_id, language_code))
		{
			final List<WordCheck> wordchecks = new ArrayList<>();
			while (rs.next())
			{
				final Integer	trial_id	= rs.getInt("trial_id");
				final WordCheck	c			= WordCheck.get(this, Trial.get(trial_id));
				wordchecks.add(c);
			}
			return wordchecks;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
}
