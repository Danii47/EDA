package practica2;

import java.util.*;

public class SimuladorExt extends Simulador {

  private final float SCALE_PROPORTION = 1.75f;

  public class Grid {
    public int x;
    public int y;

    public Grid(Goticula g) {
      x = Math.round(g.x * SCALE_PROPORTION);
      y = Math.round(g.y * SCALE_PROPORTION);
    }

    public Grid(int i, int j) {
      x = i;
      y = j;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Grid)) {
        return false;
      }

      Grid g = (Grid) obj;
      return x == g.x && y == g.y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y);
    }
  }

  public SimuladorExt(int n) {
    super(n);
  }

  Map<Grid, ArrayList<Goticula>> dropletsHashMap = new HashMap<>();

  @Override
  protected float CalcDensidad(float x, float y) {
    float density = 0;

    int xGrid = getCoord(x);
    int yGrid = getCoord(y);

    for (int i = xGrid - 1; i <= xGrid + 1; i++) {
      for (int j = yGrid - 1; j <= yGrid + 1; j++) {
        Grid searchGrid = new Grid(i, j);
        ArrayList<Goticula> droplet = dropletsHashMap.getOrDefault(searchGrid, null);
        if (droplet != null) {
          for (Goticula g: droplet) {
            density += CalcDensidadIter(x, y, g);
          }
        }
      }
    }
    return density;
  }

  @Override
  protected VecXY CalcPresion(Goticula gi) {
    VecXY f = new VecXY(0, 0);

    int xGrid = getCoord(gi.x);
    int yGrid = getCoord(gi.y);

    for (int i = xGrid - 1; i <= xGrid + 1; i++) {
      for (int j = yGrid - 1; j <= yGrid + 1; j++) {
        Grid searchGrid = new Grid(i, j);
        ArrayList<Goticula> droplet = dropletsHashMap.getOrDefault(searchGrid, null);
        if (droplet != null) {
          for (Goticula g: droplet) {
            f.Add(CalcPresionIter(gi, g));
          }
        }
      }
    }
    f.Scale(1 / (gi.d));
    return f;
  }

  @Override
  protected VecXY CalcViscosidad(Goticula gi) {
    VecXY f = new VecXY(0, 0);

    int xGrid = getCoord(gi.x);
    int yGrid = getCoord(gi.y);

    for (int i = xGrid - 1; i <= xGrid + 1; i++) {
      for (int j = yGrid - 1; j <= yGrid + 1; j++) {
        Grid searchGrid = new Grid(i, j);
        ArrayList<Goticula> droplet = dropletsHashMap.getOrDefault(searchGrid, null);
        if (droplet != null) {
          for (Goticula g: droplet) {
            f.Add(CalcViscosidadIter(gi, g));
          }
        }
      }
    }
    return f;
  }

  @Override
  protected void ReestructuraED() {
    dropletsHashMap.clear();
    for (Goticula g : gotas) {
      Grid grid = new Grid(g);
      ArrayList<Goticula> droplet = dropletsHashMap.get(grid);
      if (droplet != null) {
        dropletsHashMap.get(grid).add(g);
      } else {
        ArrayList<Goticula> list = new ArrayList<>();
        list.add(g);
        dropletsHashMap.put(grid, list);
      }
    }
  }

  protected int getCoord(float x) {
    return Math.round(x * SCALE_PROPORTION);
  }
}