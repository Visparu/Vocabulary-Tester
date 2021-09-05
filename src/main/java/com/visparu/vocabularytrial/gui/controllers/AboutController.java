package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.root.VocabularyTrialJavaFxApplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public final class AboutController implements Initializable, VokAbfController
{
	@FXML
	private Label	lb_name;
	@FXML
	private Label	lb_version;
	@FXML
	private Label	lb_author;
	@FXML
	private Label	lb_date;
	private Stage	stage;
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with AboutController...");
		
		this.lb_name.setText(VocabularyTrialJavaFxApplication.NAME);
		this.lb_version.setText(VocabularyTrialJavaFxApplication.VERSION);
		this.lb_author.setText(VocabularyTrialJavaFxApplication.AUTHOR);
		this.lb_date.setText(VocabularyTrialJavaFxApplication.RELEASE_DATE);
		
		LogItem.debug("Finished initializing new stage");
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		this.stage = stage;
	}
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.debug("Closed stage");
	}
	
	@FXML
	public final void close(final ActionEvent event)
	{
		this.close();
	}
}
