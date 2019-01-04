package models;

public class Overlap {

    private Long group1_id;
    private Long group2_id;

    public Overlap(String[] csvLineParsed) {
        this.group1_id = Long.parseLong(csvLineParsed[0]);
        this.group2_id = Long.parseLong(csvLineParsed[1]);
    }
}
