package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

import javafx.stage.Stage;

public interface VokAbfController
{
	List<VokAbfController> instances = new ArrayList<>();
	
	static void repopulateAll()
	{
		LanguageComponent.repopulateAllLanguages();
		WordComponent.repopulateAllWords();
		TrialComponent.repopulateAllTrials();
		LogComponent.repopulateAllLogs();
		LogItem.debug("All components repopulated");
	}
	
	static void closeAll()
	{
		while (!VokAbfController.instances.isEmpty())
		{
			VokAbfController.instances.remove(0).close();
		}
		LogItem.debug("All stages closed");
	}
	
	default void closeRequest()
	{
		
	}
	
	void setStage(Stage stage);
	
	void close();
}
