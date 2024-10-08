package fr.soe.a3s.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileAccessMethods implements DataAccessConstants {

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}
		} else {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = new FileInputStream(sourceLocation);
				out = new FileOutputStream(targetLocation);
				// Copy the bits from instream to outstream
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
		}
	}

	public static boolean deleteDirectory(File file) {

		if (file.exists()) {
			File[] subFiles = file.listFiles();
			if (subFiles != null) {
				for (int i = 0; i < subFiles.length; i++) {
					if (subFiles[i].isDirectory()) {
						deleteDirectory(subFiles[i]);
					} else {
						subFiles[i].delete();
					}
				}
			}
		}
		return (file.delete());
	}

	public static void copyFile(File sourceLocation, File targetLocation) throws IOException {

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(sourceLocation);
			FileChannel source = fis.getChannel();
			fos = new FileOutputStream(targetLocation);
			FileChannel destination = fos.getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static boolean deleteFile(File file) {
		boolean response = false;
		if (file.exists()) {
			response = file.delete();
		}
		return response;
	}

	public static void setWritePermissions(File folder) {

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File f = files[i];
					if (f.isDirectory()) {
						boolean succeed = f.setWritable(true, false);
						setWritePermissions(f);
					}
				}
			}
		}
	}

	public static String getCanonicalPath(File file) {

		String filePath = null;
		try {
			filePath = file.getCanonicalPath();
		} catch (IOException e) {
			filePath = file.getAbsolutePath();
		}
		return filePath;
	}

	public static void extractToFolder(File zipFile, File folder) throws IOException {

		// cr�ation de la ZipInputStream qui va servir � lire les donn�es du
		// fichier zip
		ZipInputStream zis = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(zipFile.getCanonicalFile())));

		// extractions des entr�es du fichiers zip (i.e. le contenu du zip)
		ZipEntry ze = null;
		try {
			while ((ze = zis.getNextEntry()) != null) {

				// Pour chaque entr�e, on cr�e un fichier
				// dans le r�pertoire de sortie "folder"
				File f = new File(folder.getCanonicalPath(), ze.getName());

				// Si l'entr�e est un r�pertoire,
				// on le cr�e dans le r�pertoire de sortie
				// et on passe � l'entr�e suivante (continue)
				if (ze.isDirectory()) {
					f.mkdirs();
					continue;
				}

				// L'entr�e est un fichier, on cr�e une OutputStream
				// pour �crire le contenu du nouveau fichier
				f.getParentFile().mkdirs();
				OutputStream fos = new BufferedOutputStream(new FileOutputStream(f));

				// On �crit le contenu du nouveau fichier
				// qu'on lit � partir de la ZipInputStream
				// au moyen d'un buffer (byte[])
				try {
					try {
						final byte[] buf = new byte[8192];
						int bytesRead;
						while (-1 != (bytesRead = zis.read(buf)))
							fos.write(buf, 0, bytesRead);
					} finally {
						fos.close();
					}
				} catch (final IOException ioe) {
					// en cas d'erreur on efface le fichier
					f.delete();
					throw ioe;
				}
			}
		} finally {
			// fermeture de la ZipInputStream
			zis.close();
		}
	}

	public static boolean zip(File zipFile, File folder) throws IOException {

		boolean result = false;
		try {
			System.out.println("Program Start zipping the given files");
			/*
			 * send to the zip procedure
			 */
			zipFolder(folder.getAbsolutePath(), zipFile.getAbsolutePath());
			result = true;
			System.out.println("Given files are successfully zipped");
		} catch (Exception e) {
			System.out.println("Some Errors happned during the zip process");
		}
		return result;
	}

	private static void zipFolder(String srcFolder, String destZipFile) throws Exception {
		ZipOutputStream zip = null;
		FileOutputStream fileWriter = null;
		/*
		 * create the output stream to zip file result
		 */
		fileWriter = new FileOutputStream(destZipFile);
		zip = new ZipOutputStream(fileWriter);
		/*
		 * add the folder to the zip
		 */
		addFolderToZip("", srcFolder, zip);
		/*
		 * close the zip objects
		 */
		zip.flush();
		zip.close();
	}

	/*
	 * recursively add files to the zip files
	 */
	private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws Exception {
		/*
		 * create the file object for inputs
		 */
		File folder = new File(srcFile);

		/*
		 * if the folder is empty add empty folder to the Zip file
		 */
		if (flag == true) {
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
		} else { /*
					 * if the current name is directory, recursively traverse it to get the files
					 */
			if (folder.isDirectory()) {
				/*
				 * if folder is not empty
				 */
				addFolderToZip(path, srcFile, zip);
			} else {
				/*
				 * write the file to the output
				 */
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in = new FileInputStream(srcFile);
				zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
				while ((len = in.read(buf)) > 0) {
					/*
					 * Write the Result
					 */
					zip.write(buf, 0, len);
				}
				in.close();
			}
		}
	}

	/*
	 * add folder to the zip file
	 */
	private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
		File folder = new File(srcFolder);

		/*
		 * check the empty folder
		 */
		if (folder.list().length == 0) {
			System.out.println(folder.getName());
			addFileToZip(path, srcFolder, zip, true);
		} else {
			/*
			 * list the files in the folder
			 */
			for (String fileName : folder.list()) {
				if (path.equals("")) {
					addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, false);
				} else {
					addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, false);
				}
			}
		}
	}

	public static String computeSHA1(File file) throws IOException {

		if (file.length() == 0) {
			return "0";
		}

		/*
		 * char[] chars = null; MessageDigest md = MessageDigest.getInstance("SHA1");
		 * FileInputStream fis = new FileInputStream(file); FileChannel ch =
		 * fis.getChannel(); MappedByteBuffer mb = ch.map(FileChannel.MapMode.READ_ONLY,
		 * 0L, ch.size()); int buffsize = (int) Math.min(file.length(), 4 * 1024 *
		 * 1024); byte[] dataBytes = new byte[buffsize]; long checkSum = 0L; int nread;
		 * while (mb.hasRemaining()) { nread = Math.min(mb.remaining(), buffsize);
		 * mb.get(dataBytes, 0, nread); md.update(dataBytes, 0, nread); } fis.close();
		 * System.gc(); byte[] mdbytes = md.digest(); chars = new char[2 *
		 * mdbytes.length]; for (int i = 0; i < mdbytes.length; ++i) { chars[2 * i] =
		 * HEX_CHARS[(mdbytes[i] & 0xF0) >>> 4]; chars[2 * i + 1] = HEX_CHARS[mdbytes[i]
		 * & 0x0F]; }
		 */

		// convert the byte to hex format
		FileInputStream fis = null;

		/*
		 * ReadableByteChannel inChannel = Channels.newChannel(inputStream); ByteBuffer
		 * buffer = ByteBuffer.allocate(BUFFER_SIZE); while (((bytesRead =
		 * inChannel.read(buffer)) != -1) && !httpDAO.isCanceled()) { byte[] array =
		 * buffer.array(); dos.write(array, 0, bytesRead); buffer.clear(); }
		 */

		char[] chars = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			fis = new FileInputStream(file);
			ReadableByteChannel inChannel = Channels.newChannel(fis);
			int buffsize = (int) Math.min(file.length(), 4 * 1024 * 1024);
			ByteBuffer buffer = ByteBuffer.allocate(buffsize);
			int nread = 0;
			while ((nread = inChannel.read(buffer)) != -1) {
				md.update(buffer.array(), 0, nread);
				buffer.clear();
			}
			byte[] mdbytes = md.digest();
			chars = new char[2 * mdbytes.length];
			for (int i = 0; i < mdbytes.length; ++i) {
				chars[2 * i] = HEX_CHARS[(mdbytes[i] & 0xF0) >>> 4];
				chars[2 * i + 1] = HEX_CHARS[mdbytes[i] & 0x0F];
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		return new String(chars);
	}
}
