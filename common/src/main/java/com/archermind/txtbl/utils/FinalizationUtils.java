package com.archermind.txtbl.utils;

import org.apache.pdfbox.pdmodel.PDDocument;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.mail.Folder;
import javax.mail.Store;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;


public class FinalizationUtils {
    public static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void close(Store store) {
        try {
            if (store != null) {
                store.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void close(PDDocument document) {
        try {
            if (document != null) document.close();
        } catch (Throwable ignored) {

        }
    }

    public static void close(javax.jms.Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (Throwable ignored) {

            }
        }

    }

    public static void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (Throwable ignored) {

            }
        }
    }

    public static void close(Folder inbox) {
        if (inbox != null) {
            try {
                inbox.close(false);
            } catch (Throwable ignored) {

            }
        }
    }

    public static void close(ExecutorService executor) {
        if (executor != null) {
            try {
                executor.shutdown();
            } catch (Throwable ignored) {

            }
        }
    }

    public static void close(HttpURLConnection conn) {
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Throwable ignored) {

            }
        }
    }

    public static void close(Thread thread, boolean interruptCurrentThreadUponExit) {
        if (thread != null && interruptCurrentThreadUponExit) {
            try {
                thread.interrupt();
            } catch (Throwable ignored) {

            }
        }
    }

    public static void close(MessageProducer messageProducer) {
        if (messageProducer != null) {
            try {
                messageProducer.close();
            } catch (Throwable ignored) {

            }
        }
    }
}
