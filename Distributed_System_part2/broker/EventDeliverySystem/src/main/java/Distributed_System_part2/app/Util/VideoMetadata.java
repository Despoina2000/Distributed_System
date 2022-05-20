package Distributed_System_part2.app.Util;

import java.io.Serializable;

/**
 * pojo containing the video metadata
 */
public class VideoMetadata implements Serializable {

    private String fileName;
    /**
     * file size in bytes
     */
    private int fileSize;
    /**
     * frame width in pixels
     */
    private int frameWidth;
    /**
     * frame height in pixels
     */
    private int frameHeight;
    /**
     * video length in ms
     */
    private int length;
    /**
     * frames per second
     */
    private int frameRate;
    public VideoMetadata(){}

    public VideoMetadata(String fileName, int fileSize, int frameWidth, int frameHeight, int length, int frameRate) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.length = length;
        this.frameRate = frameRate;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getLength() {
        return length;
    }

    public int getFrameRate() {
        return frameRate;
    }
    public void setFileName(String name) {
         fileName=name;
    }

    public void setFileSize( int size) {
         fileSize= size;
    }

    public void setFrameWidth(int width) {
         frameWidth=width;
    }

    public void setFrameHeight(int height) {
         frameHeight=height;
    }

    public void setLength(int length) {
        this.length=length;
    }

    public void setFrameRate( int rate) {
         frameRate=rate;
    }

    @Override
    public String toString() {
        return "VideoMetadata{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", frameWidth=" + frameWidth +
                ", frameHeight=" + frameHeight +
                ", length=" + length +
                ", frameRate=" + frameRate +
                '}';
    }
}
