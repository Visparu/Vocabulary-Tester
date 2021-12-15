package de.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.util.C11N;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController implements VokAbfController, Initializable
{
	@FXML
	private TextField tf_separator;
	
	private Stage stage;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		LogItem.debug("Initializing new Stage with SettingsController");
		final String separators = C11N.getSeparators();
		this.tf_separator.setText(separators);
		LogItem.debug("Finished initializing new Stage.");
	}
	
	@FXML
	public void cancel(ActionEvent event)
	{
		this.close();
	}
	
	@FXML
	public void save(ActionEvent event)
	{
		C11N.setSeparators(this.tf_separator.getText());
		this.close();
	}

	@Override
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}

	@Override
	public void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
	}
}
