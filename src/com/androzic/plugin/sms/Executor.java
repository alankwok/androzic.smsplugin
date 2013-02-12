package com.androzic.plugin.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Executor extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		String action = intent.getAction();
		if (action.equals("com.androzic.plugins.action.INITIALIZE"))
		{
			// Do nothing, we just have to wake up from stopped state
		}
	}
}
