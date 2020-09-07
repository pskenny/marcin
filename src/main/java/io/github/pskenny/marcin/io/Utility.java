package io.github.pskenny.marcin.io;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.io.FilenameUtils;

public class Utility {
	// File extensions from https://en.wikipedia.org/wiki/Audio_file_format
	public static final HashSet<String> AUDIO_EXTENSIONS = new HashSet<String>(
			Arrays.asList("3gp", "8svx", "aa", "aac", "aax", "aax", "act", "aiff", "alac", "amr", "ape",
					"au", "awb", "cda", "dct.", "dss", "dvf", "flac", "gsm", "iklax", "ivs", "m4a", "m4b",
					"m4p", "mmf", "mp3", "mpc", "msv", "nmf", "nsf", "ogg", "oga", "mogg", "opus", "ra",
					"rm", "raw", "rf64", "sln", "tta", "voc", "vox", "wav", "wma", "wv", "webm"));
	/* Invalid characters/filenames:
	https://docs.microsoft.com/en-gb/windows/win32/fileio/naming-a-file?redirectedfrom=MSDN
	https://en.wikipedia.org/wiki/Ext3 */
	private static final String[] INVALID_CHARACTERS = { "\"", "<", ">", "!", "\\?", ":", ";", "\\", "\\*", "|", "$" , "/"};

	public static HashSet<File> listFiles(File dir) {
		HashSet<File> files = new HashSet<File>();

		File[] fi = dir.listFiles();

		for (File f : fi) {
			if (f.isDirectory()) {
				files.addAll(listFiles(f));
			} else {
				files.add(f);
			}
		}

		return files;
	}

	public static HashSet<File> listFolders(File dir) {
		HashSet<File> folders = new HashSet<File>();

		File[] fi = dir.listFiles();

		for (File f : fi) {
			if (f.isDirectory()) {
				folders.addAll(listFolders(f));
			}
		}
		folders.add(dir);

		return folders;
	}
	
	public static String makeValidPath(String path) {
		// Check if each invalid character is in the path
		for (String s : INVALID_CHARACTERS) {
			if (path.contains(s))
				// remove character from file
				path = path.replaceAll(s, "");
		}
		return path;
	}
	
	public static boolean hasAudioExtension(File file) {
		String extension = FilenameUtils.getExtension(file.getName());

		return AUDIO_EXTENSIONS.contains(extension);
	}
}
