package fer.models.csv;

import lombok.Data;

@Data
public class LimitCsv {

    private Long group_id;
    private Long students_cnt;
    private Long min;
    private Long min_preferred;
    private Long max;
    private Long max_preferred;

    public LimitCsv(String[] csvLineParsed) {
        this.group_id = Long.parseLong(csvLineParsed[0]);
        this.students_cnt = Long.parseLong(csvLineParsed[1]);
        this.min = Long.parseLong(csvLineParsed[2]);
        this.min_preferred = Long.parseLong(csvLineParsed[3]);
        this.max = Long.parseLong(csvLineParsed[4]);
        this.max_preferred = Long.parseLong(csvLineParsed[5]);
    }
}
