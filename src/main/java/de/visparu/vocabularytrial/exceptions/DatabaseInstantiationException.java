package de.visparu.vocabularytrial.exceptions;

public class DatabaseInstantiationException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public DatabaseInstantiationException(Exception e)
	{
		super(e);
	}
}
