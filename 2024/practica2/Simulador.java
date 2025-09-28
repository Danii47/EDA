//Versión 3 del Simulador para la Segunda Práctica

public class Simulador {

    public static long NOPER; // Contador del número de operaciones

    public static class VecXY {
        public static VecXY Zero = new VecXY(0, 0);

        public float x, y;

        public VecXY(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void Add(VecXY otro) {
            x += otro.x;
            y += otro.y;
            NOPER += 2;
        }

        public void Scale(float k) {
            x *= k;
            y *= k;
            NOPER += 2;
        }
    }

    public class Goticula {
        public float x, y; // Posición
        public float vx, vy; // Velocidad
        public float xa, ya; // Posición anterior
        public VecXY fp; // Fuerza debida al diferencial de presión
        public VecXY fu; // Fuerza debida a la interacción del usuario
        public VecXY fv; // Fuerza debida a la viscosidad
        public float d; // Densidad en la posición de la gotícula

        public void SetPos(float xn, float yn, int lx, int ly) {
            x = xn;
            y = yn;
            if (x < 0) {
                x = 0;
                vx *= -0.95f;
                NOPER++;
            }
            if (y < 0) {
                y = 0;
                vy *= -0.95f;
                NOPER++;
            }
            if (x >= lx) {
                x = lx - 0.00001f;
                vx *= -0.95f;
                NOPER += 2;
            }
            if (y >= ly) {
                y = ly - 0.00001f;
                vy *= -0.95f;
                NOPER += 2;
            }
        }

        @Override
        public String toString() {
            return String.format("pos:[%.3f;%.3f] vel:[%.3f;%.3f]", x, y, vx, vy);
        }
    }

    // Parámetros
    public float DT = 0.005f; // Paso de integración
    public float D0 = 6.0f; // Densidad objetivo
    public float KP = 1000.0f; // Conversión fuerza-presión
    public float KV = 0.5f; // Conversión fuerza-viscosidad
    public float KU = 100.0f; // Factor interacción usuario
    public float GX = 0.0f; // Gravedad (x)
    public float GY = -9.81f; // Gravedad (y)
    static float KK = (float) (6 / Math.PI); // Normalización del Kernel

    // Variables
    public Goticula[] gotas; // Las gotículas
    public int n; // Número de gotículas
    public int lx; // Longitud del tanque
    public int ly; // Altura del tanque
    public double tpo; // Tiempo de la última ejecución (milisegundos)
    public float xu, yu; // Posición del ratón (xu < 0 si no se aplica)
    public float ru; // Radio fuerza usuario
    public float su; // Signo intensidad fuerza usuario

    /**
     * Atención: En vuestro constructor debeis incluir la llamada a éste
     * (mediante super()) o dara error de compilación. Deberíais añadir las
     * operaciones de creación e inicialización de las estructuras de datos de
     * apoyo
     */
    public Simulador(int n) {
        this.n = n;
        xu = -1;
        CalculaDimensiones(); // <- Nuevo! Se calcula lx e ly antes de llamar a CreaGoticulas
        CreaGoticulas();
        ColocaGoticulasEnCuadrado();
    }

    /**
     * Atención: Si creais una clase que extienda a Goticula entonces debéis
     * sobrescribir (override) éste método en vuestra clase para que al crear
     * las gotículas sean objetos de la clase extendida. Ver ejemplo depositado
     * en el Campus Virtual
     */
    public void CreaGoticulas() {
        // Creamos y colocamos las gotículas
        gotas = new Goticula[n];
        for (int i = 0; i < n; i++) {
            gotas[i] = new Goticula();
        }
    }

    /**********************************************************************
     ************************ ZONA NO MODIFICABLE *************************
     *********************************************************************/

    /**
     * Calcula las dimensiones (lx, ly) del tanque
     */
    public final void CalculaDimensiones() {
        // Calculamos las dimensiones adecuadas del tanque
        int nfil = (int) Math.floor(Math.sqrt(n));
        int ncol = (n - 1) / nfil + 1;
        float sep = (float) Math.sqrt(n / (nfil * ncol * D0));
        ly = (int) Math.ceil((nfil + 1) * sep);
        lx = 16 * ly / 9;
        ru = ly / 5f;
    }

    /**
     * Dispone las gotículas en la configuración inicial
     */
    public final void ColocaGoticulasEnCuadrado() {
        int nfil = (int) Math.floor(Math.sqrt(n));
        int ncol = (n - 1) / nfil + 1;
        float sep = (float) Math.sqrt(n / (nfil * ncol * D0));
        int i = 0;
        for (Goticula g : gotas) {
            float x = sep * (i % ncol + 1);
            float y = sep * (i / ncol + 1);
            g.SetPos(x + 0.0001f * y, y - 0.0001f * x, lx, ly);
            i++;
        }
    }

    /**
     * Devuelve la distancia entre los puntos (x1,y1) y (x2,y2)
     * 
     * @param x1 La posición x del primer punto
     * @param y1 La posición y del primer punto
     * @param x2 La posición x del segundo punto
     * @param y2 La posición y del segundo punto
     * @return La distancia euclidea entre los puntos
     */
    protected final float Dist(float x1, float y1, float x2, float y2) {
        NOPER += 8;
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Función de ponderación (kernel)
     * 
     * @param d Distancia
     * @return El valor de ponderación a esa distancia
     */
    protected final float Kernel(float d) {
        if (d <= 1) {
            NOPER += 4;
        }
        return d > 1 ? 0 : KK * (1 - d) * (1 - d);
    }

    /**
     * Derivada de la función de ponderación
     * 
     * @param d Distancia
     * @return La derivada de la función a esa distancia
     */
    protected final float DKernel(float d) {
        if (d <= 1) {
            NOPER += 3;
        }
        return d > 1 ? 0 : KK * 2 * (d - 1);
    }

    /**
     * Calcula la contribución de cada gotícula a la densidad en el punto (x,y)
     * 
     * @param x Posición x del punto
     * @param y Posición y del punto
     * @param g Gotícula que se considera
     * @return La contribución de la gotícula a la densidad
     */
    protected final float CalcDensidadIter(float x, float y, Goticula g) {
        NOPER++; // Tenemos en cuenta aquí la suma que se realiza en CalcDensidad
        return Kernel(Dist(x, y, g.x, g.y));
    }

    /**
     * Devuelve la fuerza de presión que ejerce una gotícula sobre otra
     * 
     * @param gi Gotícula objetivo
     * @param gj Gotícula que ejerce la fuerza
     * @return Vector de fuerza de la presión de gj sobre gi
     */
    protected final VecXY CalcPresionIter(Goticula gi, Goticula gj) {
        if (gi == gj) {
            return VecXY.Zero;
        }
        // Distancia entre goticulas (atención: se usa la posición salvada)
        float dist = Dist(gi.xa, gi.ya, gj.xa, gj.ya);
        // Si las partículas estan muy juntas puede dar problemas numéricos
        if (dist < 0.0001f) {
            return VecXY.Zero;
        }
        NOPER += 13;
        // Diferencial de densidad en la otra partícula
        float dj = gj.d;
        float fr = KP * 0.5f * ((gi.d - D0) + (dj - D0)) * DKernel(dist) / dj;
        // Devolvemos la fuerza
        return new VecXY(fr * (gj.xa - gi.xa) / dist, fr * (gj.ya - gi.ya) / dist);
    }

    /**
     * Devuelve la fuerza viscosa que ejerce una gotícula sobre otra
     * 
     * @param gi Gotícula objetivo
     * @param gj Gotícula que ejerce la fuerza
     * @return Vector de fuerza viscosa de gj sobre gi
     */
    protected final VecXY CalcViscosidadIter(Goticula gi, Goticula gj) {
        if (gi == gj) {
            return VecXY.Zero;
        }
        // Distancia entre goticulas (atención: se usa la posición salvada)
        float dist = Dist(gi.xa, gi.ya, gj.xa, gj.ya);
        if (dist < 0.0001f) {
            return VecXY.Zero;
        }
        NOPER += 7;
        // Si las partículas estan muy juntas puede dar problemas numéricos
        float fr = KV * Kernel(dist);
        // Cada gotícula debe tender a tener la misma velocidad que sus vecinas
        return new VecXY(fr * (gj.vx - gi.vx) / dist, fr * (gj.vy - gi.vy) / dist);
    }

    /**
     * Calcula la fuerza debida a la interacción del usuario.
     * 
     * @param g La gotícula sobre la que se calcula la fuerza
     * @return La fuerza (fx,fy) que se realiza sobre la gotícula. Se devuelve
     *         en un vector (x,y) (clase VecXY).
     */
    protected final VecXY CalcInteraccion(Goticula g) {
        if (xu < 0) {
            return VecXY.Zero;
        }
        // Distancia entre la goticula y el punto marcado por el usuario
        float dist = Dist(xu, yu, g.x, g.y);
        if (dist > ru) {
            return VecXY.Zero;
        }
        float dx = 0, dy = 0; // Dirección al punto, normalizada
        if (dist > 0.0001f) {
            NOPER += 4;
            dx = (xu - g.x) / dist;
            dy = (yu - g.y) / dist;
        }
        NOPER += 10;
        float fr = 1 - dist / ru;
        return new VecXY(fr * (su * KU * dx - g.vx), fr * (su * KU * dy - g.vy));
    }

    public final void Click(float x, float y, boolean implosion) {
        xu = x;
        yu = y;
        su = implosion ? -1 : 1;
    }

    public final void NoClick() {
        xu = -1;
    }

    /**
     * Realiza un paso de la simulación, integrando el sistema un intervalo
     * de tiempo DT. Se actualizan las posiciones y velocidades de las
     * gotículas (array gotas).
     * Se utiliza el método de Euler con algún que otro truco para mejorar la
     * estabilidad de la simulación.
     */
    public final void PasoSimulacion() {
        NOPER = 0;
        long t0 = System.nanoTime();
        // Aplicamos la fuerza de la gravedad y predecimos la nueva posición
        for (Goticula g : gotas) {
            // Salvamos la posición anterior
            g.xa = g.x;
            g.ya = g.y;
            NOPER += 8;
            // Aplicamos la gravedad
            g.vx += DT * GX;
            g.vy += DT * GY;
            // Actualizamos la posición
            g.SetPos(g.x + DT * g.vx, g.y + DT * g.vy, lx, ly);
        }
        // Adaptación de vuestras estructuras de datos al cambio de posición de las
        // gotículas
        ReestructuraED();
        // Precalculamos y almacenamos la densidad
        for (Goticula g : gotas) {
            g.d = CalcDensidad(g.x, g.y);
        }
        // Calculamos la fuerza debida a los diferenciales de presión,
        // la viscosidad y la interacción del usuario
        for (Goticula g : gotas) {
            g.fp = CalcPresion(g);
            g.fu = CalcInteraccion(g);
            g.fv = CalcViscosidad(g);
        }
        // Aplicamos las fuerzas calculadas y almacenadas en el bucle anterior
        for (Goticula g : gotas) {
            NOPER += 12;
            g.vx += DT * (g.fp.x + g.fu.x + g.fv.x);
            g.vy += DT * (g.fp.y + g.fu.y + g.fv.y);
            // Actualizamos la posición (usamos la posición original)
            g.SetPos(g.xa + DT * g.vx, g.ya + DT * g.vy, lx, ly);
        }
        tpo = 1e-6 * (System.nanoTime() - t0);
    }

    /**********************************************************************
     ******************** ZONA MODIFICABLE POR HERENCIA *******************
     *********************************************************************/

    /**
     * Calcula la densidad en los alrededores del punto (x,y).
     * Recorre todas las gotículas y suma su masa (que es 1 por definición)
     * ponderada según su distancia al punto (x,y) usando el Kernel.
     * Las gotículas más cercanas influiran más que las lejanas, y las que
     * estén a una distancia mayor que 1 no influiran.
     * 
     * @param x Posición x del punto
     * @param y Posición y del punto
     * @return La densidad alrededor de ese punto
     */
    protected float CalcDensidad(float x, float y) {
        float densidad = 0;
        for (Goticula g : gotas) {
            // La suma ya se cuenta en CalcDensidad
            densidad += CalcDensidadIter(x, y, g);
        }
        return densidad;
    }

    /**
     * Calcula la fuerza debida a diferenciales de presión.
     * Atención: La densidad se calcula con las posiciones actualizadas y
     * la presión con la posición original de las partículas (para que la
     * simulación sea más estable)
     * 
     * @param gi La gotícula sobre la que se calcula la fuerza
     * @return La fuerza que se realiza sobre la gotícula. Se devuelve
     *         en un vector (x,y) (clase VecXY).
     */
    protected VecXY CalcPresion(Goticula gi) {
        VecXY f = new VecXY(0, 0);
        // Se acumula el efecto del resto de gotículas
        for (Goticula gj : gotas) {
            f.Add(CalcPresionIter(gi, gj));
        }
        f.Scale(1 / gi.d);
        return f;
    }

    /**
     * Calcula la fuerza debida a la viscosidad entre gotículas.
     * 
     * @param gi La gotícula sobre la que se calcula la fuerza
     * @return La fuerza (fx,fy) que se realiza sobre la gotícula. Se devuelve
     *         en un vector (x,y) (clase VecXY).
     */
    protected VecXY CalcViscosidad(Goticula gi) {
        VecXY f = new VecXY(0, 0);
        for (Goticula gj : gotas) {
            f.Add(CalcViscosidadIter(gi, gj));
        }
        return f;
    }

    /**
     * Método que se debe sobreescribir para adaptar vuestras estructuras de
     * datos auxiliares (ED) al cambio de posición de las gotículas.
     * Nota: Si no necesitais adaptar vuestras ED o bien lo realizais
     * sobreescribiendo el método SetPos en una clase que hereda de Gotícula
     * entonces no sobreescribais el método (que no hace nada), por supuesto.
     */
    protected void ReestructuraED() {

    }

}
