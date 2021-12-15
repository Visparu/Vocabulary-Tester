package de.visparu.vocabularytrial.model.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.visparu.vocabularytrial.exceptions.DatabaseInstantiationException;
import de.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import de.visparu.vocabularytrial.model.db.VPS;
import de.visparu.vocabularytrial.util.ConvertUtil;

public final class Trial
{
	private static final Map<Integer, Trial>	cache	= new HashMap<>();
	private Integer								trial_id;
	private final LocalDateTime					datetime;
	private final Language						language_from;
	private final Language						language_to;
	
	public Trial(final Integer trial_id, final LocalDateTime datetime, final Language language_from, final Language language_to)
	{
		this.trial_id		= trial_id;
		this.datetime		= datetime;
		this.language_from	= language_from;
		this.language_to	= language_to;
		
		LogItem.debug("Initialized new trial " + ConvertUtil.convertDateToString(datetime) + "");
	}
	
	public static final void createTable()
	{
		String query = "CREATE TABLE IF NOT EXISTS trial(" + "trial_id INTEGER PRIMARY KEY AUTOINCREMENT," + "datetime VARCHAR(23), " + "language_code_from VARCHAR(2), "
			+ "language_code_to VARCHAR(2), " + "FOREIGN KEY(language_code_from) REFERENCES language(language_code) ON UPDATE CASCADE ON DELETE CASCADE, "
			+ "FOREIGN KEY(language_code_to) REFERENCES language(language_code) ON UPDATE CASCADE ON DELETE CASCADE" + ")";
		
		VPS.execute(query);
		
		LogItem.debug("Trial table created");
	}
	
	public static final void clearCache()
	{
		Trial.cache.clear();
		
		LogItem.debug("Cleared trial cache");
	}
	
	public static final Trial get(final Integer trial_id)
	{
		if (Trial.cache.containsKey(trial_id))
		{
			Trial t = Trial.cache.get(trial_id);
			return t;
		}
		Trial t = Trial.readEntity(trial_id);
		return t;
	}
	
	public static final Trial createTrial(final LocalDateTime date, final Language language_from, final Language language_to)
	{
		final Trial		t			= new Trial(-1, date, language_from, language_to);
		final Integer	trial_id	= Trial.writeEntity(t);
		t.setTrial_id(trial_id);
		Trial.cache.put(trial_id, t);
		TrialComponent.repopulateAllTrials();
		return t;
	}
	
	public static final void removeTrial(final Integer trial_id)
	{
		final String query = "DELETE FROM trial " + "WHERE trial_id = ?";
		
		String date = ConvertUtil.convertDateToString(Trial.get(trial_id).getDateTime());
		
		VPS.execute(query, trial_id);
		Trial.cache.remove(trial_id);
		
		LogItem.debug("Trial at " + date + " removed");
	}
	
	public static final void removeAllTrials()
	{
		String query = "DELETE FROM trial";
		
		VPS.execute(query);
		Trial.clearCache();
		
		LogItem.debug("All trials removed");
	}
	
	private static final Trial readEntity(final Integer trial_id)
	{
		final String query = "SELECT * " + "FROM trial " + "WHERE trial_id = ?";
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(trial_id))
		{
			if (rs.next())
			{
				final String		dateString		= rs.getString("datetime");
				final LocalDateTime	date			= ConvertUtil.convertStringToDate(dateString);
				final String		l_fromString	= rs.getString("language_code_from");
				final Language		l_from			= Language.get(l_fromString);
				final String		l_toString		= rs.getString("language_code_to");
				final Language		l_to			= Language.get(l_toString);
				final Trial			t				= new Trial(trial_id, date, l_from, l_to);
				Trial.cache.put(trial_id, t);
				
				return t;
			}
			return null;
		}
		catch (SQLException | DatabaseInstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static final Integer writeEntity(final Trial trial)
	{
		final String query = "INSERT INTO trial(datetime, language_code_from, language_code_to) " + "VALUES(?, ?, ?)";
		
		final String	dateString			= ConvertUtil.convertDateToString(trial.getDateTime());
		final String	language_code_from	= trial.getLanguage_from().getLanguage_code();
		final String	language_code_to	= trial.getLanguage_to().getLanguage_code();
		
		List<Integer> keys = VPS.execute(query, dateString, language_code_from, language_code_to);
		if (keys.isEmpty())
		{
			return -1;
		}
		final Integer trial_id = keys.get(0);
		
		LogItem.debug("Inserted new trial entity at " + dateString);
		
		return trial_id;
	}
	
	public static final List<Trial> getTrials(final Language l_from, final Language l_to)
	{
		final String query = "SELECT trial_id " + "FROM trial " + "WHERE language_code_from = ? " + "AND language_code_to = ?";
		
		final String	language_code_from	= l_from.getLanguage_code();
		final String	language_code_to	= l_to.getLanguage_code();
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(language_code_from, language_code_to))
		{
			final List<Trial> trials = new ArrayList<>();
			while (rs.next())
			{
				final int	trial_id	= rs.getInt("trial_id");
				final Trial	t			= Trial.get(trial_id);
				trials.add(t);
			}
			return trials;
		}
		catch (SQLException | DatabaseInstantiationException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public final Integer getTrial_id()
	{
		return this.trial_id;
	}
	
	private final void setTrial_id(final Integer trial_id)
	{
		this.trial_id = trial_id;
	}
	
	public final LocalDateTime getDateTime()
	{
		return this.datetime;
	}
	
	public final Language getLanguage_from()
	{
		return this.language_from;
	}
	
	public Language getLanguage_to()
	{
		return this.language_to;
	}
	
	public final List<WordCheck> getWordChecks()
	{
		final String query = "SELECT * " + "FROM wordcheck " + "WHERE trial_id = ?";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(this.trial_id))
		{
			final List<WordCheck> wordchecks = new ArrayList<>();
			while (rs.next())
			{
				final Integer	word_id	= rs.getInt("word_id");
				final WordCheck	wc		= WordCheck.get(Word.get(word_id), this);
				wordchecks.add(wc);
			}
			return wordchecks;
		}
		catch (SQLException | DatabaseInstantiationException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
}
