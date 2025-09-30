public class Conexion {
  private int usr1;
  private int usr2;

  public Conexion(int usr1, int usr2) {
    this.usr1 = usr1;
    this.usr2 = usr2;
  }

  public int getUsr1() {
    return usr1;
  }

  public int getUsr2() {
    return usr2;
  }

  @Override
  public int hashCode() {
    return usr1 ^ usr2;
  }

  @Override
  public boolean equals(Object obj) {
    if (getClass() != obj.getClass())
      return false;

    Conexion otro = (Conexion) obj;
    return (usr1 == otro.usr1 && usr2 == otro.usr2) ||
        (usr1 == otro.usr2 && usr2 == otro.usr1);
  }

  @Override
  public String toString() {
    return String.format("%d %d", usr1, usr2);
  }
}