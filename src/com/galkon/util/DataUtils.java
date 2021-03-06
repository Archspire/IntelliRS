package com.galkon.util;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.galkon.io.bzip2.BZip2InputStream;
import com.galkon.io.bzip2.BZip2OutputStream;


public class DataUtils {

	/**
	 * Generates a Jagex hash for the specified string.
	 * @param string
	 * @return
	 */
	public static int getHash(String string) {
		int identifier = 0;
		string = string.toUpperCase();
		for (int index = 0; index < string.length(); index++) {
			identifier = (identifier * 61 + string.charAt(index)) - 32;
		}
		return identifier;
	}

	/**
	 * Reads the specified file and returns it's data as a byte array.
	 * @param file
	 * @return
	 */
	public static byte[] readFile(String file) {
		try {
			RandomAccessFile raf = new RandomAccessFile(new File(file), "r");
			byte[] data = new byte[(int) raf.length()];
			raf.readFully(data);
			raf.close();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Writes data to the specified file.
	 * @param file
	 * @param data
	 */
	public static void writeFile(String file, byte[] data) {
		try {
			if (data != null) {
				OutputStream out = new FileOutputStream(file);
				out.write(data);
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Compresses all data provided to headerless bz2 format
	 *
	 * @param data the data to compress
	 * @return The compressed data
	 * @throws IOException If there was an error compressing
	 */
	public static byte[] compressBZip2(byte[] data) {
		return compressBZip2(data, 0, data.length);
	}


	/**
	 * Compresses data provided between off + len to headerless bz2 format
	 *
	 * @param data the data to compress
	 * @param off  offset to compress from
	 * @param len  amount to compress
	 * @return The compressed data
	 * @throws IOException If there was an error compressing
	 */
	public static byte[] compressBZip2(byte[] data, int off, int len) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			BZip2OutputStream bzo = new BZip2OutputStream(bos);
			bzo.write(data, off, len);
			bzo.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Decompresses all headerless bz2 data provided
	 *
	 * @param data bz2 data to decompress
	 * @return The decompressed data
	 * @throws IOException If there was an error decompressing
	 */
	public static byte[] decompressBZip2(byte[] data) {
		return decompressBZip2(data, 0, data.length);
	}

	/**
	 * Decompressed data provided between off + len from headerless bz2 format
	 *
	 * @param data the data to decompress
	 * @param off  offset to decompress from
	 * @param len  amount to decompress
	 * @return The decompressed data
	 * @throws IOException If there was an error decompressing
	 */
	public static byte[] decompressBZip2(byte[] data, int off, int len) {
		try {
			byte[] dat = new byte[len];
			System.arraycopy(data, off, dat, 0, len);
			BZip2InputStream bzi = new BZip2InputStream(new ByteArrayInputStream(dat));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int read;
			while ((read = bzi.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] compressGZip(byte[] data, int off, int len) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzo = new GZIPOutputStream(bos);
			try {
				gzo.write(data, off, len);
			} finally {
				gzo.close();
				bos.close();
			}
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decompressGZip(byte[] b) throws IOException {
		GZIPInputStream gzi = new GZIPInputStream(new ByteArrayInputStream(b));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = gzi.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		return out.toByteArray();
	}

}
