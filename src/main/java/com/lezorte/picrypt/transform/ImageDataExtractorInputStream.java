package com.lezorte.picrypt.transform;

import com.lezorte.picrypt.exceptions.ImageCorruptException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by lezorte on 12/31/16.
 */
public class ImageDataExtractorInputStream extends InputStream {

    private BufferedImage image;

    private int x = 0;
    private int y = 0;
    private int currentColor = 0;
    private int currentBitPosition = 0;
    private long messageSize = 0;
    private long totalBytesRead = 0;

    public ImageDataExtractorInputStream(BufferedImage image) {
        this.image = image;
        try {
            byte[] messageSizeAsArray = new byte[8];
            read(messageSizeAsArray);
            messageSize = ByteBuffer.wrap(messageSizeAsArray).getLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int read() throws IOException{
        if(totalBytesRead == messageSize) {
            return -1;
        }
        int byteValue = 0;
        for(int i=0;i<8;i++) {
            byteValue |= readBit()<<i;
        }
        return byteValue;
    }

    @Override
    public void close() {
        image = null;
    }

    private int readBit() throws ImageCorruptException {
        // Check if any variables need to be changed
        if(x==image.getWidth()) {
            x = 0;
            y++;
            if(y==image.getHeight()) {
                y = 0;
                currentColor++;
                if (currentColor == 3) {
                    currentColor = 0;
                    currentBitPosition++;
                    if (currentBitPosition == 8) {
                        throw new ImageCorruptException("Message size is larger than image can store. Image is corrupt or not a Picrypt image.");
                    }
                }
            }
        }
        int rgb = image.getRGB(x, y);
        int shift = currentColor*8+currentBitPosition;
        x++;
        return (rgb & (1<<shift))>>>shift;
    }
}