package eu.bebanana;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    static String hook = null;

    public static void log(String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()) + " - " + message);
    }

    public static void report(String message, Throwable t) {
        report(message, throwableToString(t));
    }

    public static void report(String message) {
        report(message, (String) null);
    }

    private static String throwableToString(Throwable t) {
        String string = t.toString() + "\n";

        StackTraceElement[] trace = t.getStackTrace();
        for (StackTraceElement traceElement : trace) {
            string = string + "\tat " + traceElement + "\n";
        }

        // Print cause, if any
        Throwable cause = t.getCause();
        if (cause != null) {
            string = string + "Caused by: " + cause.toString();
            StackTraceElement[] causeTrace = cause.getStackTrace();
            for (StackTraceElement traceElement : causeTrace) {
                string = string + "\tat " + traceElement + "\n";
            }
        }
        return string;
    }

    private static void report(String message, String error) {
        if(hook == null) {
            throw new RuntimeException("You cannot send a report without a webhook. Please use `Basics.init(<Your Hook>)`before using the reporting methods.");
        }

        HttpPost httpPost = null;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpPost = new HttpPost(hook);
            String json;
            if(error != null) {
                json = "{\"text\":\"" + message + "\n" + error + "\"}";
            } else {
                json = "{\"text\":\"" + message + "\"}";
            }
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-type", "application/json");
            httpClient.execute(httpPost);
        } catch (IOException e) {
            log("Error while reporting bug");
            e.printStackTrace();
        } finally {
            if(httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }
}
