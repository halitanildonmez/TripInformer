package Model;

import java.sql.Timestamp;

/**
 * Created by anildonmez on 7.2.2018.
 */

public class SLData {
    private String displayData;
    private String destination;
    private Timestamp timeTabledDateTime;

    public SLData(String displayData, String destination, Timestamp timeTabledDateTime) {
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

    public Timestamp getTimeTabledDateTime() {
        return timeTabledDateTime;
    }

    public void setTimeTabledDateTime(Timestamp timeTabledDateTime) {
        this.timeTabledDateTime = timeTabledDateTime;
    }
}
