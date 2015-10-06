package org.dhis2.messaging.REST;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

public class RESTClient {
    private final static int IO_EXCEPTION = 10;
    private final static int SOCKET_TIMEOUT_EXCEPTION = 11;
    private final static int OTHER_EXCEPTION = 12;
    private final static int SSL_HANDSHAKE_EXCEPTION = 13;
    private final static int SERVICE_UNAVAILABLE_EXCEPTION = 503;
    public final static int JSON_EXCEPTION = 14;
    public final static int MALFORMED_URL_EXCEPTION = 15;

    private RESTClient() {
        //disableSslVerification();
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static Response get(String api, String userCredentials) {
        int code = -1;
        String body = "";

        // if (api.contains("197.243.37.125") || api.contains("10.10.35.207 "))
        //   disableSslVerification();
        //disableSslVerification();

        HttpURLConnection connection = null;
        try {
            URL url = new URL(api);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            code = connection.getResponseCode();
            body = readInputStream(connection.getInputStream());
        } catch (SSLHandshakeException e) {
            code = SSL_HANDSHAKE_EXCEPTION;
        } catch (MalformedURLException e) {
            code = HttpURLConnection.HTTP_NOT_FOUND;
        } catch (SocketTimeoutException e) {
            code = HttpURLConnection.HTTP_GATEWAY_TIMEOUT;
        } catch (IOException one) {
            try {
                if (connection != null) {
                    code = connection.getResponseCode();
                } else
                    code = IO_EXCEPTION;
            } catch (IOException two) {
                code = IO_EXCEPTION;
            }
        } catch (Exception e) {
            code = OTHER_EXCEPTION;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return (new Response(code, body));
    }

    public static Response post(String server, String userCredentials, String data, String contentType) {
        int code = -1;
        String body = "";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(server);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream output = connection.getOutputStream();
            output.write(data.getBytes());
            output.close();

            connection.connect();
            code = connection.getResponseCode();
            body = readInputStream(connection.getInputStream());
        } catch (SocketTimeoutException e) {
            code = SOCKET_TIMEOUT_EXCEPTION;
        } catch (MalformedURLException e) {
            code = HttpURLConnection.HTTP_NOT_FOUND;
        } catch (IOException one) {
            try {
                if (connection != null) {
                    code = connection.getResponseCode();
                } else
                    code = IO_EXCEPTION;
            } catch (IOException two) {
                code = IO_EXCEPTION;
            }
        } catch (Exception e) {
            code = OTHER_EXCEPTION;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return (new Response(code, body));
    }

    public static Response put(String server, String userCredentials, String data, String contentType) {
        int code = -1;
        String body = "";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(server);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);

            OutputStream output = connection.getOutputStream();
            output.write(data.getBytes());
            output.close();

            connection.connect();
            code = connection.getResponseCode();
            body = readInputStream(connection.getInputStream());
        } catch (SocketTimeoutException e) {
            code = SOCKET_TIMEOUT_EXCEPTION;
        } catch (MalformedURLException e) {
            code = HttpURLConnection.HTTP_NOT_FOUND;
            e.printStackTrace();
        } catch (IOException one) {
            one.printStackTrace();
            try {
                if (connection != null) {
                    code = connection.getResponseCode();
                }
            } catch (IOException two) {
                two.printStackTrace();
            }
        } catch (Exception e) {
            code = OTHER_EXCEPTION;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return (new Response(code, body));
    }

    public static Response delete(String server, String userCredentials, String contentType) {
        int code = -1;
        String body = "";
        HttpURLConnection connection = null;
        try {
            URL url = new URL(server);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestMethod("DELETE");
            connection.connect();

            connection.connect();
            code = connection.getResponseCode();
            body = readInputStream(connection.getInputStream());
        }  catch (SocketTimeoutException e) {
            code = SOCKET_TIMEOUT_EXCEPTION;
        } catch (MalformedURLException e) {
            code = HttpURLConnection.HTTP_NOT_FOUND;
            e.printStackTrace();
        } catch (IOException one) {
            one.printStackTrace();
            try {
                if (connection != null) {
                    code = connection.getResponseCode();
                }
            } catch (IOException two) {
                two.printStackTrace();
            }
        } catch (Exception e) {
            code = OTHER_EXCEPTION;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new Response(code,body);
    }

    public static Bitmap getPicture(String api, String userCredentials) {
        Bitmap bitmap = null;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(api);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (IOException one) {
            one.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return bitmap;
    }

    private static String readInputStream(InputStream stream)
            throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream));
        try {
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            return builder.toString();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isDeviceConnectedToInternett(Context context) {
/*        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            return false;
        }*/
        return true;
    }

    public static boolean noErrors(int code) {
        return code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_NO_CONTENT;
    }

    public static String getErrorMessage(int code) {
        switch (code) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return "Wrong username or password";
            case HttpURLConnection.HTTP_NOT_FOUND:
                return "Wrong URL";
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return "Internal server error, server might be down at the moment. Try again later..";
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                return "Timeout exception. \n 1. Do you have airtime? \n 2. Try again when better internet connection";
            case SERVICE_UNAVAILABLE_EXCEPTION:
                return "Service unavailable, server might be down at the moment. Try again later..";
            case SSL_HANDSHAKE_EXCEPTION:
                return "SSLHandshakeException: No Trust anchor found on server";
            case IO_EXCEPTION:
                return "Something went wrong, try again";
            case SOCKET_TIMEOUT_EXCEPTION:
                return "Connection timeout - Try again";
            case MALFORMED_URL_EXCEPTION:
                return "The URL is not correct";
            case JSON_EXCEPTION:
                return "Something went wrong, try again";
            case OTHER_EXCEPTION:
                return "Something went wrong, try again";
            default:
                return "Something went wrong, try again";
        }
    }
}