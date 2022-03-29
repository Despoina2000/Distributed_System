package Distributed_System_part1.Util;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

public class Util {
    //Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic") (mporoume na tin valoume se helper class px utils.java)
    public BigInteger hash(String s) {
        //TODO
        return null;
    }

    //xwrizei to arxeio se mia lista bytes[](to megethos twn byte[] pernaei san parametros)
    public ArrayList<byte[]> splitFileToChunks(File file, int chunkSize) {
        //TODO
        return null;
    }

    //epanasinthetei arxeio apo chunks byte[] (to antitheto tou apo panw)
    public File chunksToFile(ArrayList<byte[]> chunks) {
        //TODO
        return null;
    }
}
