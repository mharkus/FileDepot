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

package com.sap.sapfiledepotandroid.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ContentStream;

import com.sap.sapfiledepotandroid.R;

public class Utils {
	public static <T> List<T> copyIterator(Iterator<T> iter) {
		List<T> copy = new ArrayList<T>();
		while (iter.hasNext())
			copy.add(iter.next());
		return copy;
	}

	public static String getContentAsString(ContentStream stream) throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader reader = new InputStreamReader(stream.getStream());

		try {

			int b;
			while ((b = reader.read()) != -1) {
				sb.append((char) b);
			}
		} finally {
			reader.close();
		}

		return sb.toString();
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String result = convertStreamToString(fin);
		fin.close();
		return result;
	}

	public static int getIcon(String mimeType) {
		int drawable = 0;

		if (mimeType == null) {
			drawable = R.drawable.folder;
		} else if (mimeType.equals("image/jpg") || mimeType.equals("image/jpeg")) {
			drawable = R.drawable.jpeg;
		} else if (mimeType.equals("image/gif")) {
			drawable = R.drawable.gif;
		} else if (mimeType.equals("image/png")) {
			drawable = R.drawable.imgpng;
		} else if (mimeType.equals("audio/ogg")) {
			drawable = R.drawable.ogg;
		} else if (mimeType.equals("video/mp4")) {
			drawable = R.drawable.mp4;
		} else if (mimeType.equals("application/vnd.android.package-archive")) {
			drawable = R.drawable.apk;
		} else if (mimeType.equals("application/pdf")) {
			drawable = R.drawable.pdf;
		} else if (mimeType.equals("text/plain")) {
			drawable = R.drawable.text;
		} else if (mimeType.equals("text/xml")) {
			drawable = R.drawable.xml;
		} else if (mimeType.equals("application/zip") || mimeType.equals("application/x-zip") || mimeType.equals("application/x-zip-compressed") || mimeType.equals("application/octet-stream") || mimeType.equals("application/x-compress") || mimeType.equals("application/x-compressed")) {
			drawable = R.drawable.zip;
		}

		return drawable;
	}
}
