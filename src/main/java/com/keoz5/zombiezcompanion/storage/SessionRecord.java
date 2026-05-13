package com.keoz5.zombiezcompanion.storage;

import com.keoz5.zombiezcompanion.modules.tracker.SessionStats;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Immutable snapshot of a finished session, serialized to the history file.
 */
public final class SessionRecord {

    public long   startTimeMs;
    public long   endTimeMs;
    public long   durationMs;
    public int    kills;
    public int    maxStreak;
    public int    eventsCompleted;
    public String mainZone;
    public String date;
    public List<String> eventHistory;

    /** Factory from a finished SessionStats. */
    public static SessionRecord from(SessionStats stats) {
        SessionRecord r  = new SessionRecord();
        r.startTimeMs    = stats.startTimeMs;
        r.endTimeMs      = stats.endTimeMs > 0 ? stats.endTimeMs : System.currentTimeMillis();
        r.durationMs     = r.endTimeMs - r.startTimeMs;
        r.kills          = stats.kills;
        r.maxStreak      = stats.maxStreak;
        r.eventsCompleted = stats.eventsCompleted;
        r.mainZone       = stats.mainZone;
        r.date           = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(r.startTimeMs));
        r.eventHistory   = new ArrayList<>(stats.eventHistory);
        return r;
    }
}
