package de.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import de.visparu.vocabularytrial.model.db.entities.LogItem;

public interface TrialComponent
{
	List<TrialComponent> instances = new ArrayList<>();
	
	static void repopulateAllTrials()
	{
		TrialComponent.instances.forEach(i -> i.repopulateTrials());
		LogItem.debug("All trials repopulated");
	}
	
	void repopulateTrials();
}
