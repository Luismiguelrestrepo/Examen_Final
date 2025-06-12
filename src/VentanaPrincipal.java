

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private JTable tabla;
    private DefaultTableModel modelo;
    private List<RegistroTemperatura> registros;
    private final String[] columnas = { "Ciudad", "Fecha", "Temperatura" };
    private final String rutaArchivo = "Temperaturas.csv"; 
    public VentanaPrincipal() {
        setTitle("Registro de Temperaturas");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        modelo = new DefaultTableModel(columnas, 0);
        tabla = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tabla);
        add(scrollPane, BorderLayout.CENTER);


        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnGuardar = new JButton("Guardar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);
        JButton btnGrafica = new JButton("Gráfica Promedio");
        panelBotones.add(btnGrafica);

        btnGrafica.addActionListener(e -> mostrarGraficaPromedio());

        JButton btnAnalisisDia = new JButton("Análisis por Fecha");
        panelBotones.add(btnAnalisisDia);

        btnAnalisisDia.addActionListener(e -> mostrarCiudadExtremosEnFecha());


        try {
            registros = GestorCSV.cargarDatos(rutaArchivo);
            actualizarTabla();
        } catch (IOException e) {
            registros = new ArrayList<>();
            JOptionPane.showMessageDialog(this, "Error al cargar el archivo CSV");
        }


        btnAgregar.addActionListener(e -> {
            RegistroTemperatura nuevo = mostrarDialogoRegistro(null);
            if (nuevo != null) {
                registros.add(nuevo);
                actualizarTabla();
            }
        });


        btnModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila != -1) {
                RegistroTemperatura seleccionado = registros.get(fila);
                RegistroTemperatura modificado = mostrarDialogoRegistro(seleccionado);
                if (modificado != null) {
                    registros.set(fila, modificado);
                    actualizarTabla();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar");
            }
        });


        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila != -1) {
                registros.remove(fila);
                actualizarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar");
            }
        });

        btnGuardar.addActionListener(e -> {
            try {
                GestorCSV.guardarDatos(rutaArchivo, registros);
                JOptionPane.showMessageDialog(this, "Datos guardados correctamente.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo.");
            }
        });

        setVisible(true);
    }

    private void actualizarTabla() {
        modelo.setRowCount(0);
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (RegistroTemperatura r : registros) {
            modelo.addRow(new Object[] {
                    r.getCiudad(),
                    r.getFecha().format(formato),
                    r.getTemperatura()
            });
        }
    }

    private RegistroTemperatura mostrarDialogoRegistro(RegistroTemperatura actual) {
        JTextField campoCiudad = new JTextField();
        JTextField campoFecha = new JTextField("dd/MM/yyyy");
        JTextField campoTemp = new JTextField();

        if (actual != null) {
            campoCiudad.setText(actual.getCiudad());
            campoFecha.setText(actual.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            campoTemp.setText(String.valueOf(actual.getTemperatura()));
        }

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Ciudad:"));
        panel.add(campoCiudad);
        panel.add(new JLabel("Fecha:"));
        panel.add(campoFecha);
        panel.add(new JLabel("Temperatura:"));
        panel.add(campoTemp);

        int result = JOptionPane.showConfirmDialog(null, panel,
                actual == null ? "Agregar Registro" : "Modificar Registro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String ciudad = campoCiudad.getText();
                LocalDate fecha = LocalDate.parse(campoFecha.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                double temperatura = Double.parseDouble(campoTemp.getText());
                return new RegistroTemperatura(ciudad, fecha, temperatura);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Datos inválidos. Verifica el formato.");
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(VentanaPrincipal::new);
    }

    private void mostrarGraficaPromedio() {
        String fechaInicioStr = JOptionPane.showInputDialog(this, "Ingrese fecha inicio (dd/MM/yyyy):");
        String fechaFinStr = JOptionPane.showInputDialog(this, "Ingrese fecha fin (dd/MM/yyyy):");

        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInicio = LocalDate.parse(fechaInicioStr, formato);
            LocalDate fechaFin = LocalDate.parse(fechaFinStr, formato);

            Map<String, List<RegistroTemperatura>> agrupados = new HashMap<>();

            for (RegistroTemperatura r : registros) {
                if (!r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin)) {
                    agrupados.computeIfAbsent(r.getCiudad(), k -> new ArrayList<>()).add(r);
                }
            }

            Map<String, Double> promedios = new HashMap<>();
            for (Map.Entry<String, List<RegistroTemperatura>> entrada : agrupados.entrySet()) {
                double promedio = entrada.getValue().stream()
                        .mapToDouble(RegistroTemperatura::getTemperatura)
                        .average().orElse(0.0);
                promedios.put(entrada.getKey(), promedio);
            }

            if (promedios.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay datos para ese rango.");
                return;
            }

            JFrame ventanaGrafica = new JFrame("Promedio de Temperaturas");
            ventanaGrafica.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventanaGrafica.add(new GraficaBarrasPanel(promedios));
            ventanaGrafica.pack();
            ventanaGrafica.setLocationRelativeTo(this);
            ventanaGrafica.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fechas inválidas.");
        }
    }

    private void mostrarCiudadExtremosEnFecha() {
        String fechaStr = JOptionPane.showInputDialog(this, "Ingrese la fecha a consultar (dd/MM/yyyy):");

        try {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaConsulta = LocalDate.parse(fechaStr, formato);

            List<RegistroTemperatura> delDia = new ArrayList<>();
            for (RegistroTemperatura r : registros) {
                if (r.getFecha().equals(fechaConsulta)) {
                    delDia.add(r);
                }
            }

            if (delDia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay registros para esa fecha.");
                return;
            }

            RegistroTemperatura max = Collections.max(delDia,
                    Comparator.comparingDouble(RegistroTemperatura::getTemperatura));
            RegistroTemperatura min = Collections.min(delDia,
                    Comparator.comparingDouble(RegistroTemperatura::getTemperatura));

            String mensaje = String.format(
                    "📅 Fecha: %s\n\n🌡️ Ciudad más calurosa: %s (%.1f °C)\n❄️ Ciudad menos calurosa: %s (%.1f °C)",
                    fechaStr, max.getCiudad(), max.getTemperatura(), min.getCiudad(), min.getTemperatura());

            JOptionPane.showMessageDialog(this, mensaje);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fecha inválida.");
        }
    }

}
