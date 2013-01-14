/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2012  Andrey Novikov <http://andreynovikov.info/>
 *
 * This file is part of Androzic application.
 *
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Androzic.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.androzic.plugin.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import com.androzic.util.CoordinateParser;

public class SMSReceiver extends BroadcastReceiver
{
	private static final String TAG = "SMSReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle extras = intent.getExtras();
		if (extras == null)
			return;

		Log.d(TAG, "SMS received");

		String sender = "";
		String title = "";
		double coords[] = null;

		Object[] pdus = (Object[]) extras.get("pdus");
		for (int i = 0; i < pdus.length; i++)
		{
			SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[i]);
			String text = msg.getMessageBody();
			if (text == null)
				continue;
			if (text.contains("@"))
			{
				int idx = text.indexOf("@");
				title = text.substring(0, idx).trim();
				text = text.substring(idx + 1, text.length()).trim();
			}
			coords = CoordinateParser.parse(text);
			if (!Double.isNaN(coords[0]) && !Double.isNaN(coords[1]))
			{
				sender = msg.getOriginatingAddress();
				Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender));
				String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };
				// Query the filter URI
				Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
				if (cursor != null)
				{
					if (cursor.moveToFirst())
						sender = cursor.getString(0);
					cursor.close();
				}
			}
		}
		if (coords != null && !Double.isNaN(coords[0]) && !Double.isNaN(coords[1]))
		{
			Log.d(TAG, "Passing coordinates to Androzic");
			Intent i = new Intent("com.androzic.COORDINATES_RECEIVED");
			i.putExtra("title", title);
			i.putExtra("sender", sender);
			i.putExtra("lat", coords[0]);
			i.putExtra("lon", coords[1]);
			context.sendBroadcast(i);
		}
	}
}
