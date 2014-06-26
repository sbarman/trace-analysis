package csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class CsvImport {

	public final Set<CsvTrace> traces;

	public CsvImport(String fileName) {
		File file = new File(fileName);
		traces = new HashSet<CsvTrace>();

		try {
			Scanner scanner = new Scanner(file);
			int lineNum = 0;

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				List<String> values = Arrays.asList(line.split(","));
				traces.add(new CsvTrace("" + lineNum, values));
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
