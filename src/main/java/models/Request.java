package models;

public class Request {

    private Long student_id;
    private Long activity_id;
    private Long req_group_id;

    public Request(String[] csvLineParsed) {
        this.student_id = Long.parseLong(csvLineParsed[0]);
        this.activity_id = Long.parseLong(csvLineParsed[1]);
        this.req_group_id = Long.parseLong(csvLineParsed[2]);
    }
}
