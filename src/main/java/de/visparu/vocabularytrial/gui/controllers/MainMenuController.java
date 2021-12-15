package de.visparu.vocabularytrial.gui.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

import de.visparu.vocabularytrial.debug.Debug;
import de.visparu.vocabularytrial.exceptions.DatabaseInstantiationException;
import de.visparu.vocabularytrial.gui.interfaces.LanguageComponent;
import de.visparu.vocabularytrial.gui.interfaces.TrialComponent;
import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.gui.interfaces.WordComponent;
import de.visparu.vocabularytrial.model.db.Database;
import de.visparu.vocabularytrial.model.db.VPS;
import de.visparu.vocabularytrial.model.db.entities.Language;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.db.entities.Translation;
import de.visparu.vocabularytrial.model.db.entities.Trial;
import de.visparu.vocabularytrial.model.db.entities.Word;
import de.visparu.vocabularytrial.model.db.entities.WordCheck;
import de.visparu.vocabularytrial.model.views.WordToLanguageView;
import de.visparu.vocabularytrial.util.C11N;
import de.visparu.vocabularytrial.util.GUIUtil;
import de.visparu.vocabularytrial.util.I18N;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public final class MainMenuController implements Initializable, LanguageComponent, WordComponent
{
	@FXML
	private Menu									mn_languages;
	@FXML
	private CheckMenuItem							cmi_trial_randomize;
	@FXML
	private ChoiceBox<Language>						cb_language_from;
	@FXML
	private ChoiceBox<Language>						cb_language_to;
	@FXML
	private TableView<WordToLanguageView>			tv_vocabulary;
	@FXML
	private TableColumn<WordToLanguageView, String>	tc_word;
	@FXML
	private TableColumn<WordToLanguageView, String>	tc_translations;
	@FXML
	private HBox									hb_status_bar;
	@FXML
	private Label									lb_status;
	@FXML
	private ImageView								iv_status_icon;
	
	private final Stage stage;
	
	public MainMenuController(final Stage stage)
	{
		this.stage = stage;
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with MainMenuController");
		
		int index;
		switch (C11N.getLocale().toLanguageTag())
		{
			case "en":
			{
				index = 0;
				break;
			}
			case "de":
			{
				index = 1;
				break;
			}
			default:
			{
				index = 0;
				break;
			}
		}
		((CheckMenuItem) this.mn_languages.getItems().get(index)).setSelected(true);
		this.cmi_trial_randomize.setSelected(true);
		this.repopulateLanguages_from();
		this.cb_language_from.getSelectionModel().select(0);
		this.repopulateLanguages_to();
		this.cb_language_to.getSelectionModel().select(0);
		this.repopulateWords();
		this.cb_language_from.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateLanguages_to();
			this.repopulateWords();
		});
		this.cb_language_to.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateWords();
		});
		this.tv_vocabulary.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.tv_vocabulary.setOnKeyPressed(e ->
		{
			if (e.getCode() == KeyCode.DELETE)
			{
				this.removeAllSelectedWords();
			}
		});
		this.tc_word.setCellValueFactory(new PropertyValueFactory<WordToLanguageView, String>("name"));
		this.tc_translations.setCellValueFactory(new PropertyValueFactory<WordToLanguageView, String>("translationsString"));
		LogItem.debug("Finished initializing new stage");
	}
	
	private final void removeAllSelectedWords()
	{
		final Alert					alert	= new Alert(AlertType.CONFIRMATION, I18N.createStringBinding("gui.mainmenu.alert.delete").get(), ButtonType.YES, ButtonType.NO);
		final Optional<ButtonType>	result	= alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES)
		{
			int count = this.tv_vocabulary.getSelectionModel().getSelectedItems().size();
			for (final WordToLanguageView wv : this.tv_vocabulary.getSelectionModel().getSelectedItems())
			{
				Word.removeWord(wv.getWord_id());
				this.tv_vocabulary.getItems().remove(wv);
			}
			LogItem.info("Removed " + count + " words");
		}
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
	public final void repopulateWords()
	{
		final Language	language_from	= this.cb_language_from.getSelectionModel().getSelectedItem();
		final Language	language_to		= this.cb_language_to.getSelectionModel().getSelectedItem();
		if (language_from == null || language_to == null)
		{
			this.tv_vocabulary.getItems().clear();
			return;
		}
		final List<Word>							wordsRaw	= language_from.getWords();
		final ObservableList<WordToLanguageView>	wordViews	= FXCollections.observableArrayList();
		wordsRaw.stream().filter(w -> !w.getTranslations(language_to).isEmpty()).forEach(w -> wordViews.add(new WordToLanguageView(w, language_to)));
		this.tv_vocabulary.setItems(wordViews);
		LogItem.debug("Words repopulated");
	}
	
	@FXML
	public final void file_new(final ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.menubar.file.new.title"));
		fc.getExtensionFilters().add(new ExtensionFilter(I18N.createStringBinding("gui.mainmenu.menubar.file.new.filter").get(), "*.db"));
		final File selectedFile = fc.showSaveDialog(this.stage);
		if (selectedFile != null)
		{
			try
			{
				Files.deleteIfExists(selectedFile.toPath());
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
			C11N.setDatabasePath(selectedFile.getAbsolutePath());
			try
			{
				Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			}
			catch (DatabaseInstantiationException e)
			{
				e.printStackTrace();
				return;
			}
			VokAbfController.repopulateAll();
			LogItem.info("New database file created", "New database file created under " + selectedFile.getAbsolutePath());
		}
		else
		{
			LogItem.debug("New file creation aborted");
		}
	}
	
	@FXML
	public final void file_open(final ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.menubar.file.open.title"));
		fc.getExtensionFilters().add(new ExtensionFilter(I18N.createStringBinding("gui.mainmenu.menubar.file.open.filter").get(), "*.db"));
		final File selectedFile = fc.showOpenDialog(this.stage);
		if (selectedFile != null)
		{
			C11N.setDatabasePath(selectedFile.getAbsolutePath());
			try
			{
				Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			}
			catch (DatabaseInstantiationException e)
			{
				e.printStackTrace();
				return;
			}
			VokAbfController.repopulateAll();
			LogItem.info("Switched to existing database", "Switched to existing database under " + selectedFile.getAbsolutePath());
		}
		else
		{
			LogItem.debug("File opening aborted");
		}
	}
	
	@FXML
	public final void file_saveas(final ActionEvent event)
	{
		final FileChooser fc = new FileChooser();
		fc.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.menubar.file.saveas.title"));
		fc.getExtensionFilters().add(new ExtensionFilter(I18N.createStringBinding("gui.mainmenu.menubar.file.saveas.filter").get(), "*.db"));
		final File selectedFile = fc.showSaveDialog(this.stage);
		if (selectedFile != null)
		{
			if (C11N.getDatabasePath().equals(selectedFile))
			{
				return;
			}
			C11N.setDatabasePath(selectedFile.getAbsolutePath());
			try
			{
				Database.get().copyDatabase(selectedFile);
				Database.get().changeDatabase(C11N.getDriver(), C11N.getProtocol(), C11N.getDatabasePath().getAbsolutePath());
			}
			catch (DatabaseInstantiationException e)
			{
				e.printStackTrace();
				return;
			}
			VokAbfController.repopulateAll();
			LogItem.info("Saved database to new file", "Saved database to " + selectedFile.getAbsolutePath());
		}
		else
		{
			LogItem.debug("Aborted saving the database");
		}
	}
	
	@FXML
	public final void file_saveselectionas(final ActionEvent event)
	{
		if(this.tv_vocabulary.getSelectionModel().isEmpty())
		{
			Alert alert = new Alert(AlertType.ERROR, I18N.createStringBinding("gui.mainmenu.alert.selectionempty").get(), ButtonType.OK);
			alert.showAndWait();
			return;
		}
		final FileChooser fc = new FileChooser();
		fc.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.menubar.file.saveselection.title"));
		fc.getExtensionFilters().add(new ExtensionFilter(I18N.createStringBinding("gui.mainmenu.menubar.file.saveselection.filter").get(), "*.db"));
		final File selectedFile = fc.showSaveDialog(this.stage);
		if (selectedFile != null)
		{
			if(C11N.getDatabasePath().equals(selectedFile))
			{
				Alert alert = new Alert(AlertType.ERROR, I18N.createStringBinding("gui.mainmenu.alert.overwrite").get(), ButtonType.OK);
				alert.showAndWait();
				return;
			}
			if(Files.exists(selectedFile.toPath()))
			{
				try {
					Files.delete(selectedFile.toPath());
				} catch (IOException e) {
					Alert alert = new Alert(AlertType.ERROR, I18N.createStringBinding("gui.mainmenu.alert.overwrite.impossible").get(), ButtonType.OK);
					alert.showAndWait();
					return;
				}
			}
			try
			{
				Database.get().createNewDatabase(selectedFile.getAbsolutePath(), this.tv_vocabulary.getSelectionModel().getSelectedItems());
			}
			catch (DatabaseInstantiationException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	
	@FXML
	public final void file_settings(final ActionEvent event)
	{
		GUIUtil.createNewStage("Settings", new SettingsController(), I18N.createStringBinding("gui.settings.title"));
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void file_close(final ActionEvent event)
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
		LogItem.debug("Stage closed");
	}
	
	@FXML
	public final void vocab_add(final ActionEvent event)
	{
		final String				fxmlName	= "AddWords";
		final AddWordsController	awc			= new AddWordsController(this.cb_language_from.getSelectionModel().getSelectedItem(), this.cb_language_to.getSelectionModel().getSelectedItem());
		final StringBinding			title		= I18N.createStringBinding("gui.addwords.title");
		GUIUtil.createNewStage(fxmlName, awc, title);
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void vocab_clear(final ActionEvent event)
	{
		final Alert					alert	= new Alert(AlertType.WARNING, I18N.createStringBinding("gui.mainmenu.alert.clearvocabulary.first").get(), ButtonType.YES, ButtonType.NO);
		final Optional<ButtonType>	result	= alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES)
		{
			final Alert					confirm	= new Alert(AlertType.CONFIRMATION, I18N.createStringBinding("gui.mainmenu.alert.clearvocabulary.second").get(), ButtonType.YES, ButtonType.NO);
			final Optional<ButtonType>	result2	= confirm.showAndWait();
			if (result2.isPresent() && result2.get() == ButtonType.YES)
			{
				WordCheck.removeAllWordChecks();
				Trial.removeAllTrials();
				Translation.removeAllTranslations();
				Word.removeAllWords();
				Language.removeAllLanguages();
				LanguageComponent.repopulateAllLanguages();
				WordComponent.repopulateAllWords();
				TrialComponent.repopulateAllTrials();
			}
			LogItem.info("Cleared the entire database");
		}
		else
		{
			LogItem.debug("Aborted clearing the database");
		}
	}
	
	@FXML
	public final void vocab_shuffle(final ActionEvent event)
	{
		ObservableList<WordToLanguageView> words = FXCollections.observableArrayList(this.tv_vocabulary.getItems());
		ObservableList<WordToLanguageView> newWords = FXCollections.observableArrayList();
		Random rand = new Random();
		while(!words.isEmpty())
		{
			newWords.add(words.remove(rand.nextInt(words.size())));
		}
		this.tv_vocabulary.setItems(newWords);
	}
	
	@FXML
	public final void vocab_languages(final ActionEvent event)
	{
		final String					fxmlName	= "ManageLanguages";
		final ManageLanguagesController	mlc			= new ManageLanguagesController();
		final StringBinding				title		= I18N.createStringBinding("gui.languages.title");
		GUIUtil.createNewStage(fxmlName, mlc, title);
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void trial_list(final ActionEvent event)
	{
		final TrialListController	tlc		= new TrialListController(this.cb_language_from.getValue(), this.cb_language_to.getValue());
		final StringBinding			title	= I18N.createStringBinding("gui.triallist.title");
		GUIUtil.createNewStage("TrialList", tlc, title);
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void trial_all(final ActionEvent event)
	{
		final List<Word> trialWords = new ArrayList<>();
		this.tv_vocabulary.getItems().forEach(w -> trialWords.add(Word.get(w.getWord_id())));
		this.createTrial(trialWords);
	}
	
	@FXML
	public final void trial_selected(final ActionEvent event)
	{
		final List<Word> trialWords = new ArrayList<>();
		this.tv_vocabulary.getSelectionModel().getSelectedItems().forEach(w -> trialWords.add(Word.get(w.getWord_id())));
		this.createTrial(trialWords);
	}
	
	@FXML
	public final void trial_failed(final ActionEvent event)
	{
		final List<Word>	trialWords	= new ArrayList<>();
		final Language		l_from		= this.cb_language_from.getValue();
		final Language		l_to		= this.cb_language_to.getValue();
		if (l_from == null || l_to == null)
		{
			return;
		}
		final List<Word> words = l_from.getWords();
		for (Word w : words)
		{
			final List<WordCheck>	wordchecks	= w.getWordChecks(l_to);
			LocalDateTime			datetime	= null;
			WordCheck				latestCheck	= null;
			for (final WordCheck wc : wordchecks)
			{
				final Trial t = wc.getTrial();
				if (datetime == null || datetime.isBefore(t.getDateTime()))
				{
					datetime	= t.getDateTime();
					latestCheck	= wc;
				}
			}
			if (latestCheck != null && !latestCheck.isCorrect().get())
			{
				trialWords.add(w);
			}
		}
		this.createTrial(trialWords);
	}
	
	@FXML
	public final void trial_custom(final ActionEvent event)
	{
		// TODO: implement custom trial configuration
		final Alert alert = new Alert(AlertType.INFORMATION, I18N.createStringBinding("root.notimplementedyet").get(), ButtonType.OK);
		alert.showAndWait();
	}
	
	@FXML
	public final void trial_reset(final ActionEvent event)
	{
		final Alert					alert	= new Alert(AlertType.WARNING, I18N.createStringBinding("gui.mainmenu.alert.erasetrials").get(), ButtonType.YES, ButtonType.NO);
		final Optional<ButtonType>	result	= alert.showAndWait();
		if (result.isPresent() && result.get() == ButtonType.YES)
		{
			VPS.execute("DELETE FROM wordcheck");
			VPS.execute("DELETE FROM trial");
			
			LogItem.info("Deleted all trial data");
		}
		else
		{
			LogItem.debug("Aborted deleting trial data");
		}
	}
	
	private final void createTrial(List<Word> trialWords)
	{
		if (trialWords.isEmpty())
		{
			final Alert alert = new Alert(AlertType.ERROR, I18N.createStringBinding("gui.mainmenu.alert.noapplicablewords").get(), ButtonType.OK);
			alert.showAndWait();
			LogItem.debug("No words were provided for the trial");
			return;
		}
		if (this.cmi_trial_randomize.isSelected())
		{
			final Random		rand			= new Random();
			final List<Word>	randomizedList	= new ArrayList<>();
			while (!trialWords.isEmpty())
			{
				int randIndex = rand.nextInt(trialWords.size());
				randomizedList.add(trialWords.remove(randIndex));
			}
			trialWords = randomizedList;
			LogItem.debug("Randomized trial words");
		}
		final Language			l_from	= this.cb_language_from.getSelectionModel().getSelectedItem();
		final Language			l_to	= this.cb_language_to.getSelectionModel().getSelectedItem();
		final TrialController	tc		= new TrialController(l_from, l_to, trialWords);
		final StringBinding		title	= I18N.createStringBinding("gui.trial.title");
		GUIUtil.createNewStage("Trial", tc, title);
		LogItem.debug("New stage created");
	}
	
	@FXML
	public final void change_language(final ActionEvent event)
	{
		Alert					alert	= new Alert(AlertType.WARNING, I18N.get("gui.mainmenu.alert.languagechange"), ButtonType.YES, ButtonType.NO);
		Optional<ButtonType>	result	= alert.showAndWait();
		if (!result.isPresent() || result.get() != ButtonType.YES)
		{
			LogItem.debug("Aborted changing language");
			return;
		}
		List<MenuItem>	items		= this.mn_languages.getItems();
		Locale			l_before	= C11N.getLocale();
		for (int i = 0; i < items.size(); i++)
		{
			MenuItem item = items.get(i);
			if (event.getSource() == item)
			{
				((CheckMenuItem) item).setSelected(true);
				switch (i)
				{
					case 0:
					{
						C11N.setLocale(Locale.ENGLISH);
						break;
					}
					case 1:
					{
						C11N.setLocale(Locale.GERMAN);
						break;
					}
					default:
					{
						C11N.setLocale(I18N.getDefaultLocale());
						break;
					}
				}
			}
			else
			{
				((CheckMenuItem) item).setSelected(false);
			}
		}
		Locale l_after = C11N.getLocale();
		VokAbfController.closeAll();
		try
		{
			final URL					url		= this.getClass().getResource("/com/visparu/vocabularytrial/gui/fxml/MainMenu.fxml");
			final FXMLLoader			loader	= new FXMLLoader(url);
			final MainMenuController	mmc		= new MainMenuController(this.stage);
			loader.setController(mmc);
			loader.setResources(I18N.getResources());
			final Parent	root	= loader.load();
			final Scene		scene	= new Scene(root);
			this.stage.setScene(scene);
			this.stage.titleProperty().bind(I18N.createStringBinding("gui.mainmenu.title"));
			this.stage.show();
			LogItem.debug("Reinitialized main menu");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		LogItem.info("Changed language from " + l_before.getDisplayLanguage() + " to " + l_after.getDisplayLanguage());
	}
	
	@FXML
	public final void debug_openlog(final ActionEvent event)
	{
		GUIUtil.createNewStage("Log", new LogController(), I18N.createStringBinding("gui.log.title"));
	}
	
	@FXML
	public final void debug_fillrandomly(final ActionEvent event)
	{
		Debug.debug_fillRandomly();
		this.repopulateLanguages_from();
		this.cb_language_from.getSelectionModel().select(0);
		this.repopulateLanguages_to();
		this.cb_language_to.getSelectionModel().select(0);
		this.repopulateWords();
	}
	
	@FXML
	public final void help_about(final ActionEvent event)
	{
		final StringBinding title = I18N.createStringBinding("gui.about.title");
		GUIUtil.createNewStage("About", new AboutController(), title);
		LogItem.debug("New stage created");
	}
}
