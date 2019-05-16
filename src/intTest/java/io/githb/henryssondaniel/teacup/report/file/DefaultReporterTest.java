package io.githb.henryssondaniel.teacup.report.file;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.henryssondaniel.teacup.core.reporting.Reporter;
import io.github.henryssondaniel.teacup.core.testing.Factory;
import io.github.henryssondaniel.teacup.core.testing.Node;
import io.github.henryssondaniel.teacup.core.testing.Status;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Saves files to the hard drive")
class DefaultReporterTest {
  private final Node node = Factory.createNode("name", Collections.emptyList());
  private final Reporter reporter = new DefaultReporter();

  @Test
  void finishedAborted() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.finished(node, Factory.createResult(Status.ABORTED, null));

    assertThat(getMap()).isEmpty();
  }

  @Test
  void finishedFailed() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.finished(node, Factory.createResult(Status.FAILED, new SQLException("test")));

    assertThat(getMap()).isEmpty();
  }

  @Test
  void finishedSuccessful() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.finished(node, Factory.createResult(Status.SUCCESSFUL, null));

    assertThat(getMap()).isEmpty();
  }

  @Test
  void finishedWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.finished(node, Factory.createResult(Status.SUCCESSFUL, null));
    assertThat(getMap()).isEmpty();
  }

  @Test
  void finishedWhenNotInitialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.finished(node, Factory.createResult(Status.SUCCESSFUL, null));

    assertThat(getMap()).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private Map<Node, Integer> getMap() throws IllegalAccessException, NoSuchFieldException {
    var field = DefaultReporter.class.getDeclaredField("map");
    field.setAccessible(true);

    return (Map<Node, Integer>) field.get(reporter);
  }
}
