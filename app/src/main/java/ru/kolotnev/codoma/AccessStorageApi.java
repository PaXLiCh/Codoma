package ru.kolotnev.codoma;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Helpers to work with loadable data.
 */
public class AccessStorageApi {
	public static Bitmap loadPrescaledBitmap(String filename) throws IOException {
		// Facebook image size
		final int IMAGE_MAX_SIZE = 630;

		File file;
		FileInputStream fis;

		BitmapFactory.Options opts;
		int resizeScale;
		Bitmap bmp;

		file = new File(filename);

		// This bit determines only the width/height of the bitmap without loading the contents
		opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		fis = new FileInputStream(file);
		BitmapFactory.decodeStream(fis, null, opts);
		fis.close();

		// Find the correct scale value. It should be a power of 2
		resizeScale = 1;

		if (opts.outHeight > IMAGE_MAX_SIZE || opts.outWidth > IMAGE_MAX_SIZE) {
			resizeScale = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE / (double)Math.max(opts.outHeight, opts.outWidth)) / Math.log(0.5)));
		}

		// Load pre-scaled bitmap
		opts = new BitmapFactory.Options();
		opts.inSampleSize = resizeScale;
		fis = new FileInputStream(file);
		bmp = BitmapFactory.decodeStream(fis, null, opts);

		fis.close();

		return bmp;
	}

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context
	 * 		The context.
	 * @param uri
	 * 		The Uri to query.
	 */
	static String getPath(final Context context, final Uri uri) {
		String path = "";

		if (uri == null || uri.equals(Uri.EMPTY))
			return "";

		try {
			final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
			// DocumentProvider
			if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
				if (isTurboDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					path = "/" + split[1];
				} else if (isExternalStorageDocument(uri)) {
					// ExternalStorageProvider
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					if ("primary".equalsIgnoreCase(type)) {
						path = Environment.getExternalStorageDirectory() + "/" + split[1];
					}

					// TODO handle non-primary volumes
				} else if (isDownloadsDocument(uri)) {
					// DownloadsProvider

					final String id = DocumentsContract.getDocumentId(uri);
					final Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

					path = getDataColumn(context, contentUri, null, null);
				} else if (isMediaDocument(uri)) {
					// MediaProvider
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];

					Uri contentUri = null;
					if ("image".equals(type)) {
						contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}

					final String selection = "_id=?";
					final String[] selectionArgs = new String[] { split[1] };

					path = getDataColumn(context, contentUri, selection, selectionArgs);
				}
			} else if ("content".equalsIgnoreCase(uri.getScheme())) {
				// MediaStore (and general)
				path = getDataColumn(context, uri, null, null);
			} else if ("file".equalsIgnoreCase(uri.getScheme())) {
				// File
				path = uri.getPath();
			}
		} catch (Exception ex) {
			return "";
		}


		return path;
	}

	private static String getName(Context context, Uri uri) {
		if (uri == null || uri.equals(Uri.EMPTY))
			return "";

		String fileName = "";
		try {
			String scheme = uri.getScheme();
			if (scheme.equals("file")) {
				fileName = uri.getLastPathSegment();
			} else if (scheme.equals("content")) {
				String[] proj = { MediaStore.Images.Media.DISPLAY_NAME };
				Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
				if (cursor != null && cursor.getCount() != 0) {
					int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
					cursor.moveToFirst();
					fileName = cursor.getString(columnIndex);
				}
				if (cursor != null) {
					cursor.close();
				}
			}
		} catch (Exception ex) {
			return "";
		}
		return fileName;
	}

	public static String getExtension(Context context, Uri uri) {
		return FilenameUtils.getExtension(getName(context, uri));
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 * 		The context.
	 * @param uri
	 * 		The Uri to query.
	 * @param selection
	 * 		(Optional) Filter used in the query.
	 * @param selectionArgs
	 * 		(Optional) Selection arguments used in the query.
	 *
	 * @return The value of the _data column, which is typically a file path.
	 */
	@Nullable
	public static String getDataColumn(Context context, Uri uri, String selection,
			String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	/**
	 * @param uri
	 * 		The Uri to check.
	 *
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 * 		The Uri to check.
	 *
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 * 		The Uri to check.
	 *
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 * 		The Uri to check.
	 *
	 * @return Whether the Uri authority is Turbo Storage.
	 */
	public static boolean isTurboDocument(Uri uri) {
		return "sharedcode.turboeditor.util.documents".equals(uri.getAuthority());
	}
}
