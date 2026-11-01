package models;

public class StateData {

    private String region;
    private String description;

    public StateData(String region, String description) {
        this.region = region;
        this.description = description;
    }

    public String getRegion() {
        return region;
    }

    public String getDescription() {
        return description;
    }
}