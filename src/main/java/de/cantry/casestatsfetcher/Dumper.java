package de.cantry.casestatsfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.cantry.casestatsfetcher.Utils.httpGet;
import static de.cantry.casestatsfetcher.Utils.regexFindFirst;

public class Dumper {

    private File dumpDirectory = new File("dumps" + File.separator);
    private File cookieCache = new File("cookie");
    private String cookies;
    private String url;
    private int fails = 0;
    private List<Long> allDumpTimeStamps = new ArrayList<>();

    public boolean areCookiesCached() {
        return cookieCache.exists();
    }
    public boolean loadCachedCookies() {
        try {
            this.cookies = Files.readString(cookieCache.toPath(), StandardCharsets.UTF_8);
            return true;
        } catch(IOException e) {
            return false;
        }
    }
    public void setAndWriteCookies(String cookies) {
        this.cookies = cookies;

        try {
            Files.writeString(cookieCache.toPath(), cookies, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch(IOException e) {
            System.err.printf("Error caching cookies:\n%s\nContinuing without writing.\n",e.getLocalizedMessage());
        }
    }

    public void dumpFromTime(long startTime, boolean startFromNow) {

        File zeroDump = new File(dumpDirectory + File.separator + "0.dump");
        if(zeroDump.exists()){
            zeroDump.delete();
        }

        long time = startTime;
        String time_frac = "0";
        String s = "0";
        String sessionid;
        String appid = "730";

        boolean moreItems = false;

        try {
            String base = httpGet("https://steamcommunity.com/my", cookies, true);
            url = regexFindFirst("href=\"(https://steamcommunity.com/[^/]+/[^/]+/inventory/)\"", base).replace("inventory","inventoryhistory");
            sessionid = regexFindFirst("g_sessionID = \"([^\"]+)\"", base);
        } catch (IOException e) {
            System.err.printf("Page Parsing Error! Steam may be down or your cookies may have expired.\n");
            return;
        } catch (PatternSyntaxException e) {
            System.err.printf("Cookie parsing error, pleaes re-enter your cookies.\n");
            return;
        }

        System.out.println("Url:" + url);
        System.out.println("SessionId:" + sessionid);

        if (!dumpDirectory.exists()) {
            dumpDirectory.mkdirs();
        }
        Gson gson = new Gson();

        do {
            String getUrl = url + "?ajax=1&cursor%5Btime%5D=" + time + "&cursor%5Btime_frac%5D=" + time_frac + "&cursor%5Bs%5D=" + s + "&sessionid=" + sessionid + "&app%5B%5D=" + appid;


            try {
                String res = httpGet(getUrl, cookies, false);
                JsonObject element = gson.fromJson(res, JsonObject.class);

                moreItems = element.get("num").getAsString().equals("50");

                String currentDate = regexFindFirst("tradehistory_date\\\\\">([^<]+)<", res).replace("\\t", "").replace("\\r", "").replace("\\n", "");

                if (moreItems && element.get("cursor") != null) {
                    JsonObject cursor = element.get("cursor").getAsJsonObject();
                    s = cursor.get("s").getAsString();
                    time_frac = cursor.get("time_frac").getAsString();
                    time = cursor.get("time").getAsLong();
                    fails = 0;
                } else {
                    time = 0;
                }

                File dumpFile = new File(dumpDirectory.getAbsolutePath() + File.separator + time + ".dump");

                if (dumpFile.exists() || (startFromNow && !olderDumpExits(time))) {
                    moreItems = false;
                } else {
                    System.out.println("Dumped " + time + " Date:" + currentDate);
                    Files.write(dumpFile.toPath(), Stream.of(res).collect(Collectors.toList()), StandardOpenOption.CREATE_NEW);
                }

            } catch (IOException e) {
                if(e.getLocalizedMessage().contains("HTTP response code: 429")) {
                    fails = 10;
                    System.err.println("Looks like you've been rate limited! Either wait about 12 hours before continuing, or use a VPN as a workaround.");
                    System.err.println("Giving up.");
                } else { e.printStackTrace(); }
            } catch (Exception e) {
                System.out.printf("There was an error: %s\n", e.getLocalizedMessage());
                fails++;
                System.out.printf("Failed %d/10. %s\n", fails, fails < 10 ? "Retrying" : "Giving Up");
            }
            
            try {
                Thread.sleep(3000);
            } catch(InterruptedException e) {

            }
        } while (moreItems || fails >= 10);

    }

    public long lookForOldDumps() {
        long oldestDump = 99999999999L;

        if (!dumpDirectory.exists()) {
            return oldestDump;
        }

        Gson gson = new Gson();
        for (File dump : Objects.requireNonNull(dumpDirectory.listFiles((dir, name) -> name.endsWith("dump")))) {
            try {
                JsonObject obj = gson.fromJson(String.join("", Files.readAllLines(dump.toPath())), JsonObject.class);
                if (obj.get("cursor") == null) {
                    continue;
                }
                long timeStamp = obj.get("cursor").getAsJsonObject().get("time").getAsLong();
                if (timeStamp < oldestDump) {
                    oldestDump = timeStamp;
                }
                allDumpTimeStamps.add(timeStamp);
            } catch(IOException e) {
                System.err.printf("Error parsing dump file %s\n", dump.toString());
            }
        }
        return oldestDump;
    }

    public boolean olderDumpExits(long timeStamp) {
        for (long oldTimestamp : allDumpTimeStamps) {
            if (timeStamp > oldTimestamp) {
                return true;
            }
        }
        return false;
    }

    public File getDumpDirectory() {
        return dumpDirectory;
    }
}
