package Model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by anildonmez on 7.2.2018.
 */

public class SLData {
    private String displayData;
    private String destination;
    private Date timeTabledDateTime;

    public SLData(String displayData, String destination, Date timeTabledDateTime) {
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

    public Date getTimeTabledDateTime() {
        return timeTabledDateTime;
    }

    public void setTimeTabledDateTime(Date timeTabledDateTime) {
        this.timeTabledDateTime = timeTabledDateTime;
    }
}
