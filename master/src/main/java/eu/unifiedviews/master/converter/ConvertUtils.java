package eu.unifiedviews.master.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import eu.unifiedviews.master.model.ApiException;

public class ConvertUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static String dateToString(Date date) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        if (date != null) {
            return df.format(date);
        } else {
            return null;
        }
    }

    public static Date stringToDate(String strDate) {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        Date result = null;
        if (!StringUtils.isBlank(strDate)) {
            try {
                result = df.parse(strDate);
            } catch (ParseException e) {
                throw new ApiException(Response.Status.BAD_REQUEST, String.format("Problem parsing date=%s. Correct date format is: %s", strDate, DATE_FORMAT));
            }
        }
        return result;
    }

    /**
     * Read input stream fo a file.
     *
     * Method creates a new temp directory and a new file in it. Content of input stream is copyied to the new file.
     *
     * File, and temp directory are marked for deletion at application stop.
     *
     * @param inputStream Input stream
     * @param filename filename of a new file.
     * @return new file.
     */
    public static File inputStreamToFile(InputStream inputStream, String filename) throws IOException {
        File file = null;
        Path tempDir = Files.createTempDirectory(String.valueOf(inputStream.hashCode()));
        tempDir.toFile().deleteOnExit();
        file = new File(tempDir.toFile(), filename);
        file.deleteOnExit();
        FileUtils.copyInputStreamToFile(inputStream, file);
        return file;
    }
}
