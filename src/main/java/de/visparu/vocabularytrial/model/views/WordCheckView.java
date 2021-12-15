package de.visparu.vocabularytrial.model.views;

import java.util.List;

import de.visparu.vocabularytrial.model.db.entities.Translation;
import de.visparu.vocabularytrial.model.db.entities.WordCheck;
import javafx.beans.property.BooleanProperty;

public final class WordCheckView
{
	private final WordCheck wc;
	
	public WordCheckView(final WordCheck wc)
	{
		this.wc = wc;
	}
	
	public final String getName()
	{
		final String name = this.wc.getWord().getName();
		return name;
	}
	
	public final String getAnswerString()
	{
		return this.wc.getAnswerString();
	}
	
	public final String getTranslationString()
	{
		final List<Translation>	tlist	= this.wc.getWord().getTranslations(this.wc.getTrial().getLanguage_to());
		final StringBuilder		sb		= new StringBuilder();
		for (int i = 0; i < tlist.size(); i++)
		{
			final Translation t = tlist.get(i);
			if (i != 0)
			{
				sb.append(", ");
			}
			final String name;
			if (this.wc.getWord().getWord_id().equals(t.getWord1_id()))
			{
				name = t.getWord2().getName();
			}
			else
			{
				name = t.getWord1().getName();
			}
			sb.append(name);
		}
		String ret = sb.toString();
		return ret;
	}
	
	public final BooleanProperty correctProperty()
	{
		return this.wc.isCorrect();
	}
}
