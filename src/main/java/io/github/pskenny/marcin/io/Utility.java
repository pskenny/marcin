package io.github.pskenny.marcin.io;

import java.io.File;
import java.util.HashSet;

public class Utility {
	public static final String[] AUDIO_EXTENSIONS = { ".mp3", ".ogg", ".wav", ".aac", ".wma", ".flac", ".m4a", ".mp4" };
	
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

	public static HashSet<File> listFiles(String dir) {
		File file = new File(dir);

		HashSet<File> files = new HashSet<File>();

		File[] fi = file.listFiles();

		for (File f : fi)
			if (f.isDirectory()) {
				files.addAll(listFiles(f));
			} else {
				files.add(f);
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
		// Naive:
		final String[] notValid = { "\"", "<", ">", "!", "\\?", ":", ";", "\\", "\\*", "|", "$" , "/"};
		
		for (String s : notValid) {
			if (path.contains(s))
				path = path.replaceAll(s, "");
		}
		return path;
	}
	
	public static boolean hasAudioExtension(File file) {
		final String filename = file.getName();

		for (String extension : AUDIO_EXTENSIONS) {
			if (filename.toLowerCase().endsWith(extension))
				return true;
		}
		return false;
	}
}