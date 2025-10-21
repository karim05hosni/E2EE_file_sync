package com.karimhosny.file;

import java.nio.file.Path;
import java.util.Map;

public class EventsSuppressor {

    Map<Path, Long> suppressedEvents;

    public EventsSuppressor(Map<Path, Long> suppressedEvents) {
        this.suppressedEvents = suppressedEvents;
    }


    public void suppress(Path path) {
        suppressedEvents.put(path, System.currentTimeMillis());
    }

    public boolean isSuppressed(Path path) {

        Long ts = suppressedEvents.get(path);
        if (ts == null) {
            return false;
        }
        if (System.currentTimeMillis() - ts > 2000) { // 2s window
            suppressedEvents.remove(path);
            return false;
        }
        return true;
    }

}
