package no8;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public class ApplicationLoopTest {

  @Test
  public void fakeApplicationRuns() throws InterruptedException {
    Launcher launch = new Launcher(FakeApplication.class, Collections.emptyMap());

    assertThat(((FakeApplication) launch.application).loops(), is(0));

    // kills the test after a while
    new Thread(() -> {
      try {
        Thread.sleep(110);
      } catch (Exception e) {
        e.printStackTrace();
      }
      assertTrue(launch.application.loop().isStarted());
      launch.application.shutdown();
    }).start();

    launch.launch();
    assertThat(((FakeApplication) launch.application).loops(), not(0));
  }
}
