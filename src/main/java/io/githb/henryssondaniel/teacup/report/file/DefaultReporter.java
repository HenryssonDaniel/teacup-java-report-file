package io.githb.henryssondaniel.teacup.report.file;

import io.github.henryssondaniel.teacup.core.reporting.Reporter;
import io.github.henryssondaniel.teacup.core.testing.Node;
import io.github.henryssondaniel.teacup.core.testing.Result;
import io.github.henryssondaniel.teacup.core.testing.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Reporter that saves the logs into a file hierarchy. Each node gets its own folder together with a
 * log file.
 *
 * @since 1.0
 */
public class DefaultReporter implements Reporter {
  private static final String DIRECTORY_ERROR =
      "The directory %s could not be created. All logs belonging to this directory will not be saved.";
  private static final String DIRECTORY_EXISTS =
      "The directory {0} does already exist. All logs belonging to this directory will not be saved.";
  private static final String FILE_ERROR =
      "The file %s could not be created. The logs will not be saved.";
  private static final String LOG = ".log";
  private static final Logger LOGGER = Logger.getLogger(DefaultReporter.class.getName());
  private static final String STARTED = "Started";

  private final Map<Node, Path> map = new HashMap<>(0);
  private final Path realPath;

  private int aborted;
  private int failed;
  private Path rootLog;
  private Path rootPath;
  private int skipped;
  private int successful;

  /**
   * Constructor.
   *
   * @since 1.0
   */
  public DefaultReporter() {
    this(
        Path.of(
            System.getProperty("user.home"),
            ".teacup",
            "logs",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS"))));
  }

  DefaultReporter(Path realPath) {
    this.realPath = realPath;
  }

  @Override
  public void finished(Node node, Result result) {
    LOGGER.log(Level.FINE, "Finished");

    var status = result.getStatus();

    if (status == Status.ABORTED) aborted++;
    else if (status == Status.FAILED) failed++;
    else successful++;

    var path = Optional.ofNullable(map.remove(node)).orElseGet(() -> rootLog);
    if (path != null)
      createHandler(path.toString())
          .ifPresent(
              handler ->
                  log(
                      handler,
                      new LogRecord(
                          Level.INFO,
                          "Finished with status: "
                              + status
                              + " after "
                              + (node.getTimeFinished() - node.getTimeStarted())
                              + " ms."
                              + result.getThrowable().map(Throwable::toString).orElse(""))));
  }

  @Override
  public void initialize() {
    if (Files.exists(realPath)) LOGGER.log(Level.SEVERE, DIRECTORY_EXISTS, realPath);
    else
      try {
        rootPath = Files.createDirectories(realPath);
        rootLog = createRootLog(realPath.resolve(LOG));
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, String.format(DIRECTORY_ERROR, realPath), e);
      }
  }

  @Override
  public void initialized(Collection<? extends Node> nodes) {
    LOGGER.log(Level.FINE, "Initialized");

    if (rootPath != null) createDirectories(map, nodes, rootPath);
  }

  @Override
  public void log(LogRecord logRecord, Node node) {
    LOGGER.log(Level.FINE, "Log");

    var path = Optional.ofNullable(map.get(node)).orElseGet(() -> rootLog);
    if (path != null) createHandler(path.toString()).ifPresent(handler -> log(handler, logRecord));
  }

  @Override
  public void skipped(Node node, String reason) {
    LOGGER.log(Level.INFO, "Skipped");

    var path = Optional.ofNullable(map.remove(node)).orElseGet(() -> rootLog);
    if (path != null) {
      skipped++;

      createHandler(path.toString())
          .ifPresent(
              handler -> log(handler, new LogRecord(Level.INFO, "Skipped with reason: " + reason)));
    }
  }

  @Override
  public void started(Node node) {
    LOGGER.log(Level.FINE, STARTED);

    var path = Optional.ofNullable(map.get(node)).orElseGet(() -> rootLog);
    if (path != null)
      createHandler(path.toString())
          .ifPresent(handler -> log(handler, new LogRecord(Level.INFO, STARTED)));
  }

  @Override
  public void terminated() {
    LOGGER.log(Level.FINE, "Terminated");

    map.clear();

    if (rootLog != null)
      createHandler(rootLog.toString())
          .ifPresent(
              handler ->
                  log(
                      handler,
                      new LogRecord(
                          Level.INFO,
                          (aborted + failed + skipped + successful)
                              + " tests executed, "
                              + aborted
                              + " aborted, "
                              + skipped
                              + " skipper, "
                              + failed
                              + " failed")));

    aborted = 0;
    failed = 0;
    rootLog = null;
    rootPath = null;
    skipped = 0;
    successful = 0;
  }

  private static void createDirectories(
      Map<? super Node, ? super Path> map, Iterable<? extends Node> nodes, Path rootPath) {
    for (var node : nodes) {
      var name = rootPath.resolve(node.getName());

      if (Files.exists(name)) LOGGER.log(Level.SEVERE, DIRECTORY_EXISTS, name);
      else createDirectory(map, name, node, rootPath);
    }
  }

  private static void createDirectory(
      Map<? super Node, ? super Path> map, Path name, Node node, Path rootPath) {
    try {
      Files.createDirectories(name);

      createLog(name.resolve(LOG), map, node, rootPath);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, String.format(DIRECTORY_ERROR, name), e);
    }
  }

  private static Optional<Handler> createHandler(String path) {
    Handler handler = null;

    try {
      handler = new FileHandler(path, true);
    } catch (IOException e) {
      LOGGER.log(
          Level.SEVERE,
          String.format("The file %s could not be written to. The logs will not be saved.", path),
          e);
    }

    return Optional.ofNullable(handler);
  }

  private static void createLog(
      Path filename, Map<? super Node, ? super Path> map, Node node, Path rootPath) {
    try {
      map.put(node, Files.createFile(filename));

      createDirectories(map, node.getNodes(), rootPath);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, String.format(FILE_ERROR, filename), e);
    }
  }

  private static Path createRootLog(Path filename) {
    Path createdLog = null;

    try {
      createdLog = Files.createFile(filename);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, String.format(FILE_ERROR, filename), e);
    }

    return createdLog;
  }

  private static void log(Handler handler, LogRecord logRecord) {
    handler.setFormatter(new SimpleFormatter());

    LOGGER.addHandler(handler);
    LOGGER.setUseParentHandlers(false);
    LOGGER.log(logRecord);

    handler.close();
    LOGGER.removeHandler(handler);
  }
}
