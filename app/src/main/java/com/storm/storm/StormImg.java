package com.storm.storm;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Storm on 5/3/16.
 * Image uploading and downloading util class
 *
 * @author Yuchao
 */
public abstract class StormImg {

    private final static String PATH = "/htdocs/imgs/";
    private final static String URL_YC = "hz160035.ftp.aliapp.com";
    private final static String USER_NAME = "hz160035";
    private final static String PWD = "";

    /**
     * upload a file to FTP sever
     *
     * @param in       FileInputStream
     * @param fileName file name string
     * @return true if uploaded successfully
     */
    public static boolean uploadFile(InputStream in, String fileName) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        Log.e("uploading", PATH + fileName);

        try {
            int reply;
            ftp.connect(URL_YC); // ftp.connect(url, port)
            ftp.login(USER_NAME, PWD);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }

            ftp.enterLocalPassiveMode();

            if (ftp.storeFile(PATH + fileName, in))
                success = true;

            Log.e("FileName", PATH + fileName + "\tsuccess: " + success);
            in.close();
            ftp.logout();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    /**
     * get Bitmap by a URL
     *
     * @param c   Android Context
     * @param url URL string
     * @return the Bitmap we got
     */
    public static Bitmap getBm(Context c, String url) {
        try {
            return Bitmap.createScaledBitmap(Picasso.with(c).load(url).get(), 200, 160, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("Failed get", url);
        return null;
    }
}
