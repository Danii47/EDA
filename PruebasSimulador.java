public class PruebasSimulador {
  public static void main(String[] args) {
    int ejecuciones = 100;

    for (int i = 10; i <= 1000; i += 10) {
      
      Simulador simulador = new Simulador(i);
      double suma = 0;

      for (int j = 0; j < (ejecuciones - (i / 25)); j++) {
        simulador.PasoSimulacion();
        suma += simulador.tpo;
      }
      System.out.println(suma / 100);
    }
  }
}
