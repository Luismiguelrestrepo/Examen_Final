// GestorCSV.java
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GestorCSV {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static List<RegistroTemperatura> cargarDatos(String ruta) throws IOException {
        List<RegistroTemperatura> lista = new ArrayList<>();
        List<String> lineas = Files.readAllLines(Paths.get(ruta));
        for (int i = 1; i < lineas.size(); i++) {
            String[] partes = lineas.get(i).split(",");
            String ciudad = partes[0];
            LocalDate fecha = LocalDate.parse(partes[1], FORMATO);
            double temperatura = Double.parseDouble(partes[2]);
            lista.add(new RegistroTemperatura(ciudad, fecha, temperatura));
        }
        return lista;
    }

    public static void guardarDatos(String ruta, List<RegistroTemperatura> datos) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(ruta));
        writer.write("Ciudad,Fecha,Temperatura\n");
        for (RegistroTemperatura r : datos) {
            writer.write(r.getCiudad() + "," + r.getFecha().format(FORMATO) + "," + r.getTemperatura() + "\n");
        }
        writer.close();
    }
}
