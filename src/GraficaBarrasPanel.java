
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GraficaBarrasPanel extends JPanel {
    private final Map<String, Double> datos;

    public GraficaBarrasPanel(Map<String, Double> datos) {
        this.datos = datos;
        setPreferredSize(new Dimension(600, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int margen = 50;
        int altura = getHeight() - 2 * margen;
        int ancho = getWidth() - 2 * margen;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

  
        g.setColor(Color.BLACK);
        g.drawLine(margen, getHeight() - margen, getWidth() - margen, getHeight() - margen);
        g.drawLine(margen, margen, margen, getHeight() - margen);

        if (datos.isEmpty()) return;

        int numCiudades = datos.size();
        int anchoBarra = ancho / numCiudades - 10;
        int x = margen + 10;
        double tempMax = datos.values().stream().max(Double::compare).orElse(1.0);

        for (Map.Entry<String, Double> entrada : datos.entrySet()) {
            String ciudad = entrada.getKey();
            double promedio = entrada.getValue();

            int alturaBarra = (int) ((promedio / tempMax) * altura);
            int y = getHeight() - margen - alturaBarra;

        
            g.setColor(Color.BLUE);
            g.fillRect(x, y, anchoBarra, alturaBarra);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, anchoBarra, alturaBarra);

      
            g.drawString(ciudad, x, getHeight() - margen + 15);
            g.drawString(String.format("%.1f", promedio), x, y - 5);

            x += anchoBarra + 10;
        }
    }
}
