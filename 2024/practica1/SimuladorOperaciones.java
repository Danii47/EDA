/**
 * @author Daniel del Pozo Gómez y Daniel Fernández Varona
 */

public class SimuladorOperaciones {
  public static void main(String[] args) {
    int ejecuciones = 100;

    for (int i = 10; i <= 1000; i += 10) {

      SimuladorEditado simulador = new SimuladorEditado(i);
      double suma = 0;

      for (int j = 0; j < (ejecuciones - (i / 25)); j++) {
        simulador.PasoSimulacion();
        suma += SimuladorEditado.OPERATIONS_NUMBER;
      }
      System.out.println((long) (suma / (ejecuciones - (i / 25))));
    }
  }
}
