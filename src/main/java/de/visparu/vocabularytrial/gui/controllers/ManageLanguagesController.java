package de.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import de.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.model.db.entities.Language;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.views.LanguageView;
import de.visparu.vocabularytrial.util.GUIUtil;
import de.visparu.vocabularytrial.util.I18N;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public final class ManageLanguagesController implements Initializable, LanguageComponent, VokAbfController
{
	@FXML
	private TableView<LanguageView>				tv_languages;
	@FXML
	private TableColumn<LanguageView, String>	tc_language_code;
	@FXML
	private TableColumn<LanguageView, String>	tc_language;
	@FXML
	private TableColumn<LanguageView, Void>		tc_delete;
	
	private Stage stage;
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new Stage with ManageLanguagesController");
		
		this.tc_language_code.setCellValueFactory(new PropertyValueFactory<LanguageView, String>("language_code"));
		this.tc_language.setCellValueFactory(new PropertyValueFactory<LanguageView, String>("name"));
		this.tc_delete.setCellFactory(e ->
		{
			final TableCell<LanguageView, Void> cell = new TableCell<LanguageView, Void>()
			{
				private final Button btn = new Button();
				{
					this.btn.textProperty().bind(I18N.createStringBinding("gui.languages.table.data.delete"));
					this.btn.setOnAction((ActionEvent event) ->
					{
						LanguageView lv = (LanguageView) this.getTableRow().getItem();
						ManageLanguagesController.this.removeLanguage(lv);
						LogItem.info("Removed language " + lv.getName());
					});
				}
				
				@Override
				public final void updateItem(final Void item, final boolean empty)
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
		this.repopulateLanguages();
		
		LogItem.debug("Finished initializing new stage");
	}
	
	@Override
	public final void repopulateLanguages()
	{
		final ObservableList<LanguageView> lv_list = FXCollections.observableArrayList();
		Language.getAll().forEach(l -> lv_list.add(new LanguageView(l)));
		this.tv_languages.setItems(lv_list);
		LogItem.debug("Repopulated languages");
	}
	
	private final void removeLanguage(final LanguageView lv)
	{
		final Language	l			= Language.get(lv.getLanguage_code());
		final int		wordCount	= l.getWords().size();
		if (wordCount > 0)
		{
			final Alert					alert	= new Alert(AlertType.WARNING, I18N.createStringBinding("gui.languages.alert.wordsexist").get(), ButtonType.YES, ButtonType.NO);
			final Optional<ButtonType>	result	= alert.showAndWait();
			if (!result.isPresent() || (result.isPresent() && result.get() != ButtonType.YES))
			{
				LogItem.debug("Aborted removing language");
				return;
			}
		}
		Language.removeLanguage(l.getLanguage_code());
		LogItem.info("Language " + l.getName() + " removed");
	}
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.debug("Stage closed");
	}
	
	@Override
	public final void closeRequest()
	{
		AddLanguageController.instances.forEach(i -> i.close());
	}
	
	@Override
	public final void setStage(final Stage stage)
	{
		this.stage = stage;
	}
	
	@FXML
	public final void newLanguage(final ActionEvent event)
	{
		final String				fxmlName	= "AddLanguage";
		final AddLanguageController	alc			= new AddLanguageController();
		final StringBinding			title		= I18N.createStringBinding("gui.addlanguage.title");
		GUIUtil.createNewStage(fxmlName, alc, title);
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void close(final ActionEvent event)
	{
		this.close();
	}
}
