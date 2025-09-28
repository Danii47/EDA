import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {


  public static void main(String[] args) {
    ArrayList<Conexion> red;
    ArrayList<Integer> usr;
    ArrayList<ArrayList<Integer>> grus = new ArrayList<>();
    ArrayList<Integer> asig = new ArrayList<>();

    long startTime;
    long endTime;

    Scanner scanner = new Scanner(System.in);

    System.out.println("ANÁLISIS DE Y");
    System.out.println("-------------");

    System.out.print("Fichero principal: ");
    String ficheroPrincipal = scanner.nextLine();

    red = getConexiones(ficheroPrincipal, scanner);

    System.out.print("Porcentaje tamaño mayor grumo: ");
    int porcentaje = scanner.nextInt();

    startTime = System.currentTimeMillis();
    usr = getUsuarios(red);
    endTime = System.currentTimeMillis();

    System.out.println("Creación lista usuarios: " + ((endTime - startTime) / 1000.0) + " seg.");
    
    startTime = System.currentTimeMillis();
    for (int usuario : usr) {
      if (!asig.contains(usuario)) {
        ArrayList<Integer> grumo = new ArrayList<>();
        grumo.add(usuario);
        uber_amigos(usuario, red, grumo);
        grus.add(grumo);
        asig.addAll(grumo);
      }
    }
    endTime = System.currentTimeMillis();

    System.out.println("Creación lista grumos: " + ((endTime - startTime) / 1000.0) + " seg.");

    startTime = System.currentTimeMillis();
    grus.sort((a, b) -> b.size() - a.size());

    int numeroUsuariosTotales = 0;
    ArrayList<ArrayList<Integer>> grumosFinales = new ArrayList<>();

    for (ArrayList<Integer> grumo : grus) {
      if (numeroUsuariosTotales / usr.size() * 100 > porcentaje)
        break;

      numeroUsuariosTotales += grumo.size();
      grumosFinales.add(grumo);
    }
    endTime = System.currentTimeMillis();
    
    System.out.println("Ordenación y selección de grumos: " + ((endTime - startTime) / 1000.0) + " seg.");
    System.out.println("Existen " + grus.size() + " grumos");

    System.out.println("Se deben unir los " + grumosFinales.size() + " mayores");

    for (int i = 0; i < grumosFinales.size(); i++) {
      System.out.println("#" + (i + 1) + ": " + grumosFinales.get(i).size() + " usuarios (" + (grumosFinales.get(i).size() / (double) usr.size() * 100) + "%)");
    }

    System.out.println("Nuevas relaciones de amistad (salvadas en extra.txt)");

    for (int i = 0; i < grumosFinales.size() - 1; i++) {
      int usuarioBase = grumosFinales.get(i).get(0);
      System.out.println(usuarioBase + " <-> " + grumosFinales.get(i + 1).get(1));
    }
  }
  
  public static void uber_amigos(int usuarioInicial, ArrayList<Conexion> red, ArrayList<Integer> grumo) {
    for (Conexion conexion : red) {
      if (conexion.getId1() == usuarioInicial && !grumo.contains(conexion.getId2())) {
        grumo.add(conexion.getId2());
        uber_amigos(conexion.getId2(), red, grumo);
      } else if (conexion.getId2() == usuarioInicial && !grumo.contains(conexion.getId1())) {
        grumo.add(conexion.getId1());
        uber_amigos(conexion.getId1(), red, grumo);
      }
    }
  }
  
  public static ArrayList<Conexion> getConexiones(String nombreFichero, Scanner scanner) {
    ArrayList<Conexion> conexiones = new ArrayList<>();
    Scanner fichero;

    long startTime;
    long endTime;

    try {
      fichero = new Scanner(new File(nombreFichero));
      startTime = System.currentTimeMillis();
    } catch (Exception e) {
      System.out.println("Error al abrir el fichero " + nombreFichero);
      return conexiones;
    }

    int numeroUsuarios = fichero.nextInt();
    int numeroConexiones = fichero.nextInt();

    for (int i = 0; i < numeroConexiones; i++) {
      int id1 = fichero.nextInt();
      int id2 = fichero.nextInt();
      conexiones.add(new Conexion(id1, id2));
    }

    fichero.close();
    endTime = System.currentTimeMillis();

    System.out.println("Lectura fichero: " + ((endTime - startTime) / 1000.0) + " seg.");

    System.out.print("Fichero de nuevas conexiones (pulse enter si no existe): ");
    String ficheroNuevasConexiones = scanner.nextLine();

    if (!ficheroNuevasConexiones.isEmpty()) {
      try {
        fichero = new Scanner(new File(ficheroNuevasConexiones));
      } catch (Exception e) {
        System.out.println("Error al abrir el fichero " + ficheroNuevasConexiones);
        return conexiones;
      }

      while (fichero.hasNextLine()) {
        int id1 = fichero.nextInt();
        int id2 = fichero.nextInt();
        conexiones.add(new Conexion(id1, id2));
        numeroConexiones++;
      }

      fichero.close();
    }

    System.out.println(numeroUsuarios + " usuarios, " + numeroConexiones + " conexiones");

    return conexiones;
  }

  public static ArrayList<Integer> getUsuarios(ArrayList<Conexion> conexiones) {
    ArrayList<Integer> usuarios = new ArrayList<>();

    for (Conexion conexion : conexiones) {
      if (!usuarios.contains(conexion.getId1())) {
        usuarios.add(conexion.getId1());
      }
      if (!usuarios.contains(conexion.getId2())) {
        usuarios.add(conexion.getId2());
      }
    }

    return usuarios;
  }
}
