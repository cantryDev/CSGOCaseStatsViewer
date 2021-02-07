package de.cantry.casestatsfetcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cantry.casestatsfetcher.Utils.regexFindAll;
import static de.cantry.casestatsfetcher.Utils.regexFindFirst;
import static java.util.stream.Collectors.toMap;

public class Analyser {

    private final HashMap<String, Double> officialOdds = new LinkedHashMap<>();

    public Analyser() {
        officialOdds.put("Blue", 79.92);
        officialOdds.put("Purple", 15.98);
        officialOdds.put("Pink", 3.20);
        officialOdds.put("Red", 0.64);
        officialOdds.put("Yellow", 0.26);
    }

    public void outPutStats(File path) throws IOException {
        Gson gson = new Gson();

        List<String> duplicatePreventer = new ArrayList<>();

        int unlockedContainers = 0;

        HashMap<String, HashMap<String, Integer>> cases = new HashMap<>();
        HashMap<String, List<String>> rarities = new HashMap<>();

        logToConsoleAndFile("Sorted by time");

        for (File f : Objects.requireNonNull(path.listFiles())) {
            JsonObject obj = gson.fromJson(String.join("", Files.readAllLines(f.toPath())), JsonObject.class);
            String currentDumpHTML = obj.get("html").getAsString();
            HashMap<String, JsonObject> itemNames = new HashMap<>();
            JsonObject descriptions = obj.get("descriptions").getAsJsonObject();

            for (Map.Entry<String, JsonElement> description : descriptions.get("730").getAsJsonObject().entrySet()) {
                itemNames.put(description.getKey(), description.getValue().getAsJsonObject());
            }

            String[] rows = currentDumpHTML.split("tradehistoryrow");

            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                List<String> appids = regexFindAll("data-appid=\"([^\"]+)\"", row);
                boolean isCSGORow = true;
                for (String appid : appids) {
                    if (!appid.equals("730")) {
                        isCSGORow = false;
                        break;
                    }
                }
                if (!isCSGORow) {
                    continue;
                }
                List<String> classids = regexFindAll("data-classid=\"([^\"]+)\"", row);
                List<String> instances = regexFindAll("data-instanceid=\"([^\"]+)\"", row);
                if (classids.size() == 3 && instances.size() == 3) {
                    String caseId = classids.get(0) + "_" + instances.get(0);
                    String keyId = classids.get(1) + "_" + instances.get(1);
                    String itemId = classids.get(2) + "_" + instances.get(2);
                    String caseName = itemNames.get(caseId).get("market_hash_name").getAsString();
                    String itemName = itemNames.get(itemId).get("market_hash_name").getAsString();
                    String keyName = itemNames.get(keyId).get("market_hash_name").getAsString();
                    if (keyName.toLowerCase().contains("case key") && caseName.toLowerCase().contains("case") && !caseName.toLowerCase().contains("key")) {
                        String assetidFromItemId;

                        try {
                            assetidFromItemId = regexFindFirst("inventory/#730_2_([0-9]+)\"", row);
                        } catch (Exception e) {
                            logToConsoleAndFile("No assetid for " + itemName + " found");
                            e.printStackTrace();
                            continue;
                        }

                        String itemRarity = "";
                        boolean unusual = false;
                        for (JsonElement element : itemNames.get(itemId).get("tags").getAsJsonArray()) {
                            JsonObject object = element.getAsJsonObject();
                            if (object.get("category").getAsString().equals("Rarity")) {
                                itemRarity = object.get("internal_name").getAsString();
                            } else if (object.get("category").getAsString().equals("Quality")) {
                                if (object.get("internal_name").getAsString().equals("unusual")) {
                                    unusual = true;
                                }
                            }
                        }
                        if (unusual && itemRarity.equals("Rarity_Legendary_Weapon")) {
                            itemRarity = "Yellow";
                        }

                        switch (itemRarity) {
                            case "Rarity_Rare_Weapon":
                                itemRarity = "Blue";
                                break;
                            case "Rarity_Mythical_Weapon":
                                itemRarity = "Purple";
                                break;
                            case "Rarity_Legendary_Weapon":
                                itemRarity = "Pink";
                                break;
                            case "Rarity_Ancient_Weapon":
                                itemRarity = "Red";
                                break;
                        }


                        if (duplicatePreventer.contains(assetidFromItemId)) {
                            System.out.println(itemName + " with id " + assetidFromItemId + " allready found");
                            continue;
                        }
                        duplicatePreventer.add(assetidFromItemId);
                        rarities.putIfAbsent(itemRarity, new ArrayList<>());
                        rarities.get(itemRarity).add(itemName);
                        cases.computeIfAbsent(caseName, k -> new HashMap<>());
                        logToConsoleAndFile(caseName + "->" + itemName + " (" + itemRarity + ")");
                        cases.get(caseName).merge(itemName, 1, Integer::sum);
                        unlockedContainers++;
                    }
                }

            }
        }

        logToConsoleAndFile("");
        logToConsoleAndFile("Sorted by case");
        for (Map.Entry<String, HashMap<String, Integer>> caseEntry : cases.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(e -> e.getValue().size())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)).entrySet()) {
            logToConsoleAndFile("");
            logToConsoleAndFile(caseEntry.getKey() + " Stats");
            for (Map.Entry<String, Integer> skinEntries : caseEntry.getValue().entrySet()) {
                logToConsoleAndFile(skinEntries.getKey() + ":" + skinEntries.getValue());
            }
        }

        logToConsoleAndFile("");
        logToConsoleAndFile("Sorted by rarity");
        for (Map.Entry<String, List<String>> rarityEntry : rarities.entrySet().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(e -> e.getValue().size())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new)).entrySet()) {
            logToConsoleAndFile("");
            logToConsoleAndFile(rarityEntry.getKey());
            for (String item : rarityEntry.getValue()) {
                logToConsoleAndFile(item);
            }
        }

        logToConsoleAndFile("");
        logToConsoleAndFile("Total unboxed cases " + unlockedContainers);


        logToConsoleAndFile("");
        logToConsoleAndFile("Item distribution and odds calulation");
        logToConsoleAndFile("Rarity name | Yours | Official");
        logToConsoleAndFile("-------------------------------------");

        int sizeForAmountAndTotal = 0;

        for (Map.Entry<String, Double> officialOdd : officialOdds.entrySet()) {

            String rarityName = officialOdd.getKey();
            String amountAndTotal = rarities.getOrDefault(officialOdd.getKey(), new ArrayList<>()).size() + "/" + unlockedContainers;
            if (sizeForAmountAndTotal == 0) {
                sizeForAmountAndTotal = amountAndTotal.length();
            }
            String calculatedOdds = round((rarities.getOrDefault(officialOdd.getKey(), new ArrayList<>()).size() / (double) unlockedContainers) * 100, 2) + "";
            logToConsoleAndFile(format(rarityName, 6, false) + " | " + format(amountAndTotal, sizeForAmountAndTotal, true) + " (~" + format(calculatedOdds, 6, true) + " %) | " + format(officialOdd.getValue() + "", 5, true) + "%");
        }


        System.out.println("");
        System.out.println("Saved your results to " + getCurrentResultFile().getAbsolutePath());
    }

    private void logToConsoleAndFile(String msg) throws IOException {
        if (!getCurrentResultFile().exists()) {
            getCurrentResultFile().createNewFile();
        }
        System.out.println(msg);
        Files.write(getCurrentResultFile().toPath(), (msg + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    private File getCurrentResultFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm");
        Date dt = new Date();
        String date = sdf.format(dt);

        return new File("result_" + date + ".txt");
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private String format(String input, int toLength, boolean inFront) {
        StringBuilder inputBuilder = new StringBuilder(input);
        while (inputBuilder.length() < toLength) {
            if (inFront) {
                inputBuilder.insert(0, " ");
            } else {
                inputBuilder.append(" ");
            }

        }
        input = inputBuilder.toString();
        return input;
    }

}
