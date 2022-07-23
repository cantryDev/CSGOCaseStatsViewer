package de.cantry.casestatsfetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryChangeEntry {

    private String event;

    private String partner;

    private long time;

    private List<Item> itemsAdded;

    private List<Item> itemsRemoved;

    public InventorySnapshot getInventorySnapshot(List<Item> items){
        InventorySnapshot inventorySnapshot = new InventorySnapshot();
        inventorySnapshot.setItems(items);
        inventorySnapshot.setLastEvent(this);
        inventorySnapshot.setTime(this.time);
        return inventorySnapshot;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<Item> getItemsAdded() {
        return itemsAdded == null ? new ArrayList<>() : itemsAdded;
    }

    public void setItemsAdded(List<Item> itemsAdded) {
        this.itemsAdded = itemsAdded;
    }

    public List<Item> getItemsRemoved() {
        return itemsRemoved  == null ? new ArrayList<>() : itemsRemoved;
    }

    public void setItemsRemoved(List<Item> itemsRemoved) {
        this.itemsRemoved = itemsRemoved;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryChangeEntry that = (InventoryChangeEntry) o;
        return time == that.time && Objects.equals(event, that.event) && Objects.equals(partner, that.partner) && Objects.equals(itemsAdded, that.itemsAdded) && Objects.equals(itemsRemoved, that.itemsRemoved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, partner, time, itemsAdded, itemsRemoved);
    }

    @Override
    public String toString() {
        return "InventoryChangeEntry{" +
                "event='" + event + '\'' +
                ", partner='" + partner + '\'' +
                ", time=" + time +
                ", itemsAdded=" + ((itemsAdded == null) ? 0 : itemsAdded.size()) +
                ", itemsRemoved=" + ((itemsRemoved == null) ? 0 : itemsRemoved.size()) +
                '}';
    }
}
