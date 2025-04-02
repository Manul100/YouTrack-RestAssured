package utils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.util.List;

public class CsvDataProvider {

    public static Object[][] readCsvData(String filePath) {

        try {
            FileReader filereader = new FileReader(filePath);
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();

            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .withCSVParser(parser)
                    .build();

            List<String[]> data = csvReader.readAll();
            Object[][] result = new Object[data.size()][];
            for (int i = 0; i < data.size(); i++) {
                result[i] = data.get(i);
            }

            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
}
