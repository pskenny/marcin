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
		// Naive:
		final String[] notValid = { "\"", "<", ">", "!", "\\?", ":", ";", "\\", "\\*", "|", "$" , "/"};
		
		for (String s : notValid) {
			if (path.contains(s))
				path = path.replaceAll(s, "");
		}
		return path;
	}
	
	public static boolean hasAudioExtension(File file) {
		String extension = FilenameUtils.getExtension(file.getName());

		return AUDIO_EXTENSIONS.contains(extension);
	}
}
