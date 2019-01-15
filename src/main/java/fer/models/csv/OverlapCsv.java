package fer.models.csv;

import lombok.Data;

@Data
public class OverlapCsv {

    private Long group1_id;
    private Long group2_id;

    public OverlapCsv(String[] csvLineParsed) {
        this.group1_id = Long.parseLong(csvLineParsed[0]);
        this.group2_id = Long.parseLong(csvLineParsed[1]);
    }
}
