import models.Limit;
import models.Overlap;
import models.Request;
import models.Student;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVReader {

    private static String studentsFile = "src/main/resources/instances/simple/task_example/student.csv";
    private static String overlapsFile = "src/main/resources/instances/simple/task_example/overlaps.csv";
    private static String requestsFile = "src/main/resources/instances/simple/task_example/requests.csv";
    private static String limitsFile = "src/main/resources/instances/simple/task_example/limits.csv";

    public static void main(String[] args) {

        List<Student> students = parseCsvFileWithHeader(studentsFile).stream()
                .map(Student::new)
                .collect(Collectors.toList());
        List<Overlap> overlaps = parseCsvFileWithHeader(overlapsFile).stream()
                .map(Overlap::new)
                .collect(Collectors.toList());
        List<Request> requests = parseCsvFileWithHeader(requestsFile).stream()
                .map(Request::new)
                .collect(Collectors.toList());
        List<Limit> limits = parseCsvFileWithHeader(limitsFile).stream()
                .map(Limit::new)
                .collect(Collectors.toList());

        Map<Long, Student> studentsMap = new HashMap<>();


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
