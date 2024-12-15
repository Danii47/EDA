import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Interfaz Gráfico para la práctica de EDA 2024-25 (Versión 2)
 * 
 * @author César Vaca
 */
public class GUIPtos extends JPanel implements ActionListener, ChangeListener, MouseListener {

    static int MARGEN = 20;      // Reborde (pixels)
    static int RADG = 6;         // Radio gotículas
    static int TAMP = 100;       // Tamaño paleta
    static float MAXV = 20;      // Rango velocidad (repr. colores)
    static float MAXD = 1;       // Rango densidad (repr. colores)
    static boolean DARK = false; // Dark Theme
    
    JFrame ventana;
    JLabel etqTpo;
    JButton btnStart;
    JComboBox<String> lisParam;
    JSlider sldParam;
    JLabel etqParam;
    Simulador sim;
    boolean iniciado = false;
    GUIWorker worker;
    int lxc, lyc; // Dimensiones del panel (pixels)
    float tdx, tdy, tesc; // Escalado
    BufferedImage img;
    BufferedImage den;
    BufferedImage[] bolas;
    int skip = 1;
    boolean verDensidad = false;
    
    public GUIPtos(Simulador sim) {
        super();        
        this.sim = sim;        
        CreaBolas(this::PalVel);
        setDoubleBuffered(false);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) { click(e.getX(), e.getY()); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) { Escalar(); }            
        });
        etqTpo = new JLabel("");
        etqTpo.setMinimumSize(new Dimension(200,26));
        etqTpo.setMaximumSize(new Dimension(200,26));
        etqTpo.setPreferredSize(new Dimension(200,26));
        btnStart = new JButton(" Iniciar ");
        lisParam = new JComboBox<>(new String[] {
            "Radio Gotículas",
            "Rango Velocidad",
            "Rango Densidad",            
            "Densidad objetivo",
            "Coeficiente Presion",
            "Coeficiente Viscosidad",
            "Gravedad (intensidad)",
            "Gravedad (ángulo)",
            "Paso de integración",
            "Coeficiente Interacción",
            "Skip Frames",
            "Mostrar Densidad"
        });
        lisParam.setMaximumRowCount(12);
        sldParam = new JSlider(JSlider.HORIZONTAL, 1, 20, RADG);
        etqParam = new JLabel(""+RADG);
        JPanel panSup = new JPanel(new BorderLayout());       
        panSup.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel panSupIzdo = new JPanel(new BorderLayout(5,0));
        panSupIzdo.add(btnStart, BorderLayout.WEST);
        panSupIzdo.add(etqTpo, BorderLayout.CENTER);
        panSupIzdo.add(lisParam, BorderLayout.EAST);
        panSup.add(panSupIzdo, BorderLayout.WEST);
        panSup.add(sldParam, BorderLayout.CENTER);
        panSup.add(etqParam, BorderLayout.EAST);
        JPanel panPpal = new JPanel(new BorderLayout());
        panPpal.add(panSup,BorderLayout.NORTH);
        panPpal.add(this,BorderLayout.CENTER);
        btnStart.addActionListener(this);
        lisParam.addActionListener(this);
        sldParam.addChangeListener(this);
        ventana = new JFrame("Visor de Gotículas - Modo Puntos - EDA 2024-25");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setContentPane(panPpal);
        ventana.setSize(600,500);
        ventana.setLocation(100,100);
        ventana.setAlwaysOnTop(true);
        ventana.setVisible(true);        
    }
    
    protected void Escalar() {
        lxc = getWidth();
        lyc = getHeight();
        tesc = Math.min((lxc - 2*MARGEN)/sim.lx, (lyc - 2*MARGEN)/sim.ly);
        tdx = (lxc - tesc*sim.lx)/2;
        tdy = lyc - (lyc - tesc*sim.ly)/2;
        img = new BufferedImage(lxc, lyc, BufferedImage.TYPE_INT_ARGB);
        den = new BufferedImage(lxc, lyc, BufferedImage.TYPE_INT_ARGB);
    }
    
    protected int Cx(float x) { return (int) (tdx + x*tesc); }
    protected int Cy(float y) { return (int) (tdy - y*tesc); }
    
    protected Color PalVel(float k) {
        return new Color(Math.min(k, 1), k < 0.5f ? Math.min(3*k, 1) : Math.min(3*(1-k), 1), Math.max(-2*k+1,0));
    }
   
    protected final void CreaBolas(Function<Float, Color> paleta) {
        bolas = new BufferedImage[TAMP];
        for(int i = 0; i < TAMP; i++) {
            float k = ((float) i)/TAMP;
            BufferedImage bola = new BufferedImage(RADG+2, RADG+2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bola.createGraphics();
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
            g.setColor(paleta.apply(k));
            g.fill(new Ellipse2D.Double(1,1,RADG,RADG));
            bolas[i] = bola;
        }
    }
    
    protected int PalDen(float k) {
        if(k < -1) { return 0xFF00FF00; }
        if(k > 1) { return 0xFFFF0000; }
        int R = (int) (127*(k+1));
        int G = (int) (127*(1-k));
        int B = (int) (128*(1-Math.abs(k)));
        return 0xFF000000 | (R << 16) | (G << 8) | B;
    }   

    protected final void DibujaDensidad() {
        for(int i = 0; i < lyc; i++) {
            float y = (tdy-i)/tesc;
            for(int j = 0; j < lxc; j++) {
                float x = (j-tdx)/tesc;
                float d = (sim.CalcDensidad(x, y)-sim.D0)/MAXD;
                den.setRGB(j, i, PalDen(d));
            }
        }
    }
    
    protected void Dibujar(Graphics2D g) {
        g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED));
        if(!verDensidad) { 
            g.setColor(DARK ? Color.black : Color.white);
            g.fillRect(0,0,lxc,lyc);
            // Dibujar retícula
            g.setColor(DARK ? Color.darkGray : Color.lightGray);
            for(int y = 1; y < sim.ly; y++) { g.drawLine(Cx(0), Cy(y), Cx(sim.lx), Cy(y)); }            
            for(int x = 1; x < sim.lx; x++) { g.drawLine(Cx(x), Cy(sim.ly), Cx(x), Cy(0)); }                   
        }
        for(Simulador.Goticula w: sim.gotas) {
            double v = Math.sqrt(w.vx*w.vx+w.vy*w.vy);
            int i = Math.min(TAMP-1, (int) (TAMP*v/MAXV));
            g.drawImage(bolas[i], (int) (tdx+w.x*tesc), (int) (tdy-w.y*tesc), null);
        }       
        g.setColor(DARK ? Color.gray : Color.black);
        g.drawRect(Cx(0), Cy(sim.ly), Cx(sim.lx)-Cx(0), Cy(0)-Cy(sim.ly));
        if(sim.xu >= 0) {
            g.setColor(Color.red);
            int r = (int) (tesc*sim.ru);
            g.drawOval(Cx(sim.xu)-r, Cy(sim.yu)-r, 2*r, 2*r);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if(img == null) {return;}
        long t1 = System.nanoTime();
        if(verDensidad) { 
            DibujaDensidad();
            g.drawImage(den, 0, 0, lxc, lyc, null);
        } else {
            Dibujar(img.createGraphics());
            g.drawImage(img, 0, 0, lxc, lyc, null);
        }        
        long t2 = System.nanoTime();
        etqTpo.setText(String.format(" Sim: %5.1f ms | Graf: %5.1f ms.", sim.tpo, 1e-6*(t2-t1)));
    }
    
    @Override
    public void mouseClicked(MouseEvent e) { }

    int btn;
    
    public void click(int xp, int yp) {
        float x = (xp - tdx)/tesc;
        float y = (tdy - yp)/tesc;
        sim.Click(x, y, btn == 1);
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        btn = e.getButton();
        click(e.getX(), e.getY());
    }
   
    @Override
    public void mouseReleased(MouseEvent e) {
        sim.NoClick();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnStart) {            
            if(iniciado) { 
                iniciado = false;
                worker.cancel(true);
                btnStart.setText(" Iniciar ");
            } else {
                iniciado = true;
                worker = new GUIWorker(sim, this, skip);
                worker.execute();
                btnStart.setText(" Parar ");
            }
            return;
        }
        if(e.getSource() == lisParam) {           
            nocambio = true;
            switch(lisParam.getSelectedIndex()) {
                case 0: // Radio
                    sldParam.setMinimum(1);
                    sldParam.setMaximum(20);
                    sldParam.setValue(RADG);
                    etqParam.setText(""+RADG);
                    break;
                case 1: // Rango Velocidad
                    sldParam.setMinimum(1);
                    sldParam.setMaximum(100);
                    sldParam.setValue((int) (MAXV));
                    etqParam.setText(String.format("%.1f", MAXV));
                    break;                   
                case 2: // Rango Densidad
                    sldParam.setMinimum(1);  // 1/20 = 0.05
                    sldParam.setMaximum(400); // 40/20 = 2
                    sldParam.setValue((int) (20*MAXD));
                    etqParam.setText(String.format("%.2f", MAXD));
                    break;                   
                case 3: // Densidad
                    sldParam.setMinimum(0);
                    sldParam.setMaximum(500);  // 50
                    sldParam.setValue((int) (10*sim.D0));
                    etqParam.setText(String.format("%.1f", sim.D0));
                    break;
                case 4: // Coeficiente Presion
                    sldParam.setMinimum(0);    // 1
                    sldParam.setMaximum(400);  // 10000
                    sldParam.setValue((int) (100*Math.log10(sim.KP)));
                    etqParam.setText(String.format("%.2f", sim.KP));
                    break;
                case 5: // Coeficiente Viscosidad
                    sldParam.setMinimum(0); 
                    sldParam.setMaximum(500);  // 500/20 = 25
                    sldParam.setValue((int) (20*sim.KV));
                    etqParam.setText(String.format("%.2f", sim.KV));
                    break;
                case 6: // Gravedad (intensidad)
                    double g = Math.sqrt(sim.GX*sim.GX+sim.GY*sim.GY);
                    sldParam.setMinimum(0);
                    sldParam.setMaximum(400);
                    sldParam.setValue((int) (20*g));
                    etqParam.setText(String.format("%.2f", g));
                    break;
                case 7: // Gravedad (ángulo)
                    double a = 180*Math.atan2(sim.GY, sim.GX)/Math.PI;
                    sldParam.setMinimum(-180);
                    sldParam.setMaximum(180);
                    sldParam.setValue((int) (a));
                    etqParam.setText(String.format("%.0f °", a));
                    break;
                case 8: // Paso de integración
                    sldParam.setMinimum(0);    // 1
                    sldParam.setMaximum(600);  // 0.000001
                    sldParam.setValue((int) (-100*Math.log10(sim.DT)));
                    etqParam.setText(String.format("%.7f", sim.DT));
                    break;
                case 9: // Coeficiente Interacción
                    sldParam.setMinimum(0);    // 1
                    sldParam.setMaximum(300);  // 10000
                    sldParam.setValue((int) (100*Math.log10(sim.KU)));
                    etqParam.setText(String.format("%.2f", sim.KU));
                    break;
                case 10: // Skip frames
                    sldParam.setMinimum(0);
                    sldParam.setMaximum(20);
                    sldParam.setValue(skip);
                    etqParam.setText(String.format("1:%d", skip));
                    break;
                case 11: // Visualización
                    sldParam.setMinimum(0);
                    sldParam.setMaximum(1);
                    sldParam.setValue(verDensidad ? 1 : 0);
                    etqParam.setText(verDensidad ? "True" : "False");
                    break;
            }
            nocambio = false;
        }
    }

    boolean nocambio = false;
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if(nocambio) { return; }
        double g, a;
        int x = sldParam.getValue();
        switch(lisParam.getSelectedIndex()) {
            case 0: // Radio
                RADG = x;
                etqParam.setText(""+RADG);
                CreaBolas(this::PalVel);
                repaint();
                break;
            case 1: // Rango Velocidad
                MAXV = x;
                etqParam.setText(String.format("%.1f", MAXV));
                repaint();                
                break;                   
            case 2: // Rango Densidad
                MAXD = x/20f;
                etqParam.setText(String.format("%.2f", MAXD));
                repaint();
                break;                   
            case 3: // Densidad
                sim.D0 = x/10f;
                etqParam.setText(String.format("%.1f", sim.D0));
                if(!iniciado) {
                    sim.ColocaGoticulasEnCuadrado();
                    Escalar();
                }
                repaint();                
                break;
            case 4: // Coeficiente Presion
                sim.KP = (float) Math.pow(10.0, x/100.0);
                etqParam.setText(String.format("%.2f", sim.KP));
                break;
            case 5: // Coeficiente Viscosidad
                sim.KV = x/20f;
                etqParam.setText(String.format("%.2f", sim.KV));
                break;
            case 6: // Gravedad (intensidad)
                a = Math.atan2(sim.GY, sim.GX);
                g = x/20f;
                sim.GX = (float) (g*Math.cos(a));
                sim.GY = (float) (g*Math.sin(a));
                etqParam.setText(String.format("%.1f", g));
                break;
            case 7: // Gravedad (ángulo)
                a = x*Math.PI/180;
                g = (float) Math.sqrt(sim.GX*sim.GX+sim.GY*sim.GY);                
                sim.GX = (float) (g*Math.cos(a));
                sim.GY = (float) (g*Math.sin(a));
                etqParam.setText(String.format("%d °", x));
                break;
            case 8: // Paso de integración
                sim.DT = (float) Math.pow(10.0, -x/100.0);
                etqParam.setText(String.format("%.7f", sim.DT));
                break;
            case 9: // Coeficiente Interacción
                sim.KU = (float) Math.pow(10.0, x/100.0);
                etqParam.setText(String.format("%.2f", sim.KU));
                break;                
            case 10: // Skip frames
                skip = x;
                if(worker != null) { worker.skip = skip; }
                etqParam.setText(String.format("1:%d", skip));
                break;                
            case 11: // Visualización
                verDensidad = x > 0;
                etqParam.setText(verDensidad ? "True" : "False");
                repaint();                
                break;
        }
    }  
}

class GUIWorker extends SwingWorker<Void, Void> {
    
    Simulador sim;
    JComponent panel;
    public int skip;
    
    public GUIWorker(Simulador sim, JComponent panel, int skip) { 
        this.sim = sim;
        this.panel = panel;
        this.skip = skip;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while(!isCancelled()) {
            for(int i = 0; i < skip; i++) { sim.PasoSimulacion(); }
            publish((Void) null);
        }
        return null;
    }   
    
    @Override
    protected void process(List<Void> chunks) {
        panel.repaint();
    }
}
