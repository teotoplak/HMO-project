package models;

import lombok.Data;

import java.util.List;

@Data
public class Group {

    private Long id;
    private Long studentCount;
    private Long min;
    private Long max;
    private Long minPreferred;
    private Long maxPreferred;
    private List<Long> overlapGroupIds;

    public boolean isFull() {
        return studentCount >= max;
    }

    public boolean isAtMinNumOfStudents() {
        return studentCount <= min;
    }

    public void increaseStudentCount() {
        this.studentCount++;
    }

    public void decreaseStudentCount() {
        this.studentCount--;
    }
}
