package de.cantry.casestatsfetcher;

import java.util.List;

public class InventorySnapshot {

    private long time;
    private List<Item> items;
    private InventoryChangeEntry lastEvent;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public InventoryChangeEntry getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(InventoryChangeEntry lastEvent) {
        this.lastEvent = lastEvent;
    }
}
