package de.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;

import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.model.db.entities.Language;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.db.entities.Translation;
import de.visparu.vocabularytrial.model.db.entities.Trial;
import de.visparu.vocabularytrial.model.db.entities.Word;
import de.visparu.vocabularytrial.model.db.entities.WordCheck;
import de.visparu.vocabularytrial.util.GUIUtil;
import de.visparu.vocabularytrial.util.I18N;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public final class TrialController implements Initializable, VokAbfController
{
	@FXML
	private TextArea	ta_question;
	@FXML
	private TextArea	ta_answer;
	@FXML
	private TextArea	ta_solution;
	@FXML
	private Button		bt_correct;
	@FXML
	private Button		bt_wrong;
	@FXML
	private Button		bt_solution;
	
	private enum State
	{
		INIT, QUESTION, SOLUTION
	}
	
	private Stage				stage;
	private final Language		language_to;
	private final Trial			trial;
	private final List<Word>	words;
	private int					currentIndex	= 0;
	private State				currentState	= State.INIT;
	
	public TrialController(final Language language_from, final Language language_to, final List<Word> words)
	{
		this.language_to	= language_to;
		this.words			= words;
		this.trial			= Trial.createTrial(LocalDateTime.now(), language_from, language_to);
	}
	
	@Override
	public final void initialize(final URL location, final ResourceBundle resources)
	{
		LogItem.debug("Initializing new stage with TrialController");
		
		this.ta_answer.requestFocus();
		this.cycle(State.QUESTION);
		
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
		LogItem.debug("Stage closed");
	}
	
	@Override
	public final void closeRequest()
	{
		if (this.trial.getWordChecks().isEmpty())
		{
			return;
		}
		final TrialResultController	trc		= new TrialResultController(this.trial);
		final StringBinding			title	= I18N.createStringBinding("gui.result.title");
		GUIUtil.createNewStage("TrialResult", trc, title);
	}
	
	@FXML
	public final void exit(final ActionEvent event)
	{
		this.close();
	}
	
	@FXML
	public final void correct(final ActionEvent event)
	{
		final Word word = this.words.get(this.currentIndex);
		WordCheck.createWordCheck(word, this.trial, this.ta_answer.getText(), true);
		LogItem.debug("Created correct WordCheck for word " + word.getName());
		this.cycle(State.QUESTION);
	}
	
	@FXML
	public final void wrong(final ActionEvent event)
	{
		final Word word = this.words.get(this.currentIndex);
		WordCheck.createWordCheck(word, this.trial, this.ta_answer.getText(), false);
		LogItem.debug("Created wrong WordCheck for word " + word.getName());
		this.cycle(State.QUESTION);
	}
	
	@FXML
	public final void solution(final ActionEvent event)
	{
		LogItem.debug("Showing solution");
		this.cycle(State.SOLUTION);
	}
	
	@FXML
	public final void keyPressed(final KeyEvent event)
	{
		if (event.getCode() == KeyCode.ESCAPE)
		{
			this.close();
			return;
		}
		if (event.getCode() == KeyCode.UP)
		{
			this.solution(null);
			return;
		}
		if (event.getCode() == KeyCode.LEFT)
		{
			this.correct(null);
			return;
		}
		if (event.getCode() == KeyCode.RIGHT)
		{
			this.wrong(null);
			return;
		}
	}
	
	private final void cycle(final State nextState)
	{
		if (this.currentState == State.INIT)
		{
			LogItem.debug("Cycling from INIT to QUESTION");
			this.currentState = State.QUESTION;
			this.setQuestion(this.words.get(this.currentIndex));
			return;
		}
		LogItem.debug("Cycling from " + this.currentState.name() + " to " + nextState.name());
		switch (nextState)
		{
			case QUESTION:
			{
				this.currentIndex++;
				if (this.currentIndex >= this.words.size())
				{
					LogItem.info("Trial completed");
					this.exit(null);
					return;
				}
				this.ta_answer.setEditable(true);
				this.bt_solution.setDisable(false);
				this.setSolution(null);
				this.setQuestion(this.words.get(this.currentIndex));
				this.ta_answer.setText("");
				break;
			}
			case SOLUTION:
			{
				this.ta_answer.setEditable(false);
				this.bt_solution.setDisable(true);
				this.setSolution(this.words.get(this.currentIndex));
				break;
			}
			default:
			{
				throw new IllegalStateException();
			}
		}
		this.currentState = nextState;
		LogItem.debug("State switched");
	}
	
	private final void setQuestion(final Word question)
	{
		this.ta_question.setText(question.getName());
	}
	
	private final void setSolution(final Word question)
	{
		if (question == null)
		{
			this.ta_solution.setText("");
		}
		else
		{
			final List<Translation>	translations	= question.getTranslations(this.language_to);
			final StringJoiner		sj				= new StringJoiner("\n");
			for (int i = 0; i < translations.size(); i++)
			{
				final Translation t = translations.get(i);
				
				final String name;
				if (question.getWord_id().equals(t.getWord1_id()))
				{
					name = t.getWord2().getName();
				}
				else
				{
					name = t.getWord1().getName();
				}
				
				sj.add(name);
			}
			this.ta_solution.setText(sj.toString());
		}
	}
}
