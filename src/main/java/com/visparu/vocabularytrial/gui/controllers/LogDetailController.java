package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.log.Severity;
import com.visparu.vocabularytrial.model.views.LogItemView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class LogDetailController implements Initializable, VokAbfController
{
	@FXML
	private Label		tf_logitem_id;
	@FXML
	private Label		tf_log_id;
	@FXML
	private Label		tf_message;
	@FXML
	private Label		tf_datetime;
	@FXML
	private HBox		hb_severity;
	@FXML
	private Label		tf_severity;
	@FXML
	private Label		tf_threadname;
	@FXML
	private Label		tf_function;
	@FXML
	private TextArea	ta_description;
	
	public static final List<LogDetailController> instances = new ArrayList<>();
	
	private Stage		stage;
	private LogItemView	liv;
	
	public LogDetailController(LogItemView liv)
	{
		this.liv = liv;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.tf_logitem_id.setText(String.valueOf(this.liv.getLogitem_id()));
		this.tf_log_id.setText(String.valueOf(this.liv.getLog_id()));
		this.tf_message.setText(this.liv.getMessage());
		this.tf_datetime.setText(this.liv.getDatetime());
		this.tf_severity.setText(this.liv.getSeverity());
		this.tf_threadname.setText(this.liv.getThread());
		this.tf_function.setText(this.liv.getFunction());
		this.ta_description.setText(this.liv.getDescription());
		
		Severity	severity	= Severity.valueOf(this.liv.getSeverity());
		Color		bc			= Severity.getBackgroundColor(severity);
		Color		fc			= Severity.getForegroundColor(severity);
		
		this.hb_severity.setBackground(new Background(new BackgroundFill(bc, CornerRadii.EMPTY, Insets.EMPTY)));
		this.tf_severity.setTextFill(fc);
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
	
	@FXML
	public void close(ActionEvent event)
	{
		this.close();
	}
}
