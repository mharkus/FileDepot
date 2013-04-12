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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;




public class LoginActivity extends FragmentActivity {
	private EditText username;
	private EditText password;
	private EditText account;
	private Spinner host;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		account = (EditText) findViewById(R.id.account);
		host = (Spinner) findViewById(R.id.host);

		SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
		
		if(pref.contains("valid") && pref.getBoolean("valid", false)){
			NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
			config.init(this);
			showMain();
		}
		
		username.setText(pref.getString("username", ""));
		password.setText(pref.getString("password", ""));
		account.setText(pref.getString("account", ""));

		if (pref.getString("host", "").startsWith("hanatrial")) {
			host.setSelection(0);
		} else {
			host.setSelection(1);
		}

		Button loginButton = (Button) findViewById(R.id.login);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				validate();
			}
		});
	}

	/**
	 * 
	 */
	protected void validate() {

		if (username.getText().toString().length() == 0) {
			username.setError("This is required");
			return;
		}

		if (password.getText().toString().length() == 0) {
			password.setError("This is required");
			return;
		}

		if (account.getText().toString().length() == 0) {
			account.setError("This is required");
			return;
		}

		NetWeaverCloudConfig config = NetWeaverCloudConfig.getInstance();
		config.init(this);
		config.setAccount(account.getText().toString());
		config.setHost((String) host.getSelectedItem());
		config.setPassword(password.getText().toString());
		config.setUsername(username.getText().toString());
		config.save();

		showMain();
	}

	private void showMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
		finish();
	}

	
}
