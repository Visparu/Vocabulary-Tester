package com.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import com.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import com.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import com.visparu.vocabularytrial.model.db.entities.Language;
import com.visparu.vocabularytrial.model.db.entities.LogItem;
import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.model.views.TrialView;
import com.visparu.vocabularytrial.util.GUIUtil;
import com.visparu.vocabularytrial.util.I18N;

import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public final class TrialListController implements Initializable, VokAbfController, LanguageComponent, TrialComponent
{
	@FXML
	private ChoiceBox<Language>				cb_language_from;
	@FXML
	private ChoiceBox<Language>				cb_language_to;
	@FXML
	private TableView<TrialView>			tv_trials;
	@FXML
	private TableColumn<TrialView, String>	tc_date;
	@FXML
	private TableColumn<TrialView, String>	tc_count;
	@FXML
	private TableColumn<TrialView, String>	tc_correct;
	@FXML
	private TableColumn<TrialView, String>	tc_wrong;
	@FXML
	private TableColumn<TrialView, String>	tc_percentage;
	@FXML
	private TableColumn<TrialView, Void>	tc_view;
	
	private Stage			stage;
	private final Language	init_l_from;
	private final Language	init_l_to;
	
	public TrialListController(final Language init_l_from, final Language init_l_to)
	{
		this.init_l_from	= init_l_from;
		this.init_l_to		= init_l_to;
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with TrialListController...");
		
		this.repopulateLanguages_from();
		this.cb_language_from.getSelectionModel().select(this.init_l_from);
		this.repopulateLanguages_to();
		this.cb_language_to.getSelectionModel().select(this.init_l_to);
		this.cb_language_from.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateLanguages_to();
			this.repopulateTrials();
		});
		this.cb_language_to.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateTrials();
		});
		this.repopulateTrials();
		this.tc_date.setCellValueFactory(new PropertyValueFactory<TrialView, String>("date"));
		this.tc_count.setCellValueFactory(new PropertyValueFactory<TrialView, String>("count"));
		this.tc_correct.setCellValueFactory(new PropertyValueFactory<TrialView, String>("correct"));
		this.tc_wrong.setCellValueFactory(new PropertyValueFactory<TrialView, String>("wrong"));
		this.tc_percentage.setCellValueFactory(new PropertyValueFactory<TrialView, String>("percentage"));
		this.tc_view.setCellFactory(e ->
		{
			final TableCell<TrialView, Void> cell = new TableCell<TrialView, Void>()
			{
				private final Button btn = new Button();
				{
					this.btn.textProperty().bind(I18N.createStringBinding("gui.triallist.table.data.view"));
					this.btn.setOnAction((ActionEvent event) ->
					{
						final TrialResultController	trc		= new TrialResultController(Trial.get(((TrialView) this.getTableRow().getItem()).getTrial_id()));
						final StringBinding			title	= I18N.createStringBinding("gui.result.title");
						GUIUtil.createNewStage("TrialResult", trc, title);
					});
				}
				
				@Override
				public final void updateItem(Void item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty)
					{
						this.setGraphic(null);
					}
					else
					{
						this.setGraphic(this.btn);
					}
				}
			};
			return cell;
		});
		
		LogItem.debug("New stage initialized");
	}
	
	@Override
	public final void repopulateLanguages()
	{
		this.repopulateLanguages_from();
		this.repopulateLanguages_to();
	}
	
	private final void repopulateLanguages_from()
	{
		this.cb_language_from.setItems(FXCollections.observableArrayList(Language.getAll()));
		LogItem.debug("Languages_from repopulated");
	}
	
	private final void repopulateLanguages_to()
	{
		final Language l_prev = this.cb_language_to.getSelectionModel().getSelectedItem();
		this.cb_language_to.setItems(FXCollections.observableArrayList(Language.getAll()));
		this.cb_language_to.getItems().remove(this.cb_language_from.getSelectionModel().getSelectedItem());
		if (this.cb_language_to.getItems().contains(l_prev))
		{
			this.cb_language_to.getSelectionModel().select(l_prev);
		}
		LogItem.debug("Languages_to repopulated");
	}
	
	@Override
	public final void repopulateTrials()
	{
		final Language	l_from	= this.cb_language_from.getSelectionModel().getSelectedItem();
		final Language	l_to	= this.cb_language_to.getSelectionModel().getSelectedItem();
		if (l_from == null || l_to == null)
		{
			this.tv_trials.getItems().clear();
			return;
		}
		final List<TrialView> trialViews = FXCollections.observableArrayList();
		Trial.getTrials(l_from, l_to).forEach(t -> trialViews.add(new TrialView(t)));
		this.tv_trials.setItems(FXCollections.observableArrayList(trialViews));
		LogItem.debug("Trials repopulated");
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
		LogItem.debug("Stage closed");
	}
	
	@FXML
	public final void close(final ActionEvent event)
	{
		this.close();
	}
}
