package models.csv;

import lombok.Data;

import java.util.List;

@Data
public class StudentCsv {

    private Long student_id;
    private Long swap_weight;
    private Long activity_id;
    private Long group_id;
    private Long new_group_id;

    public StudentCsv(String[] csvLineParsed) {
        this.student_id = Long.parseLong(csvLineParsed[0]);
        this.activity_id = Long.parseLong(csvLineParsed[1]);
        this.swap_weight = Long.parseLong(csvLineParsed[2]);
        this.group_id = Long.parseLong(csvLineParsed[3]);
        this.new_group_id = Long.parseLong(csvLineParsed[4]);
    }
}
