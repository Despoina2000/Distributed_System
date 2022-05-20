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
    private long fileSize;
    /**
     * width in pixels
     */
    private int width;
    /**
     * height in pixels
     */
    private int height;
public ImageMetadata(){

}
    public ImageMetadata(String fileName, long  fileSize, int width, int height) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
       this.height=height;
    }
    public void setFileName( String name) {
         fileName=name;
    }

    public void setFileSize(int size) {
         fileSize=size;
    }

    public void setWidth( int width) {
        this.width= width;
    }

   

    @Override
    public String toString() {
        return "ImageMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
