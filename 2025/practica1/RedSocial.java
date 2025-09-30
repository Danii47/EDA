import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.util.HashSet;
import java.util.stream.IntStream;

public class RedSocial implements IRedSocial {
  private List<Conexion> red = new ArrayList<>();
  private List<Integer> usr = new ArrayList<>();
  private List<List<Integer>> grus = new ArrayList<>();
  private List<List<Integer>> grusFinales = new ArrayList<>();
  private List<Integer> asig = new ArrayList<>();
  private int n;
  private int m;

  public int numUsuarios() {
    return n;
  }

  public int numConexiones() {
    return m;
  }

  public int numGrumos() {
    return grus.size();
  }

  public void leeFichero(String nomfich) throws IOException {
    Scanner fichero;
    fichero = new Scanner(new File(nomfich));

    n = fichero.nextInt(); // número de usuarios
    m = fichero.nextInt(); // número de conexiones

    for (int i = 0; i < m; i++) {
      int id1 = fichero.nextInt();
      int id2 = fichero.nextInt();
      red.add(new Conexion(id1, id2));
    }

    fichero.close();
  }

  public void setRed(List<Conexion> red) {
    this.red = red;
  }

  public void creaUsuarios() {
    for (Conexion conexion : red) {
      if (!usr.contains(conexion.getUsr1()))
        usr.add(conexion.getUsr1());

      if (!usr.contains(conexion.getUsr2()))
        usr.add(conexion.getUsr2());
    }
  }

  public void creaGrumos() {
    for (int usuario : usr) {
      if (!asig.contains(usuario)) {
        List<Integer> grumo = new ArrayList<>();
        grumo.add(usuario);
        uber_amigos(usuario, red, grumo);
        grus.add(grumo);
        asig.addAll(grumo);
      }
    }
  }

  public void uber_amigos(int usuarioInicial, List<Conexion> red, List<Integer> grumo) {
    for (Conexion conexion : red) {
      if (conexion.getUsr1() == usuarioInicial && !grumo.contains(conexion.getUsr2())) {
        grumo.add(conexion.getUsr2());
        uber_amigos(conexion.getUsr2(), red, grumo);
      } else if (conexion.getUsr2() == usuarioInicial && !grumo.contains(conexion.getUsr1())) {
        grumo.add(conexion.getUsr1());
        uber_amigos(conexion.getUsr1(), red, grumo);
      }
    }
  }

  public void ordenaSelecciona(double pmin) {
    grus.sort((a, b) -> b.size() - a.size());

    int numeroUsuariosTotales = 0;

    for (List<Integer> grumo : grus) {
      if (numeroUsuariosTotales / (double) usr.size() * 100 > pmin)
        break;

      numeroUsuariosTotales += grumo.size();
      grusFinales.add(grumo);
    }
  }

  public void salvaNuevasRel(String nomfich) throws IOException {

  }

  public void informe() {
    System.out.println("Se deben unir los " + grusFinales.size() + " mayores");
  
    for (int i = 0; i < grusFinales.size(); i++) {
      System.out.println("#" + (i + 1) + ": " + grusFinales.get(i).size() + " usuarios ("
          + (grusFinales.get(i).size() / (double) usr.size() * 100) + "%)");
    }
  
    System.out.println("Nuevas relaciones de amistad (salvadas en extra.txt)");

    for (int i = 0; i < grusFinales.size() - 1; i++) {
      int usuarioBase = grusFinales.get(i).get(1);
      System.out.println(usuarioBase + " <-> " + grusFinales.get(i + 1).get(0));
    }
  }

  // public static List<Conexion> generaCaso(int n, Random rnd) {
  //   // Generar identificadores de usuarios
  //   HashSet<Integer> husr = new HashSet<>();
  //   while (husr.size() < n) {
  //     husr.add(rnd.nextInt(90000000) + 1000000);
  //   }
  //   Integer[] usr = husr.toArray(Integer[]::new);
  //   // Generar √n grumos usando rangos de índices de usuarios
  //   int[] inds = rnd.ints((int) Math.sqrt(n))
  //       .map(i -> Math.abs(i) % (n - 1) + 1)
  //       .sorted()
  //       .distinct()
  //       .toArray();
  //   inds[inds.length - 1] = n;
  //   // Añadir las conexiones de los grumos
  //   HashSet<Conexion> red = new HashSet<>();
  //   int i0 = 0;
  //   for (int i1 : inds) { // Rango [i0,i1)
  //     // Conexiones circulares
  //     red.addAll(IntStream.range(i0, i1 - 1)
  //         .mapToObj(i -> new Conexion(usr[i], usr[i + 1]))
  //         .toList());
  //     red.add(new Conexion(usr[i1 - 1], usr[i0]));
  //     // Conexiones al azar
  //     int ng = 2 * (i1 - i0);
  //     for (int k = 0; k < ng; k++) {
  //       int u1 = rnd.nextInt(i0, i1);
  //       int u2 = rnd.nextInt(i0, i1);
  //       if (u1 != u2) {
  //         red.add(new Conexion(usr[u1], usr[u2]));
  //       }
  //     }
  //     i0 = i1;
  //   }
  //   return red.stream().toList();
  // }

  public static void main(String[] args) {
    long startTime;
    long endTime;

    RedSocial rs = new RedSocial();
    Scanner scanner = new Scanner(System.in);

    System.out.println("ANÁLISIS DE Y");
    System.out.println("-------------");

    System.out.print("Fichero principal: ");
    String ficheroPrincipal = scanner.nextLine();

    try {
      startTime = System.currentTimeMillis();
      rs.leeFichero(ficheroPrincipal);
      endTime = System.currentTimeMillis();

      System.out.print("Fichero de nuevas conexiones (pulse enter si no existe): ");
      String ficheroNuevasConexiones = scanner.nextLine();

      if (!ficheroNuevasConexiones.isEmpty())
        rs.leeFichero(ficheroNuevasConexiones);

    } catch (IOException e) {
      System.out.println("Error al leer el fichero: " + e.getMessage());
      scanner.close();
      return;
    }

    System.out.print("Porcentaje tamaño mayor grumo: ");
    int porcentaje = scanner.nextInt();

    System.out.println("Lectura fichero: " + ((endTime - startTime) / 1000.0) + " seg.");
  
    startTime = System.currentTimeMillis();
    rs.creaUsuarios();
    endTime = System.currentTimeMillis();
    
    System.out.println("Creación lista usuarios: " + ((endTime - startTime) / 1000.0) + " seg.");

    startTime = System.currentTimeMillis();
    rs.creaGrumos();
    endTime = System.currentTimeMillis();

    System.out.println("Creación lista grumos: " + ((endTime - startTime) / 1000.0) + " seg.");

    startTime = System.currentTimeMillis();
    rs.ordenaSelecciona(porcentaje);
    endTime = System.currentTimeMillis();

    System.out.println("Ordenación y selección de grumos: " + ((endTime - startTime) / 1000.0) + " seg.");
    
    System.out.println(rs.numUsuarios() + " usuarios, " + rs.numConexiones() + " conexiones");
    System.out.println("Existen " + rs.numGrumos() + " grumos.");
  
    rs.informe();

    scanner.close();
  }
}
