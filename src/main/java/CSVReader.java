import models.csv.LimitCsv;
import models.csv.OverlapCsv;
import models.csv.RequestCsv;
import models.csv.StudentCsv;
import store.StudentStore;

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



        //TODO remove requests that contains non-existing student id



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
