package com.visparu.vocabularytrial.model.views;

import com.visparu.vocabularytrial.model.db.entities.Trial;
import com.visparu.vocabularytrial.util.ConvertUtil;

public final class TrialView
{
	private final Trial trial;
	
	public TrialView(final Trial trial)
	{
		this.trial = trial;
	}
	
	public final Integer getTrial_id()
	{
		Integer trial_id = this.trial.getTrial_id();
		return trial_id;
	}
	
	public final String getDate()
	{
		String dateString = ConvertUtil.convertDateToReadableString(this.trial.getDateTime());
		return dateString;
	}
	
	public final String getCount()
	{
		String count = String.valueOf(this.trial.getWordChecks().size());
		return count;
	}
	
	public final String getCorrect()
	{
		String correct = String.valueOf(this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect().get()).count());
		return correct;
	}
	
	public final String getWrong()
	{
		String wrong = String.valueOf(this.trial.getWordChecks().stream().filter(wc -> !wc.isCorrect().get()).count());
		return wrong;
	}
	
	public final String getPercentage()
	{
		final long	correct	= this.trial.getWordChecks().stream().filter(wc -> wc.isCorrect().get()).count();
		final int	count	= this.trial.getWordChecks().size();
		String		perc	= String.format("%.2f", (double) correct / count);
		return perc;
	}
}
