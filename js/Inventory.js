export default class Inventory {
    currentRebuild;
    lastRebuildIndex = -10;
    events;
    index;
    unexpectedCounter = 0;
    failedToRemove = 0;

    constructor(events) {
        this.events = events;
        this.index = 0;
        this.currentRebuild = [];
    }

    getEvents() {
        return this.events;
    }

    setIndex(index) {
        this.index = index;
    }

    getIndex() {
        return this.index;
    }

    getEventFromCurrentIndex() {
        return this.events[this.index];
    }

    getCurrentInventory() {
        if (this.currentRebuild && this.lastRebuildIndex === this.index) {
            return this.currentRebuild;
        } else {
            this.buildCurrentInventory();
            return this.currentRebuild;
        }
    }

    buildCurrentInventory() {
        if (this.lastRebuildIndex === this.index - 1) {
            this.calculateEvents(this.lastRebuildIndex, this.index);
        } else {
            this.calculateEvents(0, this.index);
        }
    }

    calculateEvents(start, end) {
        if (start === 0) {
            this.currentRebuild = [];
            this.unexpectedCounter = 0;
            this.failedToRemove = 0;
        }
        for (let i = start; i < end; i++) {
            let currentEvent = this.events[i];
            let preBuild = [...this.currentRebuild];
            let currentSize = this.currentRebuild.length;
            let expectedSize = currentSize - Number(currentEvent.itemsRemoved ? currentEvent.itemsRemoved.length : 0) + Number(currentEvent.itemsAdded ? currentEvent.itemsAdded.length : 0);
            if (currentEvent.itemsAdded) {

                this.currentRebuild = this.appendArray(this.currentRebuild, [...currentEvent.itemsAdded]);
            }
            if (currentEvent.itemsRemoved) {
                currentEvent.itemsRemoved.forEach(item => this.removeItemFromCurrentRebuild(item, currentEvent))
            }
            if (expectedSize !== this.currentRebuild.length) {
                console.log("Size pre event: " + currentSize);
                console.log("Expected: " + expectedSize);
                console.log("Actual: " + this.currentRebuild.length);
                console.log("Expected length diff");
                this.unexpectedCounter++;
            }
            if (this.currentRebuild.length === 0) {
                console.log(i);
                console.log(preBuild);
                console.log("Size pre event: " + currentSize);
                console.log(currentEvent)
            }
        }
        this.lastRebuildIndex = this.index;
        console.log(this.unexpectedCounter);
    }

    appendArray(a, b) {
        while (b.length) a.push(b.shift());
        return a;
    }

    removeItemFromCurrentRebuild(item, currentEvent) {
        let itemIndex = -1;
        let ignoreKey = false;

        let itemToRemove = this.currentRebuild.find(cur => {
            return cur.descriptionKey === item.descriptionKey
        });
        if (itemToRemove === undefined) {
            itemToRemove = this.currentRebuild.find(cur => {
                return cur.description.instanceid === item.description.instanceid;
            })
            ignoreKey = true;
        }

        if (itemToRemove === undefined && item.description.market_hash_name.includes("™")) {

            itemToRemove = this.currentRebuild.find(cur => {
                if (cur.description.market_hash_name === item.description.market_hash_name) {
                    return cur.description.icon_url === item.description.icon_url && JSON.stringify(cur.description.tags) === JSON.stringify(item.description.tags);
                } else {
                    return false;
                }
            })
            ignoreKey = true;
        }

        if (itemToRemove !== undefined) {
            itemIndex = this.currentRebuild.indexOf(itemToRemove);
        }

        if (itemIndex > -1) {
            let removedElement = this.currentRebuild.splice(itemIndex, 1);
            if (!ignoreKey && removedElement[0].descriptionKey !== item.descriptionKey) {
                console.error("DESCRIPTIONS NOT MATCHING!!")
            }
        } else {
            this.failedToRemove++;
            console.log(item, "failed to remove");
            console.log(this.currentRebuild);
            console.log(currentEvent)
        }
    }

}