/*
 * Copyright (C) 2013 Marc Lester Tan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sap.sapfiledepotandroid;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.sap.sapfiledepotandroid.data.LocalDocument;
import com.sap.sapfiledepotandroid.util.Utils;

public class DetailsActivity extends FragmentActivity {
	private ImageView icon;
	private TextView name;
	private TextView mimeType;
	private TextView size;
	private TextView dateCreated;
	private TextView lastModifiedDate;
	private DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
	private NumberFormat nFormat = NumberFormat.getNumberInstance();

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.details);
		LocalDocument doc  = (LocalDocument) getIntent().getSerializableExtra("data");
		nFormat.setMaximumFractionDigits(2);
		
		icon = (ImageView)findViewById(R.id.icon);
		name = (TextView) findViewById(R.id.name);
		mimeType = (TextView) findViewById(R.id.mimeType);
		size = (TextView) findViewById(R.id.size);
		dateCreated = (TextView) findViewById(R.id.dateCreated);
		lastModifiedDate = (TextView) findViewById(R.id.lastModifiedDate);
		
		icon.setImageResource(Utils.getIcon(doc.getContentStreamMimeType()));
		name.setText(doc.getName());
		mimeType.setText(doc.getContentStreamMimeType());
		
		String unit = "b";
		long length = doc.getContentStreamLength();
		String val = nFormat.format(length);
		
		if(length > 100000000){
			float temp = length / 100000000.0f;
			val = nFormat.format(temp);
			unit = "Gb";
		}else if(length > 100000){
			float temp = length / 100000.0f;
			val = nFormat.format(temp);
			unit = "Mb";
		}else if(length > 1000.0){
			float temp = length / 1000.0f;
			val = nFormat.format(temp);
			unit = "Kb";
		}
		
		
		
		size.setText(val + " " + unit);
		dateCreated.setText(sdf.format(doc.getCreationDate()));
		lastModifiedDate.setText(sdf.format(doc.getLastModificationDate()));
		
	}
}
