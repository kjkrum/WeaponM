package krum.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {
	
	public final String extension;
	public final String description;
	
	/**
	 * The <tt>extension</tt> parameter is typically a file extension
	 * including a leading dot, but it can be any string matching the end of a
	 * file name.  For example, it could be the tilde character (<tt>'~'</tt>)
	 * often appended to the file names of automatic backups.
	 * 
	 * @param extension file ending to match
	 * @param description file type description
	 */
	public ExtensionFileFilter(String extension, String description) {
		this.extension = extension;
		this.description = description;
	}

	@Override
	public boolean accept(File file) {
		return(file.isDirectory() || file.getName().endsWith(extension));
	}

	@Override
	public String getDescription() {
		return description + " (*" + extension + ")";
	}

}
