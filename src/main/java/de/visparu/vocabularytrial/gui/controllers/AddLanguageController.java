package de.visparu.vocabularytrial.gui.controllers;

import java.util.ArrayList;
import java.util.List;

import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.model.db.entities.Language;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public final class AddLanguageController implements VokAbfController
{
	@FXML
	private TextField								tf_language_code;
	@FXML
	private TextField								tf_language;
	public static final List<AddLanguageController>	instances	= new ArrayList<>();
	private Stage									stage;
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.debug("Stage closed");
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		this.stage = stage;
	}
	
	@FXML
	public final void confirm(final ActionEvent event)
	{
		Language.createLanguage(this.tf_language_code.getText(), this.tf_language.getText());
		LogItem.info("Language " + this.tf_language.getText() + " created");
		this.close();
	}
	
	@FXML
	public final void cancel(final ActionEvent event)
	{
		this.close();
	}
}
