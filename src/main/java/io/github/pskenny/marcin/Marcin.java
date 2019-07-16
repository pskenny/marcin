package io.github.pskenny.marcin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;

/**
 * Paul's opinionated music library cleaner. Named after Marcin Staszek.
 *
 * TODO read ID3 info from files, skip exiftool
 *
 * @author Paul Kenny
 */
public class Marcin {

	final String[] AUDIO_EXTENSIONS = { ".mp3", ".ogg", ".wav", ".aac", ".wma", ".flac", ".m4a", ".mp4" };
	final String SOURCE_PATH;

	public Marcin(String source) {
		long startTime = System.nanoTime();

		SOURCE_PATH = source;
		final File SOURCE = new File(SOURCE_PATH);

		// Exit if directory doesn't exist
		if (!SOURCE.exists()) {
			System.out.println(SOURCE_PATH + " doesn't exist. Exitting.");
			System.exit(1);
		}

		System.out.println("Reading filesystem at " + SOURCE_PATH);
		// TODO merge getting files and folders, currently reads all files twice
		final HashSet<File> FILES = listFiles(SOURCE);
		final HashSet<File> FOLDERS = listFolders(SOURCE);
		System.out.println("Finished.");

		// Get empty files
		System.out.print("Getting empty files... ");
		HashSet<File> empty = emptyFiles(FILES);
		System.out.println(empty.size() + " files.");

		// Get non-audio files
		System.out.print("Getting non-audio files... ");
		HashSet<File> nonAudio = getNonAudioFiles(FILES);
		System.out.println(nonAudio.size() + " files.");

		// Change file paths/names based on audio metadata
		fixFileNames(FILES);

		// List empty directories
		System.out.print("Getting empty directories... ");
		HashSet<File> emptyDirectories = emptyDirectories(FOLDERS);
		System.out.println(nonAudio.size() + " directories.");

		for (File f : emptyDirectories) {
			System.out.println("Empty: " + f.getAbsolutePath());
		}

		// delete(nonAudio);
		// TODO confirm to delete
		delete(emptyDirectories);

		long endTime = System.nanoTime();

		long duration = (endTime - startTime);
		double seconds = (double) duration / 1_000_000_000.0;
		System.out.println("Time (s): " + seconds);
	}

	private boolean askYesNo(String message) {
		while (true) {
			System.out.print(message);
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				String name = reader.readLine();
				switch (name) {
				case "":

					break;

				default:
					break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private HashSet<File> emptyFiles(HashSet<File> files) {
		HashSet<File> emptyFiles = new HashSet<File>();
		for (File file : files) {
			if (file.length() == 0) {
				emptyFiles.add(file);
				// Could check it's parent folders until file.legnth for them >1 (if each parent
				// has 1 child it means it's housing an empty folder
			}
		}

		return emptyFiles;
	}

	private void fixFileNames(HashSet<File> files) {
		// TODO check if exiftool is installed, bail if not

		ExifTool exif = new ExifToolBuilder().build();

		for (File file : files) {
//			if (file.getName().endsWith(".mp3")) {
//				mp3(file);
//				continue;
//			}
			try {
				Map<Tag, String> map = exif.getImageMeta(file);

				// TODO rewrite how tags are handled, this currently doesn't scale well
				String album = "";
				String title = "";
				String artist = "";
				String filetype = "";

				for (Map.Entry<Tag, String> entry : map.entrySet()) {
					if (entry.getKey().getName().equals("Album") || entry.getKey().getName().equals("AlbumTitle")) {
						album = entry.getValue();
					} else if (entry.getKey().getName().equals("Title")) {
						title = entry.getValue();
					} else if (entry.getKey().getName().equals("Artist") || entry.getKey().getName().equals("Author")) {
						artist = entry.getValue();
					} else if (entry.getKey().getName().equals("FileTypeExtension")) {
						filetype = entry.getValue().toLowerCase();
					}
				}

				// NOTE: maybe add a map structure and review changes from there
				// Remove / from filenames
				changeFilePath(album, title, artist, filetype, file);
			} catch (Exception e) {
				System.err.println("I canny do on :" + e.getMessage());
			}
		}
	}

	private void mp3(File file) {
		Mp3File mp3;
		try {
			mp3 = new Mp3File(file);
			if (mp3.hasId3v1Tag()) {
				ID3v1 tag = mp3.getId3v1Tag();
				changeFilePath(tag.getAlbum(), tag.getTitle(), tag.getArtist(), "mp3", file);
			} else if (mp3.hasId3v2Tag()) {
				ID3v2 tag = mp3.getId3v2Tag();
				changeFilePath(tag.getAlbum(), tag.getTitle(), tag.getArtist(), "mp3", file);
			} else {
				System.out.println("No id3v1or2 tags");
			}
		} catch (Exception e) {
// Try exiftool
		}
	}

	private void changeFilePath(String album, String title, String artist, String filetype, File file) {
		// TODO new thread?
		StringBuilder updatedName = new StringBuilder();
		updatedName.append(title);
		updatedName.append(".");
		updatedName.append(filetype);

		// Set up directory path for file
		StringBuilder updatedPath = new StringBuilder();
		updatedPath.append(SOURCE_PATH);
		updatedPath.append(File.separator);
		// Add artist and album folders if available
		updatedPath.append(artist.isEmpty() ? "" : artist.replaceAll("/", "") + File.separator);
		updatedPath.append(album.isEmpty() ? "" : album.replaceAll("/", "") + File.separator);

		String strPath = updatedPath.toString();
		// Make directories if not present
		String validPath = makeValidPath(strPath);
		File directory = new File(strPath);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Couldn't make directory " + validPath);
				return;
			}
		}

		// Could add a flag to force removal of potentially bad characters
		String validFilePath = makeValidPath(updatedPath.toString() + updatedName.toString().replaceAll("/", ""));
		// Check if ends with /.mp3 (empty title)
		File newFile = new File(validFilePath);

		// Update filename and correct the filing on path test
		if (newFile.getPath().equals(file.getAbsolutePath())) {
			// All gucci, next file
			return;
		}

		System.out.println("Moving " + file.getAbsolutePath() + " >> " + newFile.getAbsolutePath());

		// TODO thread renaming
		if (file.renameTo(newFile)) {
			System.out.println("... success!");
		} else {
			System.out.println("... failed");
		}
	}

	/**
	 * Remove characters that might invalidate path
	 * 
	 * @param path String path to check.
	 * @return String path with invalid characters removed.
	 */
	private String makeValidPath(String path) {
		// Naive way coming up ahead
		// TODO remove all . except for the file extension one
		final String[] notValid = { "\"", "<", ">", "!", "\\?", ";", "\\", "\\*", "|", "$" };
		
		for (String s : notValid) {
			if (path.contains(s))
				path = path.replaceAll(s, "");
		}
		return path;
	}

	private void delete(HashSet<File> arr) {
		for (File file : arr) {
			System.out.print("Deleting " + file.getAbsolutePath() + "... ");
			if (file.delete()) {
				System.out.print("success\n");
			} else {
				System.out.print("failed\n");
			}
		}
	}

	/**
	 * Checks if files in array are audio based on file extension.
	 * 
	 * @param files
	 * @return
	 */
	public HashSet<File> getNonAudioFiles(HashSet<File> files) {
		HashSet<File> notAudio = new HashSet<File>();
		for (File f : files) {
			if (!isAudio(f)) {
				notAudio.add(f);
			}
		}

		return notAudio;
	}

	public boolean isAudio(File file) {
		final String filename = file.getName();

		for (String extension : AUDIO_EXTENSIONS) {
			if (filename.toLowerCase().endsWith(extension))
				return true;
		}
		return false;
	}

	/**
	 * Gets all files
	 *
	 * @param dir
	 * @return
	 */
	public HashSet<File> listFiles(File dir) {
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

	/**
	 * Recursively search for folders
	 * 
	 * @param dir
	 * @return
	 */
	public HashSet<File> listFolders(File dir) {
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

	/**
	 * Returns set of empty directories.
	 * 
	 * @param
	 * @return
	 */
	public HashSet<File> emptyDirectories(HashSet<File> directories) {
		HashSet<File> dirs = new HashSet<File>();

		for (File directory : directories) {
			// Check if directory has any files. Add to list if 0.
			if (directory.listFiles().length == 0) {
				dirs.add(checkEmptyParents(directory));
			}
		}

		return dirs;
	}

	/**
	 * Returns the highest level of empty directories. Only checks vertically, not
	 * horizontally.
	 * 
	 * @param directory
	 * @return
	 */
	public File checkEmptyParents(File directory) {
		while (true) {
			if (directory.getParentFile().length() == 1) {
				// Parent to an empty child folder
				directory = directory.getParentFile();
			} else {
				// More than one file == not empty
				return directory;
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 0)
			return;
		// TODO Add -y automatic confirmation
		ArgumentParser parser = ArgumentParsers.newFor("marcin").build()
				.description("Paul's opinionated music cleaner.");

		new Marcin(args[0]);
	}
}
