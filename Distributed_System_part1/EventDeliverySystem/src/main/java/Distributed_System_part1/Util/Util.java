package Distributed_System_part1.Util;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

public class Util {

    /**
     * Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic") (mporoume na tin valoume se helper class px utils.java)
     * @param s (topic or "ip:port")
     * @return Hash
     */
    public BigInteger hash(String s) {
        //TODO
        return null;
    }

    /**
     * xwrizei to arxeio se mia lista bytes[](to megethos twn byte[] pernaei san parametros)
     * @param file image or video file
     * @param chunkSize megethos se byte, poso megalo tha einai kathe chunk
     * @return lista me ta byte[] chunks
     */
    public ArrayList<byte[]> splitFileToChunks(File file, int chunkSize) {
        //TODO
        return null;
    }

    /**
     * epanasinthetei arxeio apo chunks byte[] (to antitheto tou apo panw)
     * @param chunks lista me ta byte[] chunks
     * @return file image or video file
     */
    public File mergeChunksToFile(ArrayList<byte[]> chunks) {
        //TODO
        return null;
    }

    //sta epomena mporoume na xrisimopoiisoume to metadata-extractor

    /**
     * vgazei ta metadata tou image pou tha stalnoun mazi me to ImageMessage
     * @param imageFile eikona
     * @return ImageMetadata object
     * @see ImageMetadata
     */
    public ImageMetadata extractImageMetadata(File imageFile) {
        //TODO
        return null;
    }

    /**
     * vgazei ta metadata tou video pou tha staloun mazi me to VideoMessage
     * @param videoFile video
     * @return VideoMetadata object
     * @see VideoMetadata
     */
    public VideoMetadata extractVideoMetadata(File videoFile) {
        //TODO
        return null;
    }
}
