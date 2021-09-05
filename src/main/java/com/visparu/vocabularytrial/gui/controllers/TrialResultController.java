package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.db.entities.WordCheck;
import com.visparu.vocabularytrial.model.views.WordCheckView;
import com.visparu.vocabularytrial.util.ConvertUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public final class TrialResultController implements Initializable, VokAbfController
{
	@FXML
	private Label								lb_date;
	@FXML
	private Label								lb_language_from;
	@FXML
	private Label								lb_language_to;
	@FXML
	private TableView<WordCheckView>			tv_checks;
	@FXML
	private TableColumn<WordCheckView, String>	tc_word;
	@FXML
	private TableColumn<WordCheckView, String>	tc_answer;
	@FXML
	private TableColumn<WordCheckView, String>	tc_solution;
	@FXML
	private TableColumn<WordCheckView, Boolean>	tc_correct;
	@FXML
	private Label								lb_checks;
	@FXML
	private Label								lb_correct;
	@FXML
	private Label								lb_wrong;
	@FXML
	private Label								lb_perc;
	
	private Stage		stage;
	private final Trial	trial;
	
	public TrialResultController(final Trial trial)
	{
		this.trial = trial;
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with TrialResultController");
		
		this.lb_date.setText(ConvertUtil.convertDateToString(this.trial.getDateTime()));
		this.lb_language_from.setText(this.trial.getLanguage_from().getName());
		this.lb_language_to.setText(this.trial.getLanguage_to().getName());
		this.tc_word.setCellValueFactory(new PropertyValueFactory<>("name"));
		this.tc_answer.setCellValueFactory(new PropertyValueFactory<>("answerString"));
		this.tc_solution.setCellValueFactory(new PropertyValueFactory<>("translationString"));
		this.tc_correct.setCellValueFactory(new PropertyValueFactory<>("correct"));
		this.tc_correct.setCellFactory(tc -> new CheckBoxTableCell<>());
		final List<WordCheck> wordchecks = this.trial.getWordChecks();
		wordchecks.forEach(c -> this.tv_checks.getItems().add(new WordCheckView(c)));
		final int		count	= wordchecks.size();
		final long		correct	= wordchecks.stream().filter(c -> c.isCorrect().get()).count();
		final long		wrong	= count - correct;
		final double	perc	= (double) correct / count;
		this.lb_checks.setText(String.valueOf(count));
		this.lb_correct.setText(String.valueOf(correct));
		this.lb_wrong.setText(String.valueOf(wrong));
		this.lb_perc.setText(String.format("%.2f", perc * 100));
		
		LogItem.debug("New stage initialized");
	}
	
	@FXML
	public final void exit(final ActionEvent event)
	{
		this.close();
		LogItem.debug("Stage closed");
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
	}
}
