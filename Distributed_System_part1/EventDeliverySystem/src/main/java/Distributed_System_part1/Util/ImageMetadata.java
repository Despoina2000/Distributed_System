package Distributed_System_part1.Util;

import java.io.Serializable;

/**
 * pojo containing the image metadata
 */
public class ImageMetadata implements Serializable {

    private String fileName;
    /**
     * file size in bytes
     */
    private int fileSize;
    /**
     * width in pixels
     */
    private int width;
    /**
     * height in pixels
     */
    private int height;

    public ImageMetadata(String fileName, int fileSize, int width, int height) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
