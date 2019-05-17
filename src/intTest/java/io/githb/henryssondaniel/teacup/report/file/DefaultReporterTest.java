package io.githb.henryssondaniel.teacup.report.file;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.henryssondaniel.teacup.core.reporting.Reporter;
import io.github.henryssondaniel.teacup.core.testing.Factory;
import io.github.henryssondaniel.teacup.core.testing.Node;
import io.github.henryssondaniel.teacup.core.testing.Status;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultReporterTest {

  private static final String MESSAGE = "message";
  private static final String REASON = "reason";

  private final Node node = Factory.createNode("name", Collections.emptyList());
  private final Reporter reporter = new DefaultReporter();

  @BeforeEach
  void beforeEach() throws InterruptedException {
    Thread.sleep(1L);
  }

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

  @Test
  void initialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    assertThat(getMap()).isEmpty();
  }

  @Test
  void initializeWhenAlreadyExists() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialize();

    assertThat(getMap()).isEmpty();
  }

  @Test
  void initialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));

    assertThat(getMap()).containsOnlyKeys(node);
  }

  @Test
  void initializedWhenAlreadyInitialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();

    Collection<Node> nodes = new ArrayList<>(2);
    nodes.add(node);
    nodes.add(node);

    reporter.initialized(nodes);

    assertThat(getMap()).containsOnlyKeys(node);
  }

  @Test
  void initializedWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialized(Collections.singletonList(node));
    assertThat(getMap()).isEmpty();
  }

  @Test
  void log() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.log(new LogRecord(Level.INFO, MESSAGE), node);

    assertThat(getMap()).containsOnlyKeys(node);
  }

  @Test
  void logWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.log(new LogRecord(Level.INFO, MESSAGE), node);
    assertThat(getMap()).isEmpty();
  }

  @Test
  void logWhenNotInitialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.log(new LogRecord(Level.INFO, MESSAGE), node);

    assertThat(getMap()).isEmpty();
  }

  @Test
  void skipped() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.skipped(node, REASON);

    assertThat(getMap()).isEmpty();
  }

  @Test
  void skippedWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.skipped(node, REASON);
    assertThat(getMap()).isEmpty();
  }

  @Test
  void skippedWhenNotInitialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.skipped(node, REASON);

    assertThat(getMap()).isEmpty();
  }

  @Test
  void started() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));
    reporter.started(node);

    assertThat(getMap()).containsOnlyKeys(node);
  }

  @Test
  void startedWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.started(node);
    assertThat(getMap()).isEmpty();
  }

  @Test
  void startedWhenNotInitialized() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.started(node);

    assertThat(getMap()).isEmpty();
  }

  @Test
  void terminated() throws IllegalAccessException, NoSuchFieldException {
    reporter.initialize();
    reporter.terminated();

    assertThat(getMap()).isEmpty();
  }

  @Test
  void terminatedWhenNotInitialize() throws IllegalAccessException, NoSuchFieldException {
    reporter.terminated();
    assertThat(getMap()).isEmpty();
  }

  @SuppressWarnings("unchecked")
  private Map<Node, Integer> getMap() throws IllegalAccessException, NoSuchFieldException {
    var field = DefaultReporter.class.getDeclaredField("map");
    field.setAccessible(true);

    return (Map<Node, Integer>) field.get(reporter);
  }
}
