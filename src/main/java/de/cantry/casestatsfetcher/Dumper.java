package de.cantry.casestatsfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.javafx.image.impl.ByteIndexed;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.cantry.casestatsfetcher.Utils.httpGet;
import static de.cantry.casestatsfetcher.Utils.regexFindFirst;

public class Dumper {

    private File dumpDirectory = new File("dumps" + File.separator);
    private String cookies;
    private String url;
    private int fails = 0;
    private List<Long> allDumpTimeStamps = new ArrayList<>();

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public void dumpFromTime(long startTime, boolean startFromNow) throws Exception {

        long time = startTime;
        String time_frac = "0";
        String s = "0";
        String sessionid;
        String appid = "730";

        boolean moreItems;

        String base = httpGet("https://steamcommunity.com/my/inventoryhistory", cookies, true);

        url = regexFindFirst("href=\"(https://steamcommunity.com/[^/]+/[^/]+/inventoryhistory/)\"", base);
        sessionid = regexFindFirst("g_sessionID = \"([^\"]+)\"", base);

        System.out.println("Url:" + url);
        System.out.println("SessionId:" + sessionid);

        if (!dumpDirectory.exists()) {
            dumpDirectory.mkdirs();
        }
        Gson gson = new Gson();

        do {
            String getUrl = url + "?ajax=1&cursor%5Btime%5D=" + time + "&cursor%5Btime_frac%5D=" + time_frac + "&cursor%5Bs%5D=" + s + "&sessionid=" + sessionid + "&app%5B%5D=" + appid;

            String res = httpGet(getUrl, cookies, false);

            try {
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

            } catch (Exception e) {
                e.printStackTrace();
                if (fails < 10) {
                    fails++;
                    System.out.println("failed " + fails + "/10 Retrying");
                    moreItems = true;
                    Thread.sleep(10000);
                } else {
                    moreItems = false;
                }
            }

            Thread.sleep(2500);
        } while (moreItems);

        if (fails == 10) {
            System.out.println("Failed to dump all :(");
        } else {
            System.out.println("Finished dumping");
        }

    }

    public long lookForOldDumps() throws IOException {
        long oldestDump = 99999999999L;

        if (!dumpDirectory.exists()) {
            return oldestDump;
        }

        Gson gson = new Gson();
        for (File dump : Objects.requireNonNull(dumpDirectory.listFiles((dir, name) -> name.endsWith("dump")))) {
            JsonObject obj = gson.fromJson(String.join("", Files.readAllLines(dump.toPath())), JsonObject.class);
            if (obj.get("cursor") == null) {
                continue;
            }
            long timeStamp = obj.get("cursor").getAsJsonObject().get("time").getAsLong();
            if (timeStamp < oldestDump) {
                oldestDump = timeStamp;
            }
            allDumpTimeStamps.add(timeStamp);
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
