package fer;

import fer.models.*;
import fer.models.csv.LimitCsv;
import fer.models.csv.OverlapCsv;
import fer.models.csv.RequestCsv;
import fer.models.csv.StudentCsv;
import fer.store.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CSVReader {

   private static List<StudentCsv> studentsCsv = new ArrayList<>();
   private static String studentsFile = "";

    public static Solution read(String[] args) {

        String requestsFile = "";
        String overlapsFile = "";
        String limitsFile = "";

        if(args.length != 16) {
            System.err.println("Number of input arguments must be 16.\n Format is: –timeout <time_in_seconds> " +
                    "–award-activity \"[<number>,...]\" –award-student <points_for_whole_student> –minmax-penalty <minmax_panalty>" +
                    " –students-file <student_file_in_csv> –requests-file <request_file_in_csv> –overlaps-file <overlaps_file_in_csv> " +
                    "–limits-file <limit_file_in_csv>");
            System.exit(0);
        }

        try {
            for (int i = 0; i < args.length - 1; i = i + 2) {
                // maknula sam znak minusa jer oni imaju neki character koji nije jednak nasem minusu
                switch (args[i].substring(1)) {
                    case "timeout":
                        ProblemParameters.timeout = Long.parseLong(args[i + 1]);
                        break;
                    case "award-activity":
                        String[] awards = args[i + 1].split(",");
                        for (String award : awards) {
                            ProblemParameters.awardActivities.add(Long.parseLong(award));
                        }
                        break;
                    case "award-student":
                        ProblemParameters.awardStudent = Long.parseLong(args[i + 1]);
                        break;
                    case "minmax-penalty":
                        ProblemParameters.minMaxPenalty = Long.parseLong(args[i + 1]);
                        break;
                    case "students-file":
                        studentsFile = args[i + 1];
                        break;
                    case "requests-file":
                        requestsFile = args[i + 1];
                        break;
                    case "overlaps-file":
                        overlapsFile = args[i + 1];
                        break;
                    case "limits-file":
                        limitsFile = args[i + 1];
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Input arguments are not correct. \n" +
                    "Format is: –timeout <time_in_seconds> –award-activity \"[<number>,...]\" –award-student <points_for_whole_student> " +
                    "–minmax-penalty <minmax_panalty> –students-file <student_file_in_csv> –requests-file <request_file_in_csv> " +
                    "–overlaps-file <overlaps_file_in_csv> –limits-file <limit_file_in_csv>" +
                    "\nException: " + e.getMessage());
            System.exit(0);
        }

        studentsCsv = parseCsvFileWithHeader(studentsFile).stream()
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
        Map<Long, Group> groupStore = fillGroupStore(limitsCsv, overlapsCsv);
        Map<String, StudentActivity> studentActivityStore = filStudentActivityStore(studentsCsv, requestsCsv);
        return new Solution(groupStore, studentActivityStore);
    }

    private static Map<String, StudentActivity> filStudentActivityStore(List<StudentCsv> studentsCsv, List<RequestCsv> requestsCsv) {
        Map<String, StudentActivity> studentActivityMap = new HashMap<>();
        for (StudentCsv studentCsv : studentsCsv) {
            if (!studentActivityMap.containsKey(studentCsv.getStudent_id() + ":" + studentCsv.getActivity_id())) {
                List<Long> possibleGroups = findPossibleGroups(requestsCsv, studentCsv);
                studentActivityMap.put(studentCsv.getStudent_id() + ":" + studentCsv.getActivity_id(),
                        new StudentActivity(studentCsv.getStudent_id(),
                                studentCsv.getActivity_id(),
                                studentCsv.getGroup_id(),
                                studentCsv.getGroup_id(),
                                possibleGroups,
                                studentCsv.getSwap_weight()));
            }
        }
        return studentActivityMap;
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

    private static Map<Long, Group> fillGroupStore(List<LimitCsv> limitsCsv, List<OverlapCsv> overlapsCsv) {
        Map<Long, Group> groupMap = new HashMap<>();
        for (LimitCsv limitCsv : limitsCsv) {
            groupMap.put(limitCsv.getGroup_id(),
                    new Group(limitCsv.getGroup_id(),
                            limitCsv.getStudents_cnt(),
                            limitCsv.getMin(),
                            limitCsv.getMax(),
                            limitCsv.getMin_preferred(),
                            limitCsv.getMax_preferred(),
                            findOverlaps(limitCsv.getGroup_id(), overlapsCsv)));
        }
        return groupMap;
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
            br.readLine();
            while ((line = br.readLine()) != null) {
                parsedLines.add(line.split(cvsSplitBy));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return parsedLines;
    }

    public static void write(Map<String, StudentActivity> studentActivityMap) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(studentsFile.split(".csv")[0] + "_modified.csv");
            fileOutputStream.write("student_id,activity_id,swap_weight,group_id,new_group_id\n".getBytes());
            studentsCsv.forEach(studentCsv -> {
                try {
                    fileOutputStream.write((studentCsv.getStudent_id() + "," +
                            studentCsv.getActivity_id()+ "," +
                            studentCsv.getSwap_weight()+ "," +
                            studentCsv.getGroup_id() + "," +
                            studentActivityMap.get(studentCsv.getStudent_id() + ":" + studentCsv.getActivity_id()).getSelectedGroupId() + "\n").getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
