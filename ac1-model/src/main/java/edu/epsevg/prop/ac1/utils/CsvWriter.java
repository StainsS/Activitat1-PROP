package edu.epsevg.prop.ac1.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriter {
    /** Caràcter separador de camps usat per defecte en generar el CSV. */
    public static final char CSV_SEPARATOR = ';';

    private final String filename;
    private final String separator;

    public CsvWriter(String filename) { this(filename, CSV_SEPARATOR); }
    public CsvWriter(String filename, char separator) { this.filename = filename; this.separator = String.valueOf(separator); }

    public void write(List<String[]> rows) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            for (String[] r : rows) {
                fw.write(String.join(separator, r));
                fw.write("\n");
            }
        }
    }
}
