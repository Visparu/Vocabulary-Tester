package de.visparu.vocabularytrial.model.views;

import de.visparu.vocabularytrial.model.db.entities.Language;

public final class LanguageView
{
	private Language language;
	
	public LanguageView(final Language language)
	{
		this.language = language;
	}
	
	public final String getLanguage_code()
	{
		return this.language.getLanguage_code();
	}
	
	public final String getName()
	{
		return this.language.getName();
	}
}
