package Distributed_System_part1.Util;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageReade;
/**
 * contains helper methods to be used all around
 */
public class Util {

    /**
     * Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic") (mporoume na tin valoume se helper class px utils.java)
     * @param s (topic or "ip:port")
     * @return Hash
     */
    public BigInteger hash(String s) {
        
        //TODO
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
    }

    /**
     * xwrizei to arxeio se mia lista bytes[](to megethos twn byte[] pernaei san parametros)
     * @param file image or video file
     * @param chunkSize megethos se byte, poso megalo tha einai kathe chunk
     * @return lista me ta byte[] chunks
     */
    public ArrayList<byte[]> splitFileToChunks(File file, int chunkSize) throws    IOException {
    List<byte[]> chunks_bytes = new ArrayList<byte[]>();
    int sizeOfChunk = 1024 * 1024 * chunkSize;
    String eof = System.lineSeparator();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line = br.readLine();
        while (line != null) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile))) {
                int fileSize = 0;
                while (line != null) {
                    byte[] bytes = (line + eof).getBytes(Charset.defaultCharset());
                    if (fileSize + bytes.length > sizeOfChunk)
                        break;
                    out.write(bytes);
                    fileSize += bytes.length;
                    line = br.readLine();
                }
                chunks_bytes.add(bytes);
            }
        }

br.close();
    }
    return chunks_bytes;
    //TODO
            }

    /**
     * epanasinthetei arxeio apo chunks byte[] (to antitheto tou apo panw)
     * @param chunks lista me ta byte[] chunks
     * @return file image or video file
     */
    public File mergeChunksToFile(ArrayList<byte[]> chunks) {
    File fileObj = new File("file.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileObj))) {
        String name = file.getName();
        String line = br.readLine();
        for ( byte[] chunk: chunks) {
            writer.append(chunk);
    }//TODO
    }
        return fileObj;
    }

    //sta epomena mporoume na xrisimopoiisoume to metadata-extractor

    /**
     * vgazei ta metadata tou image pou tha stalnoun mazi me to ImageMessage
     * @param imageFile eikona
     * @return ImageMetadata object
     * @see ImageMetadata
     */
    public ImageMetadata extractImageMetadata(File imageFile) {
     int pos = imageFile.getName().lastIndexOf(".");
  if (pos == -1)
    throw new IOException("No extension for file: " + imageFile.getAbsolutePath());
    String suffix = imageFile.getName().substring(pos + 1);
  Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
  while(iter.hasNext()) {
    ImageReader reader = iter.next();
    try {
      ImageInputStream stream = new FileImageInputStream(imageFile);
      reader.setInput(stream);
      int width = reader.getWidth(reader.getMinIndex());
      int height = reader.getHeight(reader.getMinIndex());
      long bytes = imageFile.size();
      String name = imageFile.getName();
      ImageMetadata img = new ImageMetadata(name, bytes, width, height);
      return img;
    } catch (IOException e) {
      log.warn("Error reading: " + imgFile.getAbsolutePath(), e);
    } finally {
      reader.dispose();
    }


  }
    
    //TODO
    }

    /**
     * vgazei ta metadata tou video pou tha staloun mazi me to VideoMessage
     * @param videoFile video
     * @return VideoMetadata object
     * @see VideoMetadata
     */
    public VideoMetadata extractVideoMetadata(File videoFile) {
    long bytes = videoFile.size();
    String name = videoFile.getName();
    int length = videoFile.length();
    VideoMetadata video = new VideoMetadata(name, bytes);    
    
    //TODO
        return video;
    }
}
