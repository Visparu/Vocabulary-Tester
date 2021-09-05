package com.visparu.vocabularytrial.model.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.sqlite.JDBC;

import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Translation;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.Word;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.model.views.WordToLanguageView;
import com.visparu.vocabularytrial.util.C11N;

import javafx.collections.ObservableList;

public final class Database
{
	private static Database	instance;
	private String			driver;
	private String			protocol;
	private String			filename;
	private Connection		connection;
	static
	{
		try
		{
			DriverManager.registerDriver(new JDBC());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private Database(final String driver, final String protocol, final String filename)
	{
		this.driver		= driver;
		this.protocol	= protocol;
		this.filename	= filename;
		try
		{
			this.connection = DriverManager.getConnection(this.getConnectionString());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static final Database get()
	{
		if (Database.instance == null)
		{
			Database instance = Database.get(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			return instance;
		}
		Database instance = Database.instance;
		return instance;
	}
	
	private static final Database get(final String driver, final String protocol, final String filename)
	{
		Database.instance = new Database(driver, protocol, filename);
		Translation.clearCache();
		Word.clearCache();
		Language.clearCache();
		Database instance = Database.instance;
		return instance;
	}
	
	public final void activateForeignKeyPragma()
	{
		try (final PreparedStatement pstmt = this.prepareStatement("PRAGMA foreign_keys = ON"))
		{
			pstmt.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public final void changeDatabase(final String driver, final String protocol, final String filename)
	{
		Database.get(driver, protocol, filename);
		LogItem.createTable();
		Language.createTable();
		Word.createTable();
		Translation.createTable();
		Trial.createTable();
		WordCheck.createTable();
		LogItem.debug("All tables created for " + filename);
	}
	
	public final void copyDatabase(final File newFile)
	{
		try
		{
			Files.copy(Paths.get(this.filename), new FileOutputStream(newFile));
			LogItem.debug("Database copied to location " + newFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public final PreparedStatement prepareStatement(final String query) throws SQLException
	{
		final Connection		conn	= this.getConnection();
		final PreparedStatement	pstmt	= conn.prepareStatement(query);
		return pstmt;
	}
	
	private final void fillPreparedStatement(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		for (int i = 0; i < params.length; i++)
		{
			final Object param = params[i];
			if (param instanceof Integer)
			{
				final Integer param_i = (Integer) param;
				pstmt.setInt(i + 1, param_i);
			}
			else if (param instanceof String)
			{
				final String param_s = (String) param;
				pstmt.setString(i + 1, param_s);
			}
			else
			{
				throw new IllegalArgumentException("Type " + param.getClass().getName() + " is not supported!");
			}
		}
	}
	
	public final void execute(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		this.fillPreparedStatement(pstmt, params);
		pstmt.execute();
	}
	
	public final ResultSet executeQuery(final PreparedStatement pstmt, final Object... params) throws SQLException
	{
		this.fillPreparedStatement(pstmt, params);
		final ResultSet rs = pstmt.executeQuery();
		return rs;
	}
	
	private final String getConnectionString()
	{
		String ret = String.format("%s:%s:%s", this.driver, this.protocol, this.filename);
		return ret;
	}
	
	private final Connection getConnection()
	{
		return this.connection;
	}

	public void createNewDatabase(String path, ObservableList<WordToLanguageView> items)
	{
		Set<Word> words = new HashSet<>();
		for(WordToLanguageView wtlv : items)
		{
			words.add(Word.get(wtlv.getWord_id()));
		}
		Set<Language> languages = new HashSet<>();
		for(Word w : words)
		{
			languages.add(w.getLanguage());
			for(Language l : Language.getAll())
			{
				for(Translation t : w.getTranslations(l))
				{
					languages.add(t.getWord1().getLanguage());
					languages.add(t.getWord2().getLanguage());
				}
			}
		}
		Set<Translation> translations = new HashSet<>();
		for(Word w : words)
		{
			for(Language l : languages)
			{
				for(Translation t : w.getTranslations(l))
				{
					translations.add(t);
				}
			}
		}
		for(Translation t : translations)
		{
			words.add(t.getWord1());
			words.add(t.getWord2());
		}
		
		String connString = String.format("jdbc:sqlite:%s", path);
		try(final Connection conn = DriverManager.getConnection(connString); final Statement stmt = conn.createStatement())
		{
			final String createLangTableQuery = "CREATE TABLE IF NOT EXISTS language (" + "language_code VARCHAR(2) PRIMARY KEY, " + "name VARCHAR(30)" + ")";
			final String createWordTableQuery = "CREATE TABLE IF NOT EXISTS word(" + "word_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "name VARCHAR(100), " + "language_code VARCHAR(2), " + "FOREIGN KEY(language_code) REFERENCES language(language_code) ON UPDATE CASCADE" + ")";
			final String createTranTableQuery = "CREATE TABLE IF NOT EXISTS translation(" + "word1_id INTEGER, " + "word2_id INTEGER, " + "PRIMARY KEY(word1_id, word2_id), " + "FOREIGN KEY(word1_id) REFERENCES word(word_id) ON UPDATE CASCADE, " + "FOREIGN KEY(word2_id) REFERENCES word(word_id) ON UPDATE CASCADE" + ")";
			
			stmt.execute(createLangTableQuery);
			stmt.execute(createWordTableQuery);
			stmt.execute(createTranTableQuery);
			
			for(Language l : languages)
			{
				String addLangQuery = "INSERT INTO language(language_code, name) VALUES (?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(addLangQuery);
				pstmt.setString(1, l.getLanguage_code());
				pstmt.setString(2, l.getName());
				pstmt.execute();
				pstmt.close();
			}
			for(Word w : words)
			{
				String addWordQuery = "INSERT INTO word(word_id, name, language_code) VALUES (?, ?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(addWordQuery);
				pstmt.setInt(1, w.getWord_id());
				pstmt.setString(2, w.getName());
				pstmt.setString(3, w.getLanguage().getLanguage_code());
				pstmt.execute();
				pstmt.close();
			}
			for(Translation t : translations)
			{
				String addTranQuery = "INSERT INTO translation(word1_id, word2_id) VALUES (?, ?)";
				PreparedStatement pstmt = conn.prepareStatement(addTranQuery);
				pstmt.setInt(1, t.getWord1_id());
				pstmt.setInt(2, t.getWord2_id());
				pstmt.execute();
				pstmt.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
