package com.visparu.vocabularytrial.model.views;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Word;

public final class WordToLanguageView
{
	private final Word		w;
	private final Language	l;
	
	public WordToLanguageView(final Word w, final Language l)
	{
		this.w	= w;
		this.l	= l;
	}
	
	public final Integer getWord_id()
	{
		Integer word_id = this.w.getWord_id();
		return word_id;
	}
	
	public final String getName()
	{
		String name = this.w.getName();
		return name;
	}
	
	public final String getTranslationsString()
	{
		final List<Translation>	translations		= this.w.getTranslations(this.l);
		final List<Word>		translationWords	= new ArrayList<>();
		for (final Translation t : translations)
		{
			final Word tw;
			if (t.getWord1().getWord_id().equals(this.w.getWord_id()))
			{
				tw = t.getWord2();
			}
			else
			{
				tw = t.getWord1();
			}
			translationWords.add(tw);
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < translationWords.size(); i++)
		{
			final Word tw = translationWords.get(i);
			if (i != 0)
			{
				sb.append(", ");
			}
			sb.append(tw.getName());
		}
		String ret = sb.toString();
		return ret;
	}
}
