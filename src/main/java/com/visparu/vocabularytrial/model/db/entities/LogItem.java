package com.visparu.vocabularytrial.model.db.entities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.visparu.vocabularytrial.gui.interfaces.LogComponent;
import com.visparu.vocabularytrial.model.db.VPS;
import com.visparu.vocabularytrial.model.log.Severity;
import com.visparu.vocabularytrial.util.C11N;
import com.visparu.vocabularytrial.util.ConvertUtil;

public final class LogItem
{
	private static final Map<Integer, LogItem> cache = new HashMap<>();
	
	private static Integer			session_log_id				= -1;
	private static boolean			initialized					= false;
	private static List<LogItem>	preinitialization_logitems	= new ArrayList<>();
	private static Severity			logging_level;
	
	private Integer			logitem_id;
	private Integer			log_id;
	private Severity		severity;
	private LocalDateTime	datetime;
	private String			threadName;
	private String			function;
	private String			message;
	private String			description;
	
	static
	{
		LogItem.logging_level = C11N.getLoggingLevel();
	}
	
	private LogItem(final Integer logitem_id, final Integer log_id, final Severity severity, final LocalDateTime datetime, final String threadName, final String function, final String message,
		final String description)
	{
		this.logitem_id		= logitem_id;
		this.log_id			= log_id;
		this.severity		= severity;
		this.datetime		= datetime;
		this.threadName		= threadName;
		this.function		= function;
		this.message		= message;
		this.description	= description;
	}
	
	public final static void createTable()
	{
		String query = "CREATE TABLE IF NOT EXISTS logitem (" + "logitem_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "log_id INTEGER, " + "severity VARCHAR(20), " + "datetime VARCHAR(23), "
			+ "threadname VARCHAR(100), " + "function VARCHAR(100), " + "message VARCHAR(200), " + "description VARCHAR(500)" + ")";
		
		VPS.execute(query);
	}
	
	public final static void initializeNewLogSession()
	{
		final String query = "SELECT max(log_id) " + "FROM logitem";
		
		try (VPS vps = new VPS(query); ResultSet rs = vps.query())
		{
			final Integer	next_log_id;
			final Integer	max_log_id	= rs.getInt(1);
			if (max_log_id < 1)
			{
				next_log_id = 1;
			}
			else
			{
				next_log_id = max_log_id + 1;
			}
			
			LogItem.session_log_id	= next_log_id;
			LogItem.initialized		= true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public final static void clearCache()
	{
		LogItem.cache.clear();
	}
	
	public final static LogItem get(final Integer logitem_id)
	{
		if (LogItem.cache.containsKey(logitem_id))
		{
			return LogItem.cache.get(logitem_id);
		}
		return LogItem.readEntity(logitem_id);
	}
	
	public final static LogItem debug(String message)
	{
		return LogItem.createLogItem(Severity.DEBUG, message);
	}
	
	public final static LogItem debug(String message, String description)
	{
		return LogItem.createLogItem(Severity.DEBUG, message, description);
	}
	
	public final static LogItem info(String message)
	{
		return LogItem.createLogItem(Severity.INFO, message);
	}
	
	public final static LogItem info(String message, String description)
	{
		return LogItem.createLogItem(Severity.INFO, message, description);
	}
	
	public final static LogItem warning(String message)
	{
		return LogItem.createLogItem(Severity.WARNING, message);
	}
	
	public final static LogItem warning(String message, String description)
	{
		return LogItem.createLogItem(Severity.WARNING, message, description);
	}
	
	public final static LogItem error(String message)
	{
		return LogItem.createLogItem(Severity.ERROR, message);
	}
	
	public final static LogItem error(String message, String description)
	{
		return LogItem.createLogItem(Severity.ERROR, message, description);
	}
	
	public final static LogItem critical(String message)
	{
		return LogItem.createLogItem(Severity.CRITICAL, message);
	}
	
	public final static LogItem critical(String message, String description)
	{
		return LogItem.createLogItem(Severity.CRITICAL, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final String message)
	{
		final String description = new String(message);
		return LogItem.createLogItem(severity, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final String message, final String description)
	{
		final LocalDateTime			datetime	= LocalDateTime.now();
		final String				threadname	= Thread.currentThread().getName();
		final StackTraceElement[]	stackTrace	= Thread.currentThread().getStackTrace();
		String						function	= "n/A";
		for (int i = 1; i < stackTrace.length; i++)
		{
			final StackTraceElement ste = stackTrace[i];
			if (!ste.getClassName().contentEquals(LogItem.class.getName()))
			{
				function = String.format("%s.%s:%d", ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
				break;
			}
		}
		return LogItem.createLogItem(severity, datetime, threadname, function, message, description);
	}
	
	public final static LogItem createLogItem(final Severity severity, final LocalDateTime datetime, final String threadname, final String function, final String message, final String description)
	{
		final Integer	logging_level		= severity.ordinal();
		final Integer	min_logging_level	= C11N.getLoggingLevel().ordinal();
		if (logging_level < min_logging_level)
		{
			return null;
		}
		final LogItem li = new LogItem(-1, LogItem.session_log_id, severity, datetime, threadname, function, message, description);
		LogItem.preinitialization_logitems.add(li);
		if (LogItem.initialized)
		{
			while (!LogItem.preinitialization_logitems.isEmpty())
			{
				if (LogItem.preinitialization_logitems.get(0).getLog_id().equals(-1))
				{
					LogItem lit = LogItem.preinitialization_logitems.get(0);
					lit.log_id = LogItem.session_log_id;
				}
				final LogItem	lip			= LogItem.preinitialization_logitems.remove(0);
				final Integer	logitem_id	= LogItem.writeEntity(lip);
				li.setLogitem_id(logitem_id);
				LogItem.cache.put(logitem_id, lip);
				LogComponent.repopulateAllLogs();
			}
		}
		return li;
	}
	
	public final static void removeLogItem(final Integer logitem_id)
	{
		final String query = "DELETE FROM logitem " + "WHERE logitem_id = ?";
		
		VPS.execute(query, logitem_id);
		LogItem.cache.remove(logitem_id);
		
		LogComponent.repopulateAllLogs();
	}
	
	public final static void removeAllLogItems()
	{
		String query = "DELETE FROM logitem";
		
		VPS.execute(query);
		LogItem.clearCache();
		
		LogComponent.repopulateAllLogs();
	}
	
	private final static LogItem readEntity(final Integer logitem_id)
	{
		final String query = "SELECT * " + "FROM logitem " + "WHERE logitem_id = ?";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query(logitem_id))
		{
			if (rs.next())
			{
				final Integer		log_id		= rs.getInt("log_id");
				final Severity		severity	= Severity.values()[rs.getInt("severity")];
				final LocalDateTime	datetime	= LocalDateTime.parse(rs.getString("datetime"));
				final String		threadName	= rs.getString("threadname");
				final String		function	= rs.getString("function");
				final String		message		= rs.getString("message");
				final String		description	= rs.getString("description");
				final LogItem		li			= new LogItem(logitem_id, log_id, severity, datetime, threadName, function, message, description);
				LogItem.cache.put(logitem_id, li);
				
				return li;
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private final static Integer writeEntity(final LogItem logitem)
	{
		final String query = "INSERT INTO logitem(log_id, severity, datetime, threadname, function, message, description) " + "VALUES(?, ?, ?, ?, ?, ?, ?)";
		
		final Integer	log_id		= logitem.getLog_id();
		final Integer	severity	= logitem.getSeverity().ordinal();
		final String	datetime	= ConvertUtil.convertDateToString(logitem.getDatetime());
		final String	threadname	= logitem.getThreadName();
		final String	function	= logitem.getFunction();
		final String	message		= logitem.getMessage();
		final String	description	= logitem.getDescription();
		
		List<Integer> keys = VPS.execute(query, log_id, severity, datetime, threadname, function, message, description);
		if (keys.isEmpty())
		{
			return -1;
		}
		final Integer logitem_id = keys.get(0);
		
		return logitem_id;
	}
	
	public static final Integer getSessionLog_id()
	{
		return LogItem.session_log_id;
	}
	
	public static final Severity getLoggingLevel()
	{
		return LogItem.logging_level;
	}
	
	public static final void setLoggingLevel(Severity severity)
	{
		LogItem.logging_level = severity;
	}
	
	public final Integer getLogitem_id()
	{
		return this.logitem_id;
	}
	
	private final void setLogitem_id(final Integer logitem_id)
	{
		this.logitem_id = logitem_id;
	}
	
	public final Integer getLog_id()
	{
		return this.log_id;
	}
	
	public final Severity getSeverity()
	{
		return this.severity;
	}
	
	public final void setSeverity(Severity severity)
	{
		final String query = "UPDATE logitem " + "SET severity = ? " + "WHERE logitem_id = ?";
		
		final Integer severity_i = severity.ordinal();
		
		VPS.execute(query, severity_i, this.logitem_id);
	}
	
	public final LocalDateTime getDatetime()
	{
		return this.datetime;
	}
	
	public final void setDatetime(LocalDateTime datetime)
	{
		final String query = "UPDATE logitem " + "SET datetime = ? " + "WHERE logitem_id = ?";
		
		final String datetime_s = ConvertUtil.convertDateToString(datetime);
		
		VPS.execute(query, datetime_s, this.logitem_id);
	}
	
	public final String getThreadName()
	{
		return this.threadName;
	}
	
	public final void setThreadName(String threadName)
	{
		final String query = "UPDATE logitem " + "SET threadname = ? " + "WHERE logitem_id = ?";
		
		VPS.execute(query, threadName, this.logitem_id);
	}
	
	public final String getFunction()
	{
		return function;
	}
	
	public final void setFunction(String function)
	{
		final String query = "UPDATE logitem " + "SET function = ? " + "WHERE logitem_id = ?";
		
		VPS.execute(query, function, this.logitem_id);
	}
	
	public final String getMessage()
	{
		return this.message;
	}
	
	public final void setMessage(String message)
	{
		final String query = "UPDATE logitem " + "SET message = ? " + "WHERE logitem_id = ?";
		
		VPS.execute(query, message, this.logitem_id);
	}
	
	public final String getDescription()
	{
		return this.description;
	}
	
	public final void setDescription(String description)
	{
		final String query = "UPDATE logitem " + "SET description = ? " + "WHERE logitem_id = ?";
		
		VPS.execute(query, description, this.logitem_id);
	}
	
	public static final List<Integer> getAllLogIds()
	{
		final String query = "SELECT DISTINCT log_id " + "FROM logitem";
		
		try (final VPS vps = new VPS(query); final ResultSet rs = vps.query())
		{
			final List<Integer> log_ids = new ArrayList<>();
			while (rs.next())
			{
				log_ids.add(rs.getInt("log_id"));
			}
			return log_ids;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public static final List<LogItem> getAllLogItemsForLog(Integer log_id)
	{
		String query = "SELECT logitem_id " + "FROM logitem " + "WHERE log_id = ?";
		
		try (final VPS vps = new VPS(query); ResultSet rs = vps.query(log_id))
		{
			List<LogItem> logitems = new ArrayList<>();
			while (rs.next())
			{
				Integer	logitem_id	= rs.getInt("logitem_id");
				LogItem	li			= LogItem.get(logitem_id);
				logitems.add(li);
			}
			return logitems;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public static final List<LogItem> getFilteredLogItems(Integer log_id, Severity min_severity, String thread, String function, String message, boolean description)
	{
		int					param_count	= 0;
		final StringJoiner	sj_filter	= new StringJoiner(" AND ");
		if (log_id != null)
		{
			sj_filter.add("log_id = ?");
			param_count++;
		}
		if (min_severity != null)
		{
			sj_filter.add("severity >= ?");
			param_count++;
		}
		if (thread != null)
		{
			sj_filter.add("threadname = ?");
			param_count++;
		}
		if (function != null)
		{
			sj_filter.add("function = ?");
			param_count++;
		}
		if (message != null)
		{
			if (description)
			{
				sj_filter.add("(message LIKE ? OR description LIKE ?)");
				param_count += 2;
			}
			else
			{
				sj_filter.add("message LIKE ?");
				param_count++;
			}
		}
		String query = "SELECT logitem_id " + "FROM logitem " + "WHERE " + sj_filter.toString();
		
		Object[]	args	= new Object[param_count];
		int			index	= 0;
		if (log_id != null)
		{
			args[index++] = log_id;
		}
		if (min_severity != null)
		{
			args[index++] = min_severity.ordinal();
		}
		if (thread != null)
		{
			args[index++] = thread;
		}
		if (function != null)
		{
			args[index++] = function;
		}
		if (message != null)
		{
			args[index++] = "%" + message + "%";
			if (description)
			{
				args[index++] = "%" + message + "%";
			}
		}
		
		try (final VPS vps = new VPS(query); ResultSet rs = vps.query(args))
		{
			final List<LogItem> logitems = new ArrayList<>();
			while (rs.next())
			{
				Integer	logitem_id	= rs.getInt("logitem_id");
				LogItem	li			= LogItem.get(logitem_id);
				logitems.add(li);
			}
			return logitems;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
		
	}
}
