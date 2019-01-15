package fer.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Group {

    private Long id;
    private Long studentCount;
    private Long min;
    private Long max;
    private Long minPreferred;
    private Long maxPreferred;
    private List<Long> overlapGroupIds;

    public Group(Group group) {
        this.id = group.id;
        this.studentCount = group.studentCount;
        this.min = group.min;
        this.max = group.max;
        this.minPreferred = group.minPreferred;
        this.maxPreferred = group.maxPreferred;
        this.overlapGroupIds = group.overlapGroupIds;
    }

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

    public boolean noPrefferedOverflowIfStudentAdded() {
        return studentCount + 1 <= maxPreferred;
    }

    public boolean isInPrefferedNumberWithCount(long studentCount) {
        return minPreferred <= studentCount && maxPreferred >= studentCount;
    }

}
