package com.visparu.vocabularytrial.gui.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface LogComponent
{
	List<LogComponent> instances = new ArrayList<>();
	
	static void repopulateAllLogs()
	{
		LogComponent.instances.forEach(i -> i.repopulateLogs());
	}
	
	void repopulateLogs();
}
