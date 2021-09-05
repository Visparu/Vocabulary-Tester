package com.visparu.vocabularytrial.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.Language;

public final class Queries
{
	
	private Queries()
	{
		
	}
	
	public static final List<Language> queryAllLanguages()
	{
		final List<Language>	languages	= new ArrayList<>();
		final String			query		= "SELECT language_code " + "FROM language";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query())
		{
			while (rs.next())
			{
				final String	language_code	= rs.getString("language_code");
				final Language	language		= Language.get(language_code);
				if (language != null)
				{
					languages.add(language);
				}
			}
			return languages;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
}
