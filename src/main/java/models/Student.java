package models;

public class Student {

    private Long student_id;
    private Long activity_id;
    private Long swap_weight;
    private Long group_id;
    private Long new_group_id;

    public Student(String[] csvLineParsed) {
        this.student_id = Long.parseLong(csvLineParsed[0]);
        this.activity_id = Long.parseLong(csvLineParsed[1]);
        this.swap_weight = Long.parseLong(csvLineParsed[2]);
        this.group_id = Long.parseLong(csvLineParsed[3]);
        this.new_group_id = Long.parseLong(csvLineParsed[4]);
    }


    public Long getStudent_id() {
        return student_id;
    }

    public Long getActivity_id() {
        return activity_id;
    }

    public Long getSwap_weight() {
        return swap_weight;
    }

    public Long getGroup_id() {
        return group_id;
    }

    public Long getNew_group_id() {
        return new_group_id;
    }
}
