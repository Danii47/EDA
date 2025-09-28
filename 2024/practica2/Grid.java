/*
 * @authors Daniel del Pozo Gomez & Daniel Fernandez Varona
*/

import java.util.Objects;

public class Grid {
  private final float SCALE_PROPORTION = 1.75f;

  public int x;
  public int y;

  public Grid(float x, float y) {
    this.x = Math.round(x * SCALE_PROPORTION);
    this.y = Math.round(y * SCALE_PROPORTION);
  }

  public Grid(int i, int j) {
    this.x = i;
    this.y = j;
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
