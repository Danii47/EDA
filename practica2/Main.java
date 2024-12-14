package practica2;

public class Main {
	public static void main(String[] args) {
		int dropletsSimulatorNormal = 10000;
		int dropletsSimulatorExt = 10000;

		int executionTimesSimulatorNormal = 200;
		int executionTimesSimulatorExt = 600;

		Simulador simulatorNormal = new Simulador(dropletsSimulatorNormal);
		SimuladorExt simulatorExt = new SimuladorExt(dropletsSimulatorExt);


		double tiempoPromedioNormal = getAverageTime(simulatorNormal, executionTimesSimulatorNormal);
		System.out.println("Para " + dropletsSimulatorNormal + " goticulas | Tiempo promedio: " + tiempoPromedioNormal + "ms");
		
		double tiempoPromedioExt = getAverageTime(simulatorExt, executionTimesSimulatorExt);
		System.out.println("Para " + dropletsSimulatorExt + " goticulas | Tiempo promedio: " + tiempoPromedioExt + "ms");

		System.out.println("Mejora de rendimiento (Normal / Ext): x" + (tiempoPromedioNormal / tiempoPromedioExt));

		// double averageOperationsNormal = getAverageOperations(simulatorExt, executionTimesSimulatorNormal);
		// System.out.println("Para " + dropletsSimulatorExt + " goticulas | Operaciones promedio: " + averageOperationsNormal);

		// double averageOperationsExt = getAverageOperations(simulatorExt, executionTimesSimulatorExt);
		// System.out.println("Para " + dropletsSimulatorExt + " goticulas | Operaciones promedio: " + averageOperationsExt);


		// javax.swing.SwingUtilities.invokeLater(() -> {
		// 	new GUIPtos(simulatorExt);
		// });

	}

	public static double getAverageTime(Simulador simulator, int executionTimes) {
		double tiempoTotal = 0;
		for (int i = 0; i < executionTimes; i++) {
			simulator.PasoSimulacion();
			tiempoTotal += simulator.tpo;
		}

		return tiempoTotal / executionTimes;
	}

	public static double getAverageOperations(Simulador simulator, int executionTimes) {
		double operations = 0;
		for (int i = 0; i < executionTimes; i++) {
			simulator.PasoSimulacion();
			operations += Simulador.NOPER;
		}

		return operations / executionTimes;
	}
}