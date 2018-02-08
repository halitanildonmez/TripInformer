package Model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Created by anildonmez on 7.2.2018.
 */

public class SLData {
    private String displayData;
    private String destination;
    private LocalDateTime timeTabledDateTime;

    public SLData(String displayData, String destination, LocalDateTime timeTabledDateTime) {
        this.displayData = displayData;
        this.destination = destination;
        this.timeTabledDateTime = timeTabledDateTime;
    }

    public String getDisplayData() {
        return displayData;
    }

    public void setDisplayData(String displayData) {
        this.displayData = displayData;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getTimeTabledDateTime() {
        return timeTabledDateTime;
    }

    public void setTimeTabledDateTime(LocalDateTime timeTabledDateTime) {
        this.timeTabledDateTime = timeTabledDateTime;
    }
}
