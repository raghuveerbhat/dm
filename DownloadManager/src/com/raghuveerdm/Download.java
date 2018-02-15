package com.raghuveerdm;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.*;

class Download extends Observable implements Runnable {


    //Max size of download buffer
    private static final int MAX_BUFFER_SIZE = 1024;

    //Stautus names
    public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};

    //Download status variables
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    //Donwload info variables
    private URL url;
    private int size;
    private boolean isHttps = false;
    //no of bytes downloaded
    private int downloaded;
    private int status;

    //Constructor
    public Download(URL url) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
        if (url.toString().toLowerCase().startsWith("https://"))
            isHttps = true;
        System.out.println("Download constructor called and value initialized...");
        download();
    }

    //Get this download's url
    public String getURL() {
        return url.toString();
    }

    //Get this download's size
    public int getSize() {
        return size;
    }

    //Get this download's progress
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    public int getStatus() {
        return status;
    }

    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    //To mark the download as error
    private void error() {
        status = ERROR;
        stateChanged();
    }

    //Start or resume Downloading
    private void download() {
        System.out.println("Called download()...");
        Thread thread = new Thread(this);
        System.out.println("Created new thread...\nStarting the new thread...");
        thread.start();
    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);

    }


    @Override
    public void run() {
        System.out.println("RUN: " + Thread.currentThread().toString());

        try {
            if (isHttps == true) {
                System.out.println("Https connection opening...");
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                System.out.println("Https connection opened.");
                doHttpsStuff(connection);
            } else {
                System.out.println("Http connection opening...");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                System.out.println("Http connection opened.");
                doHttpStuff(connection);
            }
        } catch (Exception e) {
            System.out.println("no http connection opened..."+e);
        }
    }

    private void doHttpStuff(HttpURLConnection connection) {
        System.out.println("<<<<<<<<<<<<<<HTTP CONNECTION>>>>>>>>>>>>>>>");
        RandomAccessFile file = null;
        InputStream stream = null;
        System.out.println("RAF and InputStream variables initialized...");
        //Specifying what portion of file to Download
        connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
        System.out.println("Specified what portion of file to download...");
        try {
            System.out.println("Connecting to server...");
            //connect to server
            connection.connect();

            System.out.println("Checking range of response code...");
            //if not success(200 range)
            if (connection.getResponseCode() < 200 && connection.getResponseCode() >= 300) {
                System.out.println("Error response code " + connection.getResponseCode());
                error();
            }

            System.out.println("Checking valid content length...");
            //check for valid length
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                System.out.println("Not valid length.");
                error();
            }
            System.out.println("Server connected.");

            System.out.println("Setting Download size");
            //set download size
            if (size == -1) {
                size = contentLength;
                System.out.println("size changed...");
                System.out.println("Calling stateChanged()..");
                stateChanged();
            }

            System.out.println("Opening a file...");
            //open a file and seek to the end of it.
            file = new RandomAccessFile(getFileName(url), "rw");
            System.out.println("File opened in rw.");
            file.seek(downloaded);
            System.out.println("file Seek.");

            System.out.println("getting input stream...");
            stream = connection.getInputStream();
            System.out.println("got input stream.");
            while (status == DOWNLOADING) {
                //Hold the bytes that will be downloaded.
                byte buffer[];

                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                    System.out.println("New buffer of max_size...");
                } else {
                    buffer = new byte[size - downloaded];
                    System.out.println("last buffer");
                }

                //Read from server into buffer.
                System.out.println("Reading from server...");
                int read = stream.read(buffer);
                System.out.println("Read from server.");
                if (read == -1) {
                    System.out.println("Reading COMPLETE.breaking from loop...");
                    break;
                }

                //Write buffer to file
                System.out.println("Writing buffer to file...");
                file.write(buffer, 0, read);
                System.out.println("File written.");
                downloaded += read;
                System.out.println("downloaded value changed\ncalling stateChanged()...");
                stateChanged();
            }

            System.out.println("Changing status to finished...");
            //change the status to finished
            if (status == DOWNLOADING) {
                status = COMPLETE;
                System.out.println("Calling stateChanged()...");
                stateChanged();
            }
        } catch (Exception e) {
            System.out.println("(Download class)Exception caught " + e);
            error();
        } finally {
            System.out.println("Closing file...");
            //close file.
            if (file != null) {
                try {
                    file.close();
                    System.out.println("Closed file successfully.");
                } catch (Exception e) {
                    System.out.println("Couldn't close file" + e);
                }
            }

            System.out.println("Closing stream...");
            if (stream != null) {
                try {
                    stream.close();
                    System.out.println("Closed stream successful.");
                } catch (Exception e) {
                    System.out.println("Couldn't close stream.");
                }
            }
        }
    }

    private void doHttpsStuff(HttpsURLConnection connection) {
        System.out.println("<<<<<<<<<<<<<<HTTPS CONNECTION>>>>>>>>>>>>>>>");
        RandomAccessFile file = null;
        InputStream stream = null;
        System.out.println("RAF and InputStream variables initialized...");
        //Specifying what portion of file to Download
        connection.setRequestProperty("Range", "bytes=" + downloaded + "-");
        System.out.println("Specified what portion of file to download...");
        try {
            System.out.println("Connecting to server...");
            //connect to server
            connection.connect();

            System.out.println("Checking range of response code...");
            //if not success(200 range)
            if (connection.getResponseCode() < 200 && connection.getResponseCode() >= 300) {
                System.out.println("Error response code " + connection.getResponseCode());
                error();
            }

            System.out.println("Checking valid content length...");
            //check for valid length
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                System.out.println("Not valid length.");
                error();
            }
            System.out.println("Server connected.");

            System.out.println("Setting Download size");
            //set download size
            if (size == -1) {
                size = contentLength;
                System.out.println("size changed...");
                System.out.println("Calling stateChanged()..");
                stateChanged();
            }

            System.out.println("Opening a file...");
            //open a file and seek to the end of it.
            file = new RandomAccessFile(getFileName(url), "rw");
            System.out.println("File opened in rw.");
            file.seek(downloaded);
            System.out.println("file Seek.");

            System.out.println("getting input stream...");
            stream = connection.getInputStream();
            System.out.println("got input stream.");
            while (status == DOWNLOADING) {
                //Hold the bytes that will be downloaded.
                byte buffer[];

                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                    System.out.println("New buffer of max_size...");
                } else {
                    buffer = new byte[size - downloaded];
                    System.out.println("last buffer");
                }

                //Read from server into buffer.
                System.out.println("Reading from server...");
                int read = stream.read(buffer);
                System.out.println("Read from server.");
                if (read == -1) {
                    System.out.println("Reading COMPLETE.breaking from loop...");
                    break;
                }

                //Write buffer to file
                System.out.println("Writing buffer to file...");
                file.write(buffer, 0, read);
                System.out.println("File written.");
                downloaded += read;
                System.out.println("downloaded value changed\ncalling stateChanged()...");
                stateChanged();
            }

            System.out.println("Changing status to finished...");
            //change the status to finished
            if (status == DOWNLOADING) {
                status = COMPLETE;
                System.out.println("Calling stateChanged()...");
                stateChanged();
            }
        } catch (Exception e) {
            System.out.println("(Download class)Exception caught " + e);
            error();
        } finally {
            System.out.println("Closing file...");
            //close file.
            if (file != null) {
                try {
                    file.close();
                    System.out.println("Closed file successfully.");
                } catch (Exception e) {
                    System.out.println("Couldn't close file" + e);
                }
            }

            System.out.println("Closing stream...");
            if (stream != null) {
                try {
                    stream.close();
                    System.out.println("Closed stream successful.");
                } catch (Exception e) {
                    System.out.println("Couldn't close stream.");
                }
            }
        }
    }

    private void stateChanged () {
        System.out.println("Enter stateChanged()...");
        System.out.println("Calling setChanged()...");
        //to flag the class as changed
        try {
            setChanged();
            System.out.println("setChanged() successful.");
        } catch (Exception e) {
            System.out.println("setChanged() Failed..." + e);
        }
        System.out.println("Notifying Observers...");
            //notify all those who have subscribed to receive notifications
        try {
            notifyObservers();
            System.out.println("Notify successful.");
        } catch (Exception e) {
            System.out.println("Notify failed." + e);
        }
    }

}

