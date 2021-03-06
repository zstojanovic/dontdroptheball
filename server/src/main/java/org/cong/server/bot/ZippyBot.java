package org.cong.server.bot;

import com.badlogic.gdx.math.MathUtils;
import org.cong.server.Paddle;

public class ZippyBot extends Bot {

  public ZippyBot() {
    name = "Zippy_" + MathUtils.random(999);
  }

  @Override
  public void setup(Paddle paddle) {
    super.setup(paddle);
    if (MathUtils.random() > 0.5) paddle.goLeft(); else paddle.goRight();
  }

  @Override
  public void think(float delta) {
  }
}