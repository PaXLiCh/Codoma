package ru.kolotnev.codoma;

import android.net.Uri;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;

/**
 * Extended URI.
 */
public class GreatUri {
	private Uri uri;
	private String filePath;
	private String fileName;
	private String fileExtension;

	GreatUri(Uri uri, String fileAbsoluteName) {
		this.uri = uri;
		//fileAbsoluteName = AccessStorageApi.getPath(this, uri);
		filePath = fileAbsoluteName;//FilenameUtils.getFullPath(fileAbsoluteName);
		fileName = FilenameUtils.getBaseName(fileAbsoluteName);
		fileExtension = FilenameUtils.getExtension(fileAbsoluteName).toLowerCase();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
				append(uri).
				toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GreatUri))
			return false;
		if (obj == this)
			return true;

		GreatUri rhs = (GreatUri) obj;
		return new EqualsBuilder().
				// if deriving: appendSuper(super.equals(obj)).
						append(uri, rhs.uri).
				isEquals();
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getParentFolder() {
		return new File(filePath).getParent();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public boolean isReadable() {
		return new File(getFilePath()).canRead();
	}

	public boolean isWritable() {
		return new File(getFilePath()).canWrite();
	}

	@Override
	public String toString() {
		return String.format("%s/%s.%s", filePath, fileName, fileExtension);
	}
}
