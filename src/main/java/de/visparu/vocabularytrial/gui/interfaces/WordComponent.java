package de.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import de.visparu.vocabularytrial.model.db.entities.LogItem;

public interface WordComponent
{
	List<WordComponent> instances = new ArrayList<>();
	
	static void repopulateAllWords()
	{
		WordComponent.instances.forEach(i -> i.repopulateWords());
		LogItem.debug("All words repopulated");
	}
	
	void repopulateWords();
}
