package de.cantry.casestatsfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.String.join;

public class AnalyserInventoryHistory {

    private File resultFile;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, yyyy h:mma", Locale.ENGLISH);

    public void outPutStats(File path) throws IOException {
        Gson gson = new Gson();

        List<InventoryChangeEntry> inventoryChangeEntries = new ArrayList<>();
        logToConsoleAndFile("Sorted by time");

        File[] files = path.listFiles();

        if (files == null || files.length == 0) {
            logToConsoleAndFile("No dumps found. You have to dump your history first");
            return;
        }

        for (File f : files) {
            //Not pretty but better then nullchecking every field
            try {
                JsonObject obj = gson.fromJson(join("", Files.readAllLines(f.toPath())), JsonObject.class);
                String currentDumpHTML = obj.get("html").getAsString();
                Document currentDoc = Jsoup.parse(currentDumpHTML);
                HashMap<String, JsonObject> descriptionsByKey = new HashMap<>();
                JsonObject descriptions = obj.get("descriptions").getAsJsonObject();

                for (Map.Entry<String, JsonElement> description : descriptions.get("730").getAsJsonObject().entrySet()) {
                    descriptionsByKey.put(description.getKey(), description.getValue().getAsJsonObject());
                }

                List<Element> rows = currentDoc.getElementsByClass("tradehistoryrow");
                Collections.reverse(rows);
                for (Element tradehistoryrow : rows) {
                    InventoryChangeEntry inventoryChangeEntry = new InventoryChangeEntry();
                    inventoryChangeEntry.setEvent(tradehistoryrow.getElementsByClass("tradehistory_event_description").text());
                    if (tradehistoryrow.getElementsByTag("a").size() > 0) {
                        inventoryChangeEntry.setPartner(tradehistoryrow.getElementsByTag("a").get(0).attr("href"));
                    }
                    String dateString = tradehistoryrow.getElementsByClass("tradehistory_date").get(0).text().replace("am", "AM").replace("pm", "PM").replace("\\t", "").replace("\\r", "").replace("\\n", "");
                    ZonedDateTime dateTime = ZonedDateTime.parse(dateString, dateTimeFormatter.withZone(ZoneId.of("UTC")));
                    inventoryChangeEntry.setTime(dateTime.toInstant().getEpochSecond());

                    List<Element> itemGroups = tradehistoryrow.getElementsByClass("tradehistory_items");

                    for (Element items : itemGroups) {
                        if (items.getElementsByClass("tradehistory_items_plusminus").size() == 0) {
                            continue;
                        }
                        String direction = items.getElementsByClass("tradehistory_items_plusminus").get(0).text();
                        Boolean incoming = null;
                        if ("-".equals(direction)) {
                            incoming = false;
                        } else if ("+".equals(direction)) {
                            incoming = true;
                        } else if (itemGroups.size() == 3) {
                            incoming = true;
                        } else {
                            throw new Exception("Failed to parse direction: " + direction);
                        }
                        //items
                        List<Item> parsedItems = new ArrayList<>();
                        for (Element item : items.getElementsByClass("tradehistory_items_group").get(0).children()) {

                            String appid = item.attr("data-appid");
                            String classid = item.attr("data-classid");
                            String instanceid = item.attr("data-instanceid");
                            if (!"730".equals(appid)) {
                                continue;
                            }

                            String descriptionKey = classid + "_" + instanceid;
                            JsonObject description = descriptionsByKey.get(descriptionKey);

                            long assetID = 0;
                            if (item.hasAttr("href")) {
                                assetID = Long.parseLong(item.attr("href").split("inventory/#730_2_")[1]);
                            }
                            Item parsedItem = new Item();
                            parsedItem.setDescription(description);
                            parsedItem.setDescriptionKey(descriptionKey);
                            parsedItem.setAssetID(assetID);
                            parsedItems.add(parsedItem);

                        }
                        if (incoming) {
                            inventoryChangeEntry.setItemsAdded(parsedItems);
                        } else {
                            inventoryChangeEntry.setItemsRemoved(parsedItems);
                        }
                    }

                    System.out.println((inventoryChangeEntry.toString()));

                    inventoryChangeEntries.add(inventoryChangeEntry);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logToConsoleAndFile("Dump with name " + f.getName() + " seems to be corrupted");
            }

        }

        for (int i = 0; i < inventoryChangeEntries.size(); i++) {
            InventoryChangeEntry inventoryChangeEntry = inventoryChangeEntries.get(i);
            try {
                logToConsoleAndFile(Instant.ofEpochSecond(inventoryChangeEntry.getTime()).toString());
                logToConsoleAndFile("Event:" + inventoryChangeEntry.getEvent());
                logToConsoleAndFile("+");
            } catch (IOException e) {
                e.printStackTrace();
            }
            inventoryChangeEntry.getItemsAdded().forEach(item -> {
                try {
                    logToConsoleAndFile(item.getDescription().get("market_hash_name").getAsString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            try {
                logToConsoleAndFile("-");
            } catch (IOException e) {
                e.printStackTrace();
            }
            inventoryChangeEntry.getItemsRemoved().forEach(item -> {
                try {
                    logToConsoleAndFile(item.getDescription().get("market_hash_name").getAsString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }

        Files.write(getCurrentResultJsonFile().toPath(), ("let data = " + gson.toJson(inventoryChangeEntries) + System.lineSeparator()).getBytes(), StandardOpenOption.CREATE_NEW);
        Files.copy(getCurrentResultJsonFile().toPath(), new File("resultdata.js").toPath());

        System.out.println("Open ResultViewer.html to see your results");

    }

    private void logToConsoleAndFile(String msg) throws IOException {
        if (!getCurrentResultFile().exists()) {
            getCurrentResultFile().createNewFile();
        }
        System.out.println(msg);
        Files.write(getCurrentResultFile().toPath(), (msg + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    private File getCurrentResultFile() {
        if (resultFile == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
            Date dt = new Date();
            String date = sdf.format(dt);

            resultFile = new File("result_" + date + ".txt");
        }
        return resultFile;
    }

    private File getCurrentResultJsonFile() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        Date dt = new Date();
        String date = sdf.format(dt);

        return new File("js-history-" + date + ".json");
    }

}
