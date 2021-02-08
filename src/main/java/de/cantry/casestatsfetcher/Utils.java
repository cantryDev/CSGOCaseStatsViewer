package de.cantry.casestatsfetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    public static String httpGet(String url,String cookies,boolean followRedirects) throws IOException {
        URL target = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) target.openConnection();


        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 7; WOW64; rv:51.0) Gecko/20100101 Firefox/51.0");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Accept-Language", "en-US;q=0.7,en;q=0.3");
        connection.setRequestProperty("Cookie", cookies);
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(followRedirects);

        InputStream response = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String result;
        while ((result = in.readLine()) != null) {
            sb.append(result);
        }

        in.close();

        return sb.toString();
    }

    public static String regexFindFirst(String pattern, String text) throws Exception {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1);
        }
        throw new Exception("RegexFindFirst, pattern not found. Searched: " + pattern);
    }

    public static List<String> regexFindAll(String pattern, String text) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        List<String> matches = new ArrayList<>();
        while (m.find()) {
            matches.add(m.group(1));
        }
        return matches;
    }

}
