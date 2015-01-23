package org.dhis2.messaging.Utils.REST;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RESTClient {
    private RESTClient() {
    }

    public static Response get(String api, String userCredentials) {
        int code = -1;
        String body = "";

        HttpURLConnection connection = null;
        try {
            URL url = new URL(api);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(1500);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            code = connection.getResponseCode();
            body = readInputStream(connection.getInputStream());
        }
        catch (MalformedURLException e) {
            code = HttpURLConnection.HTTP_NOT_FOUND;
            e.printStackTrace();
        }
        catch (IOException one) {
            one.printStackTrace();
            try {
                if (connection != null) {
                    code = connection.getResponseCode();
                }
            } catch (IOException two) {
                two.printStackTrace();
            }
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
            connection.setConnectTimeout(1500);
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
            connection.setConnectTimeout(1500);
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
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return (new Response(code, body));
    }

    public static int delete(String server, String userCredentials) {
        int code = -1;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(server);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(1500);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestMethod("DELETE");
            connection.connect();

            code = connection.getResponseCode();
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
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return code;
    }

    public static Bitmap getPicture(String api, String userCredentials) {
        Bitmap bitmap = null;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(api);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(1500);
            connection.setRequestProperty("Authorization", "Basic " + userCredentials);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException one) {
            one.printStackTrace();

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
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static boolean noErrors(int code) {
        return code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_NO_CONTENT;
    }

   public static String getErrorMessage( int code) {
        switch (code) {
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                return "Wrong username or password";
            case HttpURLConnection.HTTP_NOT_FOUND:
                return "Wrong URL";
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                return "Internal server error, server might be down at the moment. Try again later..";
            case 503:
                return "Service unavailable, server might be down at the moment. Try again later..";
            default:
                return "Something went wrong, try again";
        }
    }
}