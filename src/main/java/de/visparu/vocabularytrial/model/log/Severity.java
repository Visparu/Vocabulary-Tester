package de.visparu.vocabularytrial.model.log;

import javafx.scene.paint.Color;

public enum Severity
{
	DEBUG, INFO, WARNING, ERROR, CRITICAL;
	
	public static Color getBackgroundColor(Severity severity)
	{
		switch (severity)
		{
			case DEBUG:
			{
				return Color.GRAY;
			}
			case INFO:
			{
				return Color.LIGHTGREEN;
			}
			case WARNING:
			{
				return Color.YELLOW;
			}
			case ERROR:
			{
				return Color.ORANGE;
			}
			case CRITICAL:
			{
				return Color.RED;
			}
			default:
			{
				return Color.PURPLE;
			}
		}
	}
	
	public static Color getForegroundColor(Severity severity)
	{
		switch (severity)
		{
			case DEBUG:
			{
				return Color.WHITE;
			}
			case INFO:
			{
				return Color.BLACK;
			}
			case WARNING:
			{
				return Color.BLACK;
			}
			case ERROR:
			{
				return Color.BLACK;
			}
			case CRITICAL:
			{
				return Color.WHITE;
			}
			default:
			{
				return Color.WHITE;
			}
		}
	}
}
