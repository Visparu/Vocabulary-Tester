package de.visparu.vocabularytrial.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VPS implements AutoCloseable
{
	
	private final PreparedStatement pstmt;
	
	public VPS(String query) throws SQLException
	{
		this.pstmt = Database.get().prepareStatement(query);
	}
	
	public static List<Integer> execute(String query, Object... params)
	{
		try (final VPS vps = new VPS(query))
		{
			return vps.execute(params);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<Integer> execute(Object... params)
	{
		try
		{
			Database.get().execute(this.pstmt, params);
			List<Integer> generatedKeys = new ArrayList<>();
			try (ResultSet rs = this.pstmt.getGeneratedKeys())
			{
				while (rs.next())
				{
					generatedKeys.add(rs.getInt(1));
				}
			}
			return generatedKeys;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public ResultSet query(Object... params)
	{
		try
		{
			return Database.get().executeQuery(this.pstmt, params);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public void close() throws SQLException
	{
		this.pstmt.close();
	}
	
}
