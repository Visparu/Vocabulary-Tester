package de.visparu.vocabularytrial.gui.controllers;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import de.visparu.vocabularytrial.gui.interfaces.LogComponent;
import de.visparu.vocabularytrial.gui.interfaces.VokAbfController;
import de.visparu.vocabularytrial.model.db.VPS;
import de.visparu.vocabularytrial.model.db.entities.LogItem;
import de.visparu.vocabularytrial.model.log.Severity;
import de.visparu.vocabularytrial.model.views.LogItemView;
import de.visparu.vocabularytrial.util.GUIUtil;
import de.visparu.vocabularytrial.util.I18N;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

public final class LogController implements Initializable, VokAbfController, LogComponent
{
	@FXML
	private ComboBox<Severity>					cb_severity;
	@FXML
	private ComboBox<String>					cb_thread;
	@FXML
	private ComboBox<String>					cb_function;
	@FXML
	private TextField							tf_search;
	@FXML
	private CheckBox							cb_includedescription;
	@FXML
	private TableView<LogItemView>				tv_log;
	@FXML
	private TableColumn<LogItemView, String>	tc_time;
	@FXML
	private TableColumn<LogItemView, String>	tc_thread;
	@FXML
	private TableColumn<LogItemView, String>	tc_function;
	@FXML
	private TableColumn<LogItemView, String>	tc_message;
	
	private Stage stage;
	
	@Override
	public final void initialize(URL location, ResourceBundle resources)
	{
		this.cb_severity.getItems().addAll(Severity.values());
		this.cb_severity.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateThreads();
		});
		this.cb_thread.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.repopulateFunctions();
		});
		this.cb_function.getSelectionModel().selectedItemProperty().addListener(e ->
		{
			this.filter(null);
		});
		this.cb_severity.getSelectionModel().select(Severity.INFO);
		final Callback<TableColumn<LogItemView, String>, TableCell<LogItemView, String>> cb = (c ->
		{
			final TableCell<LogItemView, String> tc = new TableCell<>()
			{
				@Override
				protected void updateItem(String s, boolean empty)
				{
					super.updateItem(s, empty);
					if (s == null || empty)
					{
						this.setText(null);
						this.setBackground(null);
					}
					else
					{
						this.setText(s);
						final LogItemView liv = this.getTableRow().getItem();
						if (liv == null)
						{
							this.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
							this.setTextFill(Color.WHITE);
							return;
						}
						Severity	severity	= Severity.valueOf(liv.getSeverity());
						Color		bc			= Severity.getBackgroundColor(severity);
						Color		fc			= Severity.getForegroundColor(severity);
						
						this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
						if (this.isSelected())
						{
							this.setBackground(new Background(new BackgroundFill(Color.GOLD, CornerRadii.EMPTY, Insets.EMPTY)));
							this.setTextFill(Color.BLACK);
						}
						else
						{
							this.setBackground(new Background(new BackgroundFill(bc, CornerRadii.EMPTY, Insets.EMPTY)));
							this.setTextFill(fc);
						}
					}
				}
			};
			tc.setOnMouseClicked(e ->
			{
				if (tc == null || tc.isEmpty() || tc.getItem() == null)
				{
					return;
				}
				
				if (e.getClickCount() == 2)
				{
					this.openDetailView(tc.getTableRow().getItem());
				}
			});
			return tc;
		});
		this.tc_time.setCellFactory(cb);
		this.tc_thread.setCellFactory(cb);
		this.tc_function.setCellFactory(cb);
		this.tc_message.setCellFactory(cb);
		this.tc_time.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("datetime"));
		this.tc_thread.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("thread"));
		this.tc_function.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("function"));
		this.tc_message.setCellValueFactory(new PropertyValueFactory<LogItemView, String>("message"));
		this.repopulateLogs();
	}
	
	private final void repopulateThreads()
	{
		final String before = this.cb_thread.getSelectionModel().getSelectedItem();
		this.cb_thread.getItems().clear();
		
		final String	query_thread	= "SELECT DISTINCT threadname " + "FROM logitem " + "WHERE log_id = ? " + "AND severity >= ?";
		final Integer	log_id			= LogItem.getSessionLog_id();
		final Integer	severity		= this.cb_severity.getSelectionModel().getSelectedItem().ordinal();
		
		try (final VPS vps = new VPS(query_thread); final ResultSet rs = vps.query(log_id, severity))
		{
			while (rs.next())
			{
				this.cb_thread.getItems().add(rs.getString("threadname"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		this.cb_thread.getItems().sort(Comparator.comparing(String::toString));
		this.cb_thread.getItems().add(0, "All");
		if (this.cb_thread.getItems().contains(before))
		{
			this.cb_thread.getSelectionModel().select(before);
		}
		else
		{
			this.cb_thread.getSelectionModel().select(0);
		}
		this.repopulateFunctions();
	}
	
	private final void repopulateFunctions()
	{
		final String	before		= this.cb_function.getSelectionModel().getSelectedItem();
		String			threadname	= this.cb_thread.getSelectionModel().getSelectedItem();
		this.cb_function.getItems().clear();
		
		final String query_func;
		if (threadname == null || threadname.contentEquals("All"))
		{
			query_func = "SELECT DISTINCT function " + "FROM logitem " + "WHERE log_id = ? " + "AND severity >= ?";
		}
		else
		{
			query_func = "SELECT DISTINCT function " + "FROM logitem " + "WHERE log_id = ? " + "AND threadname = ? " + "AND severity >= ?";
		}
		
		int			log_id		= LogItem.getSessionLog_id();
		int			severity	= this.cb_severity.getSelectionModel().getSelectedItem().ordinal();
		Object[]	params;
		if (threadname == null || threadname.contentEquals("All"))
		{
			params		= new Object[2];
			params[0]	= log_id;
			params[1]	= severity;
		}
		else
		{
			params		= new Object[3];
			params[0]	= log_id;
			params[1]	= threadname;
			params[2]	= severity;
		}
		
		try (final VPS vps = new VPS(query_func); final ResultSet rs = vps.query(params))
		{
			while (rs.next())
			{
				this.cb_function.getItems().add(rs.getString("function"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		this.cb_function.getItems().sort(Comparator.comparing(String::toString));
		this.cb_function.getItems().add(0, "All");
		if (this.cb_function.getItems().contains(before))
		{
			this.cb_function.getSelectionModel().select(before);
		}
		else
		{
			this.cb_function.getSelectionModel().select(0);
		}
		this.filter(null);
	}
	
	private final void openDetailView(LogItemView liv)
	{
		final LogDetailController ldc = new LogDetailController(liv);
		GUIUtil.createNewStage("LogDetail", ldc, I18N.createStringBinding("gui.logdetail.title"));
	}
	
	@Override
	public final void repopulateLogs()
	{
		this.filter(null);
	}
	
	@Override
	public final void setStage(Stage stage)
	{
		this.stage = stage;
	}
	
	@Override
	public final void close()
	{
		this.stage.getOnCloseRequest().handle(null);
		this.stage.close();
	}
	
	@Override
	public final void closeRequest()
	{
		LogDetailController.instances.forEach(i -> i.close());
	}
	
	@FXML
	public final void filter(ActionEvent event)
	{
		Severity	severity	= this.cb_severity.getSelectionModel().getSelectedItem();
		String		thread		= this.cb_thread.getSelectionModel().getSelectedItem();
		if (thread != null && thread.equals("All"))
		{
			thread = null;
		}
		String function = this.cb_function.getSelectionModel().getSelectedItem();
		if (function != null && function.equals("All"))
		{
			function = null;
		}
		String message = this.tf_search.getText();
		if (message != null && message.isEmpty())
		{
			message = null;
		}
		boolean								description		= this.cb_includedescription.isSelected();
		final List<LogItem>					logitems		= LogItem.getFilteredLogItems(LogItem.getSessionLog_id(), severity, thread, function, message, description);
		final ObservableList<LogItemView>	logitemviews	= FXCollections.observableArrayList();
		logitems.forEach(li -> logitemviews.add(new LogItemView(li)));
		this.tv_log.getItems().removeAll(this.tv_log.getItems());
		logitemviews.forEach(liv -> this.tv_log.getItems().add(liv));
	}
	
	@FXML
	public final void searchInput(KeyEvent event)
	{
		this.filter(null);
	}
	
	@FXML
	public final void close(ActionEvent event)
	{
		this.close();
	}
}
