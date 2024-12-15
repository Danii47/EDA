/*
 * @authors Daniel del Pozo Gomez & Daniel Fernandez Varona
*/

import java.util.HashMap;
import java.util.ArrayList;

public class SimuladorExt extends Simulador {

  private final float SCALE_PROPORTION = 1.75f;

  HashMap<Grid, ArrayList<Goticula>> dropletsHashMap = new HashMap<>();

  public SimuladorExt(int n) {
    super(n);
  }

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
          for (Goticula g : droplet) {
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
          for (Goticula g : droplet) {
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
          for (Goticula g : droplet) {
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
      Grid grid = new Grid(g.x, g.y);
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