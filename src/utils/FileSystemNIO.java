package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileSystemNIO {
    public static final int CHUNK_SIZE = 64000; /** read and write chunks of 64 kb */
    
    /**
     * 
     * @param pathname
     * @param data
     * @return
     */
    public static boolean storeLocalFile(String pathname, byte[] data) {
        try {
            // create the file to store the data
            File confFile = new File(pathname);

            // Creates a random access file stream to read from
            RandomAccessFile randomAccessStream = new RandomAccessFile(pathname, "rw");

            // Create the buffer and channel
            FileChannel outputFileChannel = randomAccessStream.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(CHUNK_SIZE);

            // flush the data to the buffer and then to the channel (aka write to the file)
            int remainingBytes = data.length;
            while (remainingBytes > 0) {
                // calculate the chunk size
                int bytesToWrite = remainingBytes > CHUNK_SIZE ? CHUNK_SIZE : remainingBytes;
                // write to the buffer
                buf.put(data, data.length - remainingBytes, bytesToWrite);
                // update remaining bytes left
                remainingBytes -= bytesToWrite;
                // write to the channel
                try {
                    buf.flip();
                    outputFileChannel.write(buf);
                } catch (IOException e) {
                    PrintMessage.e("Backing up", String.format("Failed to store file chunk locally: %s", pathname));
                    return false;
                }
                // mark buffer as ready for further writing (aka mark as empty)
                buf.clear();
            }

            try {
                randomAccessStream.close();
                outputFileChannel.close();
            } catch (IOException e) {
                PrintMessage.e("Close streams", "Failed to close file channel");
                return false;
            }
        } catch (FileNotFoundException e) {
            PrintMessage.e("Create file", String.format("Failed to create local file %s for backup", pathname));
            return false;
        }

        return true;
    }

    /**
     * 
     * @param pathname
     * @param data
     * @return
     */
    public static byte[] loadLocalFile(String pathname) {
        // hacky way to create a dynamic array of byte
        ByteArrayOutputStream fileContentStream = new ByteArrayOutputStream();
        byte[] fileContent;
        try {
            // Creates a random access file stream to read from
            RandomAccessFile randomAccessStream = new RandomAccessFile(pathname, "r");
            FileChannel inputFileChannel = randomAccessStream.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(CHUNK_SIZE);

            // while it doen't reach end of stream, read from the file channel into byte
            // buffer
            int readBytes;
            while ((readBytes = inputFileChannel.read(buf)) != -1) {
                // flip buffer from writing mode to reading mode
                buf.flip();
                // move the data from the buffer into the byte array
                byte[] auxiliarBuf = new byte[CHUNK_SIZE];
                buf.get(auxiliarBuf, 0, readBytes);
                // append to fileContentStream
                fileContentStream.write(auxiliarBuf, 0, readBytes);
                // mark buffer as ready for further writing (aka mark as empty)
                buf.clear();
            }

            // close all streams
            randomAccessStream.close();
            inputFileChannel.close();

            // convert the byte stream to byte array
            fileContent = fileContentStream.toByteArray();
            return fileContent;
        } catch (FileNotFoundException e1) {
            PrintMessage.e("Open File", String.format("Failed to open file %s because it doesn't exist", pathname));
            return null;
        } catch (IOException e) {
            PrintMessage.e("Open File", String.format("An IO error ocurred while reading the file %s", pathname));
            e.printStackTrace();
            return null;
        }
    }
}