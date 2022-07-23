package de.cantry.casestatsfetcher;

import com.google.gson.JsonObject;

import java.util.Objects;

public class Item {

    private JsonObject description;

    private long assetID;

    private String descriptionKey;

    public JsonObject getDescription() {
        return description;
    }

    public void setDescription(JsonObject description) {
        this.description = description;
    }

    public long getAssetID() {
        return assetID;
    }

    public void setAssetID(long assetID) {
        this.assetID = assetID;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public void setDescriptionKey(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return (assetID == item.assetID) || Objects.equals(description, item.description) && Objects.equals(descriptionKey, item.descriptionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, assetID, descriptionKey);
    }
}
