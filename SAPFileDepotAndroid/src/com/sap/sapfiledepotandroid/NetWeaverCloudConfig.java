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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NetWeaverCloudConfig {
	private static NetWeaverCloudConfig instance;

	private Context context;
	private String host;
	private String username;
	private String password;
	private String account;

	public static synchronized NetWeaverCloudConfig getInstance() {
		if (instance == null) {
			instance = new NetWeaverCloudConfig();
		}

		return instance;
	}

	public void init(Context context) {
		this.context = context;
		SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
		setUsername(pref.getString("username", ""));
		setPassword(pref.getString("password", ""));
		setAccount(pref.getString("account", ""));
		setHost(pref.getString("host", ""));
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void save() {
		Editor editor = context.getSharedPreferences("pref", Context.MODE_PRIVATE).edit();
		editor.putString("host", host);
		editor.putString("username", username);
		editor.putString("password", password);
		editor.putString("account", account);
		editor.commit();
	}

}
