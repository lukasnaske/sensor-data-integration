package at.ac.tuwien.processing.dto;

import java.math.BigDecimal;

public class Data {

    private Long timestamp;
    private BigDecimal value;

    public Data() {}

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
