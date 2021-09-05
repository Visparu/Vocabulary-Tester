package com.visparu.vocabularytrial.util;

import java.io.IOException;
import java.net.URL;

import com.visparu.vocabularytrial.gui.controllers.MainMenuController;
import com.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import com.visparu.vocabularytrial.gui.interfaces.LogComponent;
import com.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.gui.interfaces.WordComponent;
import com.visparu.vocabularytrial.model.db.entities.LogItem;

import javafx.beans.binding.StringBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class GUIUtil
{
	public static final Stage createMainStage(Stage primaryStage)
	{
		try
		{
			final URL			url		= GUIUtil.class.getResource("/com/visparu/vocabularytrial/gui/fxml/MainMenu.fxml");
			final FXMLLoader	loader	= new FXMLLoader(url);
			
			final MainMenuController mmc = new MainMenuController(primaryStage);
			LanguageComponent.instances.add(mmc);
			WordComponent.instances.add(mmc);
			primaryStage.setOnCloseRequest(e ->
			{
				LanguageComponent.instances.remove(mmc);
				WordComponent.instances.remove(mmc);
				VokAbfController.closeAll();
			});
			
			loader.setController(mmc);
			loader.setResources(I18N.getResources());
			final Parent root = loader.load();
			
			final Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.title"));
			primaryStage.show();
			
			LogItem.debug("Main stage initialized");
			
			return primaryStage;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static final Stage createNewStage(final String fxmlName, final VokAbfController vac, final StringBinding title)
	{
		try
		{
			final Stage stage = new Stage();
			stage.setOnCloseRequest(e ->
			{
				VokAbfController.instances.remove(vac);
				if (vac instanceof LanguageComponent)
				{
					LanguageComponent lc = (LanguageComponent) vac;
					LanguageComponent.instances.remove(lc);
				}
				if (vac instanceof LogComponent)
				{
					LogComponent lc = (LogComponent) vac;
					LogComponent.instances.remove(lc);
				}
				if (vac instanceof TrialComponent)
				{
					TrialComponent tc = (TrialComponent) vac;
					TrialComponent.instances.remove(tc);
				}
				if (vac instanceof WordComponent)
				{
					WordComponent wc = (WordComponent) vac;
					WordComponent.instances.remove(wc);
				}
				
				vac.closeRequest();
			});
			
			final URL			url		= GUIUtil.class.getResource(String.format("/com/visparu/vocabularytrial/gui/fxml/%s.fxml", fxmlName));
			final FXMLLoader	loader	= new FXMLLoader(url);
			
			vac.setStage(stage);
			VokAbfController.instances.add(vac);
			if (vac instanceof LanguageComponent)
			{
				LanguageComponent lc = (LanguageComponent) vac;
				LanguageComponent.instances.add(lc);
			}
			if (vac instanceof LogComponent)
			{
				LogComponent lc = (LogComponent) vac;
				LogComponent.instances.add(lc);
			}
			if (vac instanceof TrialComponent)
			{
				TrialComponent tc = (TrialComponent) vac;
				TrialComponent.instances.add(tc);
			}
			if (vac instanceof WordComponent)
			{
				WordComponent wc = (WordComponent) vac;
				WordComponent.instances.add(wc);
			}
			
			loader.setController(vac);
			loader.setResources(I18N.getResources());
			final Parent root = loader.load();
			
			final Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.titleProperty().bind(title);
			stage.show();
			
			LogItem.debug("Created new default stage");
			
			return stage;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
