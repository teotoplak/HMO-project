import models.csv.LimitCsv;
import models.csv.OverlapCsv;
import models.csv.RequestCsv;
import models.csv.StudentCsv;

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

        List<StudentCsv> studentCsvs = parseCsvFileWithHeader(studentsFile).stream()
                .map(StudentCsv::new)
                .collect(Collectors.toList());
        List<OverlapCsv> overlapCsvs = parseCsvFileWithHeader(overlapsFile).stream()
                .map(OverlapCsv::new)
                .collect(Collectors.toList());
        List<RequestCsv> requestCsvs = parseCsvFileWithHeader(requestsFile).stream()
                .map(RequestCsv::new)
                .collect(Collectors.toList());
        List<LimitCsv> limitCsvs = parseCsvFileWithHeader(limitsFile).stream()
                .map(LimitCsv::new)
                .collect(Collectors.toList());

        //key is student_id
        Map<Long, List<StudentCsv>> studentsMap = new HashMap<>();


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
