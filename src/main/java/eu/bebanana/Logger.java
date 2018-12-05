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

    public static void log(String message, Throwable t) {
        System.out.println(makeMessage(message, t));
    }

    public static void log(String message) {
        log(message, null);
    }

    public static void report(String message, Boolean shouldLog) {
        report(message, null, shouldLog);
    }

    public static void report(String message, Throwable t) {
        report(message, t, true);
    }

    public static void report(String message) {
        report(message, null, true);
    }

    public static void report(String message, Throwable t, Boolean shouldLog) {
        if(hook == null) {
            throw new RuntimeException("You cannot send a report without a webhook. Please use `Basics.init(<Your Hook>)`before using the reporting methods.");
        }

        if(shouldLog) {
            log(message, t);
        }

        HttpPost httpPost = null;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            httpPost = new HttpPost(hook);
            String json;
            json = "{\"text\":\"" + makeMessage(message, t) + "\"}";
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-type", "application/json");
            httpClient.execute(httpPost);
        } catch (IOException e) {
            log("Error while reporting bug", e);
        } finally {
            if(httpPost != null) {
                httpPost.releaseConnection();
            }
        }
    }

    private static String throwableToString(Throwable t) {
        if(t == null) {
            return "";
        }

        String string = t.toString() + "\n";

        StackTraceElement[] trace = t.getStackTrace();
        for (StackTraceElement traceElement : trace) {
            string = string + "\tat " + traceElement + "\n";
        }

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

    private static String makeMessage(String message, Throwable t) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return "\n" + simpleDateFormat.format(new Date()) + "\n" + message + "\n" + throwableToString(t);
    }
}
