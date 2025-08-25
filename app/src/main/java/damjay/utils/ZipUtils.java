package damjay.utils;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

public class ZipUtils {

    public static boolean extractZip(File file, File outputDir) {
        try(ZipFile zipFile = new ZipFile(file)) {
            Enumeration<ZipEntry> zipEntries = (Enumeration<ZipEntry>) zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry curEntry = zipEntries.nextElement();
                String path = curEntry.getName();
                File outputFile = new File(outputDir, path);
                if (curEntry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    outputFile.getParentFile().mkdirs();
                    InputStream stream = zipFile.getInputStream(curEntry);
                    if (!copyStream(stream, new FileOutputStream(outputFile))) return false;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
        return true;
    }
        
    public static boolean copyStream(InputStream srcStream, OutputStream destStream) {
    	try {
            byte[] buff = new byte[1024];
            int length = 0;

            while ((length = srcStream.read(buff)) > 0) {
                destStream.write(buff, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (srcStream != null) {
                try {
                    srcStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (destStream != null) {
                try {
                    destStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}