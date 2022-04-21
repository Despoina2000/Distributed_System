package Distributed_System_part1.Util;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifImageDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.makernotes.CanonMakernoteDescriptor;
import com.drew.metadata.jpeg.JpegDirectory;

import java.util.Locale;

/**
 * contains helper methods to be used all around
 */
public class Util {

    /**
     * Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic") (mporoume na tin valoume se helper class px utils.java)
     *
     * @param s (topic or "ip:port")
     * @return Hash
     */
    public BigInteger hash(String s) {

        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(s.getBytes());

            // Convert byte array into signum representation
            BigInteger hashInt = new BigInteger(1, messageDigest);
            return hashInt;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * xwrizei to arxeio se mia lista bytes[](to megethos twn byte[] pernaei san parametros)
     *
     * @param file      image or video file
     * @param chunkSize megethos se byte, poso megalo tha einai kathe chunk
     * @return lista me ta byte[] chunks
     */
    public ArrayList<byte[]> splitFileToChunks(File file, int chunkSize) {
        ArrayList<byte[]> chunks_bytes = new ArrayList<byte[]>();
        int sizeOfChunk = 1024 * 1024 * chunkSize;
        String eof = System.lineSeparator();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                int fileSize = 0;
                while (line != null) {
                    byte[] bytes = (line + eof).getBytes(Charset.defaultCharset());
                    if (fileSize + bytes.length > sizeOfChunk) break;
                    fileSize += bytes.length;
                    line = br.readLine();
                    chunks_bytes.add(bytes);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chunks_bytes;
        //TODO
    }


    /**
     * epanasinthetei arxeio apo chunks byte[] (to antitheto tou apo panw)
     *
     * @param chunks lista me ta byte[] chunks
     * @return file image or video file
     */
    public File mergeChunksToFile(ArrayList<byte[]> chunks, File fileObj) {
        //find out the size of the list, array by array
        int size = 0;
        for (byte[] chunk : chunks) {
            size += chunk.length;
        }
        byte[] array = new byte[size];
        int i = 0;
        for (byte[] chunk : chunks) {
            for (byte b : chunk) {
                array[i] = b;
                i++;
            }
        }
        try {
            Files.write(fileObj.toPath(), array);
            //TODO
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileObj;
    }

    //sta epomena mporoume na xrisimopoiisoume to metadata-extractor

    /**
     * vgazei ta metadata tou image pou tha stalnoun mazi me to ImageMessage
     *
     * @param imageFile eikona
     * @return ImageMetadata object
     * @see ImageMetadata
     */
    public ImageMetadata extractImageMetadata(File imageFile) {
        ImageMetadata image = new ImageMetadata();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            //
            // A Metadata object contains multiple Directory objects
            //
            for (Directory directory : metadata.getDirectories()) {
                //
                // Each Directory stores values in Tag objects
                //
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("file name"))
                        image.setFileName(tag.getDescription());
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("file size"))
                        image.setFileSize(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 6)));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).contains("width") && !tag.getTagName().toLowerCase(Locale.ROOT).contains("thumbnail"))
                        image.setWidth(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 7)));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).contains("height") && !tag.getTagName().toLowerCase(Locale.ROOT).contains("thumbnail"))
                        image.setHeight(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 7)));
                }

            }
            return image;
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * vgazei ta metadata tou video pou tha staloun mazi me to VideoMessage
     *
     * @param videoFile video
     * @return VideoMetadata object
     * @see VideoMetadata
     */
    public VideoMetadata extractVideoMetadata(File videoFile) {
        VideoMetadata video = new VideoMetadata();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(videoFile);
            //
            // A Metadata object contains multiple Directory objects
            //
            for (Directory directory : metadata.getDirectories()) {
                //
                // Each Directory stores values in Tag objects
                //
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("file name"))
                        video.setFileName(tag.getDescription());
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("file size"))
                        video.setFileSize(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 6)));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).contains("width") && !tag.getTagName().toLowerCase(Locale.ROOT).contains("thumbnail"))
                        video.setFrameWidth(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 7)));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).contains("height") && !tag.getTagName().toLowerCase(Locale.ROOT).contains("thumbnail"))
                        video.setFrameHeight(Integer.valueOf(tag.getDescription().substring(0, tag.getDescription().length() - 7)));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("duration"))
                        video.setLength(Integer.valueOf(tag.getDescription()));
                    if (tag.getTagName().toLowerCase(Locale.ROOT).equals("frame rate"))
                        video.setFrameRate(Integer.valueOf(tag.getDescription()));
                }
            }
            return video;
        } catch (ImageProcessingException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
