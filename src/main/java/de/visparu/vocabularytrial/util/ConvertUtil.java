package de.visparu.vocabularytrial.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import de.visparu.vocabularytrial.model.db.entities.LogItem;

public final class ConvertUtil
{
	
	private static final LocalDateTime default_datetime = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
	
	private static final DateTimeFormatter	dtf				= DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private static final DateTimeFormatter	dtf_readable	= DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss");
	
	public static final String convertDateToString(LocalDateTime ldt)
	{
		return ldt.format(ConvertUtil.dtf);
	}
	
	public static final String convertDateToReadableString(LocalDateTime ldt)
	{
		return ldt.format(ConvertUtil.dtf_readable);
	}
	
	public static final LocalDateTime convertStringToDate(String s)
	{
		try
		{
			return LocalDateTime.from(ConvertUtil.dtf.parse(s));
		}
		catch (DateTimeParseException e)
		{
			LogItem.warning("Deprecated date format", "Read string: " + s);
			return ConvertUtil.default_datetime;
		}
	}
	
}
