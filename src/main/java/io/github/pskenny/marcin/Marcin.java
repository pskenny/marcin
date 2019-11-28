package io.github.pskenny.marcin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.*;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import io.github.pskenny.marcin.io.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Marcin {

	public static final FieldKey[] DEFAULT_KEYS = { FieldKey.ARTIST, FieldKey.ALBUM, FieldKey.TITLE };
	private final String DEFAULT_MUSIC_DIRECTORY;

	public Marcin(String dir) {
		DEFAULT_MUSIC_DIRECTORY = dir;
		long time = System.nanoTime();
		HashSet<File> all = Utility.listFiles(dir);
		Stream<File> files = all.parallelStream().filter(file -> file.isFile() && Utility.hasAudioExtension(file));
		//Stream<File> dirs = all.parallelStream().filter(file -> file.isDirectory());

		files.forEach(file -> compareGeneratedPaths(file));

		System.out.println("Time: " + (System.nanoTime() - time) / 1000000000d);
	}

	public void compareGeneratedPaths(File file) {
		try {
			String original = file.getCanonicalPath();

			String generated = generatePath(file, DEFAULT_MUSIC_DIRECTORY, DEFAULT_KEYS);
			if (!original.contentEquals(generated)) {
				System.out.println("\n" + original + "\n" + generated);
				// Move file to new path
				// move(file, generated);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void move(File file, String newPath) {
		// Ref:
		// https://www.geeksforgeeks.org/moving-file-one-directory-another-using-java/
		try {
			Path temp = Files.move(Paths.get(file.getCanonicalPath()), Paths.get(newPath));

			if (temp != null) {
				System.out.println("File renamed and moved successfully");
			} else {
				System.out.println("Failed to move the file");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String generatePath(File file, String basePath, FieldKey[] keys) {
		StringBuilder sb = new StringBuilder();

		AudioFile f;
		try {
			f = AudioFileIO.read(file);
			Tag tag = f.getTag();

			// NOTE: Seems to take tag info from ID3v2.4.0 and not ID3v1.1
			for (FieldKey key : keys) {
				try {
					// If no album it still adds the extra file separator
					String tagKey = tag.getFirst(key);
					if (tagKey.isEmpty())
						continue;
					sb.append(System.getProperty("file.separator"));
					sb.append(Utility.makeValidPath(tagKey));
				} catch (KeyNotFoundException kex) {

				}
			}

			String extension = "." + FilenameUtils.getExtension(file.getCanonicalPath());

			// no tags found, give it a unique name
			if (sb.length() == 0) {
				sb.append(System.getProperty("file.separator"));
				sb.append(uniqueFileName(file.getParentFile(), extension));
			}
			sb.insert(0, basePath);
			sb.append(extension);
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	private String uniqueFileName(File dir, String extension) throws IOException {
		String defaultName = "FIXME";
		long number = 0l;

		while (true) {
			File newFile = new File(
					dir.getCanonicalPath() + System.getProperty("file.separator") + defaultName + number + extension);
			if (newFile.exists()) {
				number++;
			} else {
				return defaultName + number;
			}
		}
	}
	
	// Disable Jaudiotagger logging, see https://stackoverflow.com/questions/50778442/how-to-disable-jaudiotagger-logger-completely
	static {
        //Disable loggers               
        Logger[] pin = new Logger[]{ Logger.getLogger("org.jaudiotagger") };

        for (Logger l : pin)
            l.setLevel(Level.OFF);
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
