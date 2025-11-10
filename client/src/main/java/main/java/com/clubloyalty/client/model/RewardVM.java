package main.java.com.clubloyalty.client.model;

public class RewardVM {
    public long id;
    public String title;
    public String description;
    public int cost;
    public boolean active;

    public String toString() {
        return title + " (" + cost + ")";
    }
}