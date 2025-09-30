import java.io.IOException;
import java.util.List;

public interface IRedSocial {
  public int numUsuarios();
  public int numConexiones();
  public int numGrumos();
  public void leeFichero(String nomfich) throws IOException;
  public void setRed(List<Conexion> red);
  public void creaUsuarios();
  public void creaGrumos();
  public void ordenaSelecciona(double pmin);
  public void salvaNuevasRel(String nomfich) throws IOException;
  public void informe();
}
