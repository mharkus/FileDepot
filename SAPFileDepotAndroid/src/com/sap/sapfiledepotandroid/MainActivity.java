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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sap.sapfiledepotandroid.data.LocalDocument;
import com.sap.sapfiledepotandroid.util.CMISUtil;
import com.sap.sapfiledepotandroid.util.RepositoryNotFoundException;
import com.sap.sapfiledepotandroid.util.Utils;

public class MainActivity extends FragmentActivity implements android.view.View.OnClickListener {

	private static final int SELECT_FILE_REQUEST = 0x000001;
	private static final String HANA_CLOUD_ENDPOINT = "/SAPFileDepot/cmis/atom/";

	private static final String TAG = MainActivity.class.getName();
	private Session session;
	private Folder currentFolder;
	private GridView items;
	private LinearLayout navigation;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.newFolder).setOnClickListener(this);
		findViewById(R.id.newDocument).setOnClickListener(this);
		findViewById(R.id.purge).setOnClickListener(this);

		progress = (ProgressBar) findViewById(R.id.progress);
		navigation = (LinearLayout) findViewById(R.id.nav);

		items = (GridView) findViewById(R.id.items);
		items.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				return false;
			}
		});

		items.setOnItemClickListener(new OnItemClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
			 * .widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				CmisObject item = (CmisObject) ((BaseAdapter) arg0.getAdapter()).getItem(arg2);
				if (item.getBaseTypeId().equals(BaseTypeId.CMIS_FOLDER)) {
					currentFolder = (Folder) item;
					addNavigation(currentFolder);
					new RefreshListTask().execute();
				} else {
					new GetCMISObjectContentsTask().execute(item.getId(), item.getName());
				}

			}
		});
		
		bindLogout();
		registerForContextMenu(items);

		new LoginTask().execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Actions");
		AdapterContextMenuInfo cmi = (AdapterContextMenuInfo) menuInfo;
		menu.add(1, cmi.position, 0, "Delete");
		menu.add(2, cmi.position, 0, "Properties");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = item.getGroupId();
		CmisObject obj = (CmisObject) items.getItemAtPosition(item.getItemId());

		switch (id) {
		case 1: {
			deleteObject(obj);
			break;
		}
		case 2: {
			Intent intent = new Intent(this, DetailsActivity.class);
			intent.putExtra("data", new LocalDocument(obj));
			startActivity(intent);
			overridePendingTransition(R.anim.activityincoming, R.anim.activityoutgoing);
			break;
		}
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * @param itemAtPosition
	 */
	private void deleteObject(CmisObject itemAtPosition) {
		new DeleteCMISObjectTask().execute(itemAtPosition);

	}

	class DeleteCMISObjectTask extends AsyncTask<CmisObject, Void, Void> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(CmisObject... params) {
			CmisObject item = params[0];
			session.delete(item, true);
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			toggleProgressBar(false);
			new RefreshListTask().execute();
		}

	}

	class GetCMISObjectContentsTask extends AsyncTask<String, String, File> {

		private String fileName;
		private String mimeType;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected File doInBackground(String... params) {
			String id = params[0];

			Document doc = (Document) session.getObject(id);

			ContentStream contentStream = doc.getContentStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			if (contentStream != null) {
				this.fileName = contentStream.getFileName();
				this.mimeType = contentStream.getMimeType();

				try {

					BufferedInputStream reader = new BufferedInputStream(contentStream.getStream());
					byte[] buff = new byte[4 * 1024];
					int c = -1;
					while ((c = reader.read(buff)) != -1) {
						baos.write(buff, 0, c);
					}

					reader.close();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			File cacheDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
			File tempFile = new File(cacheDir, this.fileName);

			try {

				FileOutputStream fos = new FileOutputStream(tempFile);
				fos.write(baos.toByteArray());
				fos.close();

			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return tempFile;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(File result) {
			toggleProgressBar(false);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(result), mimeType);

			startActivity(intent);

		}
	}

	class ItemAdapter extends BaseAdapter {

		private Context context;
		private List<CmisObject> items;

		/**
		 * 
		 */
		public ItemAdapter(Context context, List<CmisObject> items) {
			this.context = context;

			sortByName(items);
			this.items = items;
		}

		private void sortByName(List<CmisObject> items) {
			Collections.sort(items, new Comparator<CmisObject>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.Comparator#compare(java.lang.Object,
				 * java.lang.Object)
				 */
				@Override
				public int compare(CmisObject lhs, CmisObject rhs) {
					return lhs.getName().compareToIgnoreCase(rhs.getName());
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return items.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.itemview, null);
			}

			CmisObject item = (CmisObject) getItem(position);

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			TextView tv = (TextView) convertView.findViewById(R.id.name);

			String mimeType = null;

			Property<String> p = item.getProperty("cmis:contentStreamMimeType");
			if (p != null) {
				mimeType = p.getValueAsString();
			}

			icon.setImageResource(Utils.getIcon(mimeType));
			tv.setText(item.getName());

			return convertView;
		}

	}

	class LoginTask extends AsyncTask<String, Void, Folder> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Folder doInBackground(String... params) {
			try { 
				String url = "https://".concat("sapfiledepot").concat(NetWeaverCloudConfig.getInstance().getAccount()).concat("."+NetWeaverCloudConfig.getInstance().getHost()).concat(HANA_CLOUD_ENDPOINT);
				Log.d(TAG, "Logging in using : " + url);
				session = CMISUtil.login(NetWeaverCloudConfig.getInstance().getUsername(), NetWeaverCloudConfig.getInstance().getPassword(), url);
			} catch (RepositoryNotFoundException e) {
				e.printStackTrace();
			}

			return session.getRootFolder();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Folder result) {
			currentFolder = result;
			addNavigation(currentFolder);
			toggleProgressBar(false);
			new RefreshListTask().execute();

		}
	}

	class UploadDocumentTask extends AsyncTask<String, Void, CmisObject> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected CmisObject doInBackground(String... params) {

			String fileName = params[0];
			String mimeType = params[1];
			String filePath = params[2];

			ContentStream contentStream = null;
			Document doc = null;

			try {
				contentStream = CMISUtil.createContentStream(session, fileName, mimeType, new File(filePath));
				doc = CMISUtil.createContent(session, currentFolder, fileName, contentStream);

			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}

			return doc;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(CmisObject result) {
			toggleProgressBar(false);
			new RefreshListTask().execute();

		}
	}

	class CreateFolderTask extends AsyncTask<String, Void, Folder> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Folder doInBackground(String... params) {
			String folderName = params[0];
			return CMISUtil.createFolder(currentFolder, folderName);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Folder result) {
			toggleProgressBar(false);
			new RefreshListTask().execute();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_FILE_REQUEST) {
			if (resultCode == RESULT_OK) {
				String filePath = data.getData().getPath();

				String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
				String ext = filePath.substring(filePath.lastIndexOf("."));
				String mimeType = MimeTypes.getMIMEType(ext);

				new UploadDocumentTask().execute(fileName, mimeType, filePath);
			}
		}
	}

	class RefreshListTask extends AsyncTask<Void, Void, List<CmisObject>> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected List<CmisObject> doInBackground(Void... params) {
			return Utils.copyIterator(currentFolder.getChildren().iterator());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<CmisObject> result) {
			items.setAdapter(new ItemAdapter(MainActivity.this, result));
			toggleProgressBar(false);
		}
	}

	class PurgeRootFolderTask extends AsyncTask<Void, Void, Void> {
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			Iterator<CmisObject> children = currentFolder.getChildren().iterator();
			while (children.hasNext()) {
				children.next().delete(true);
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			toggleProgressBar(false);
			new RefreshListTask().execute();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (currentFolder.isRootFolder()) {
			super.onBackPressed();
		} else {
			new GetParentFolderTask().execute();
		}

	}

	class GetParentFolderTask extends AsyncTask<Void, Void, Folder> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			toggleProgressBar(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Folder doInBackground(Void... params) {
			return currentFolder.getFolderParent();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Folder result) {
			toggleProgressBar(false);
			currentFolder = result;
			removeNavigation(currentFolder);
			new RefreshListTask().execute();
		}
	}

	private void addNavigation(Folder folder) {
		Button button = new Button(this);
		button.setPadding(12, 12, 12, 12);
		button.setTextColor(Color.WHITE);
		button.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(-18, 0, 0, 0);

		button.setTextSize(12);
		if (folder.isRootFolder()) {
			button.setBackgroundResource(R.drawable.homenav);
			button.setText("Home");
		} else {
			button.setBackgroundResource(R.drawable.navitem);
			button.setText(folder.getName());
		}

		button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		button.setTag(folder);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				currentFolder = (Folder) v.getTag();
				removeNavigation(currentFolder);
				new RefreshListTask().execute();
			}
		});
		navigation.addView(button, layoutParams);
	}

	public void removeNavigation(Folder currentFolder) {
		int childCount = navigation.getChildCount();
		int index = 0;
		for (int i = childCount - 1; i >= 0; i--) {
			View child = navigation.getChildAt(i);
			if (((Folder) child.getTag()).getId().equals(currentFolder.getId())) {
				index = i;
				break;
			}
		}
		navigation.removeViews(index + 1, childCount - index - 1);

	}

	private void toggleProgressBar(boolean visible) {
		progress.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.newFolder: {
			View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.createfolderview, null);
			final EditText folderName = (EditText) view.findViewById(R.id.folderName);

			new AlertDialog.Builder(this).setTitle("Folder Name").setView(view).setPositiveButton("Create", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new CreateFolderTask().execute(folderName.getText().toString());
				}
			}).setNegativeButton("Cancel", null).create().show();

			break;
		}
		case R.id.newDocument: {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent, SELECT_FILE_REQUEST);
			break;
		}
		case R.id.purge: {
			new PurgeRootFolderTask().execute();
			break;
		}
		}

	}
	
	
	protected void bindLogout() {
		ImageView logout = (ImageView) findViewById(R.id.logout);
		logout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.ModifiedDialog)).setTitle("Confirmation").setMessage("Are you sure you want to logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
						Editor edit = pref.edit();
						edit.remove("valid");
						edit.commit();

						Intent intent = new Intent(getBaseContext(), LoginActivity.class);
						startActivity(intent);

						finish();

					}
				}).setNegativeButton("No", null).create().show();

			}
		});
	}
}