package de.visparu.vocabularytrial.root;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import de.visparu.vocabularytrial.exceptions.DatabaseInstantiationException;
import de.visparu.vocabularytrial.model.db.Database;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.log.Severity;
import de.visparu.vocabularytrial.util.C11N;
import de.visparu.vocabularytrial.util.GUIUtil;
import de.visparu.vocabularytrial.util.I18N;
import de.visparu.vocabularytrial.util.IOUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public final class VocabularyTrialJavaFxApplication extends Application
{
	public static final String NAME         = I18N.createStringBinding("root.name").get();
	public static final String VERSION      = "0.6.0";
	public static final String AUTHOR       = "Oliver Stiller";
	public static final String RELEASE_DATE = "26.12.2019";
	
	private ConfigurableApplicationContext applicationContext;
	
	@Override
	public final void init()
	{
		this.applicationContext = new SpringApplicationBuilder(VocabularyTrialSpringApplication.class).run();
	}
	
	@Override
	public final void start(final Stage primaryStage)
	{
		this.initializeDatabase();
		LogItem.initializeNewLogSession();
		LogItem.createLogItem(Severity.INFO, "Initialized database and log");
		this.initializeStage(primaryStage);
	}
	
	@Override
	public void stop()
	{
		this.applicationContext.close();
		Platform.exit();
	}
	
	private final void initializeDatabase()
	{
		try
		{
			Database.get().activateForeignKeyPragma();
			Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
		}
		catch(DatabaseInstantiationException e)
		{
			C11N.setDatabasePath(IOUtil.DATA_PATH + "temp.db");
			try
			{
				Database.get().activateForeignKeyPragma();
				Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			}
			catch (DatabaseInstantiationException e1)
			{
				e.printStackTrace();
			}
		}
	}
	
	private final void initializeStage(final Stage primaryStage)
	{
		GUIUtil.createMainStage(primaryStage);
	}
}
