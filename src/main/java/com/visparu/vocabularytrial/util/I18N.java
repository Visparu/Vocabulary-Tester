package com.visparu.vocabularytrial.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.model.db.entities.LogItem;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class I18N
{
	private static final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(C11N.getLocale());
	static
	{
		I18N.locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
	}
	
	public static final List<Locale> getSupportedLocales()
	{
		ArrayList<Locale> locales = new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.GERMAN));
		return locales;
	}
	
	public static final Locale getDefaultLocale()
	{
		final Locale	sysDefault	= Locale.getDefault();
		Locale			l			= getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
		return l;
	}
	
	public static final Locale getLocale()
	{
		Locale l = I18N.locale.get();
		return l;
	}
	
	public static final void setLocale(final Locale locale)
	{
		localeProperty().set(locale);
		Locale.setDefault(locale);
		LogItem.debug("Set internal locale to " + locale.getDisplayName());
	}
	
	public static final ObjectProperty<Locale> localeProperty()
	{
		return I18N.locale;
	}
	
	public static final String get(final String key, final Object... args)
	{
		try
		{
			final ResourceBundle	bundle	= ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
			String					ret		= MessageFormat.format(bundle.getString(key), args);
			return ret;
		}
		catch (MissingResourceException e)
		{
			return key;
		}
	}
	
	public static final ResourceBundle getResources()
	{
		final ResourceBundle bundle = ResourceBundle.getBundle("com.visparu.vocabularytrial.gui.lang.lang", C11N.getLocale());
		return bundle;
	}
	
	public static final StringBinding createStringBinding(final String key, final Object... args)
	{
		StringBinding sb = Bindings.createStringBinding(() -> I18N.get(key, args), I18N.locale);
		return sb;
	}
}
