import models.Activity;
import models.Group;
import models.Student;
import models.StudentActivity;
import models.csv.LimitCsv;
import models.csv.OverlapCsv;
import models.csv.RequestCsv;
import models.csv.StudentCsv;
import store.ActivityStore;
import store.GroupStore;
import store.StudentActivityStore;
import store.StudentStore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CSVReader {

    private static String studentsFile = "src/main/resources/instances/simple/task_example/student.csv";
    private static String overlapsFile = "src/main/resources/instances/simple/task_example/overlaps.csv";
    private static String requestsFile = "src/main/resources/instances/simple/task_example/requests.csv";
    private static String limitsFile = "src/main/resources/instances/simple/task_example/limits.csv";

    public static void main(String[] args) {

        // TODO read args - error handling

        List<StudentCsv> studentsCsv = parseCsvFileWithHeader(studentsFile).stream()
                .map(StudentCsv::new)
                .collect(Collectors.toList());
        List<OverlapCsv> overlapsCsv = parseCsvFileWithHeader(overlapsFile).stream()
                .map(OverlapCsv::new)
                .collect(Collectors.toList());
        List<RequestCsv> requestsCsv = parseCsvFileWithHeader(requestsFile).stream()
                .map(RequestCsv::new)
                .collect(Collectors.toList());
        List<LimitCsv> limitsCsv = parseCsvFileWithHeader(limitsFile).stream()
                .map(LimitCsv::new)
                .collect(Collectors.toList());

        fillStudentStore(studentsCsv);
        fillActivityStore(studentsCsv);
        fillGroupStore(limitsCsv, overlapsCsv);
        filStudentActivityStore(studentsCsv, requestsCsv);

    }

    private static void filStudentActivityStore(List<StudentCsv> studentsCsv, List<RequestCsv> requestsCsv) {
        for (StudentCsv studentCsv : studentsCsv) {
            if(!StudentActivityStore.studentActivityMap.containsKey(studentCsv.getStudent_id() + ":" + studentCsv.getActivity_id())) {
                List<Long> possibleGroups = findPossibleGroups(requestsCsv, studentCsv);
                if(possibleGroups.size() > 1) {
                    StudentActivityStore.studentActivityMap.put(studentCsv.getStudent_id() + ":" + studentCsv.getActivity_id(),
                            new StudentActivity(studentCsv.getStudent_id(),
                                    studentCsv.getActivity_id(),
                                    studentCsv.getGroup_id(),
                                    studentCsv.getGroup_id(),
                                    possibleGroups,
                                    studentCsv.getSwap_weight()));
                }
            }
        }
    }

    private static List<Long> findPossibleGroups(List<RequestCsv> requestsCsv, StudentCsv studentCsv) {
        List<Long> list = requestsCsv.stream()
                .filter(requestCsv ->
                        requestCsv.getStudent_id().equals(studentCsv.getStudent_id()) &&
                        requestCsv.getActivity_id().equals(studentCsv.getActivity_id()))
                .map(RequestCsv::getReq_group_id)
                .collect(Collectors.toList());
        list.add(studentCsv.getGroup_id());
        return list;
    }

    private static void fillGroupStore(List<LimitCsv> limitsCsv, List<OverlapCsv> overlapsCsv) {
        for (LimitCsv limitCsv : limitsCsv) {
            GroupStore.groupMap.put(limitCsv.getGroup_id(),
                    new Group(limitCsv.getGroup_id(),
                            limitCsv.getStudents_cnt(),
                            limitCsv.getMin(),
                            limitCsv.getMax(),
                            limitCsv.getMin_preferred(),
                            limitCsv.getMax_preferred(),
                            findOverlaps(limitCsv.getGroup_id(), overlapsCsv)));
        }
    }


    private static void fillStudentStore(List<StudentCsv> studentsCsv) {
        for (StudentCsv studentCsv : studentsCsv) {
            findStudent(studentCsv.getStudent_id())
                    .getActivityIds()
                    .add(studentCsv.getActivity_id());
        }
    }

    private static void fillActivityStore(List<StudentCsv> studentsCsv) {
        for (StudentCsv studentCsv : studentsCsv) {
            findActivity(studentCsv.getActivity_id())
                    .getStudentIds()
                    .add(studentCsv.getStudent_id());
        }
    }

    private static List<Long> findOverlaps(Long groupId, List<OverlapCsv> overlapsCsv) {
        return overlapsCsv.stream()
                .filter(overlapCsv -> overlapCsv.getGroup1_id().equals(groupId))
                .map(OverlapCsv::getGroup2_id)
                .collect(Collectors.toList());
    }

    private static Student findStudent(Long studentId) {
        Student student = StudentStore.studentMap.get(studentId);
        if (student == null) {
            student = new Student(studentId, new ArrayList<>());
            StudentStore.studentMap.put(studentId, student);
        }
        return student;
    }

    private static Activity findActivity(Long activityId) {
        for (Activity activity : ActivityStore.activities) {
            if (activity.getId().equals(activityId)) return activity;
        }
        Activity activity = new Activity(activityId, new ArrayList<>());
        ActivityStore.activities.add(activity);
        return activity;
    }

    private static List<String[]> parseCsvFileWithHeader(String pathToFile) {
        List<String[]> parsedLines = new LinkedList<>();
        String line = "";
        String cvsSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
            // skipping header
            br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                parsedLines.add(line.split(cvsSplitBy));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsedLines;
    }


}
