package io.githb.henryssondaniel.teacup.report.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.henryssondaniel.teacup.core.reporting.Reporter;
import io.github.henryssondaniel.teacup.core.testing.Node;
import io.github.henryssondaniel.teacup.core.testing.Result;
import io.github.henryssondaniel.teacup.core.testing.Status;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultReporterTest {
  private static final String FINISHED = "finished";
  private static final String INITIALIZED = "initialized";
  private static final String LOG = ".log";
  private static final String NAME = "name";
  private static final String REASON = "reason";
  private static final String SKIPPED = "skipped";
  private static final String TEST = "test";

  private final FileSystem fileSystem = mock(FileSystem.class);
  private final FileSystemProvider fileSystemProvider = mock(FileSystemProvider.class);
  private final LogRecord logRecord = mock(LogRecord.class);
  private final Node node = mock(Node.class);
  private final Path path = mock(Path.class);
  private final Result result = mock(Result.class);
  private final SeekableByteChannel seekableByteChannel = mock(SeekableByteChannel.class);

  @BeforeEach
  void beforeEach() {
    when(node.getName()).thenReturn(NAME);
  }

  @Test
  void finishedWhenAborted(@TempDir Path tempDir) {
    when(result.getStatus()).thenReturn(Status.ABORTED);

    Reporter reporter = new DefaultReporter(tempDir.resolve(FINISHED));
    reporter.initialize();
    reporter.finished(node, result);

    verify(node).getTimeFinished();
    verify(node).getTimeFinished();
    verify(result).getStatus();
    verify(result).getThrowable();
  }

  @Test
  void finishedWhenFailed(@TempDir Path tempDir) {
    when(result.getStatus()).thenReturn(Status.FAILED);

    Reporter reporter = new DefaultReporter(tempDir.resolve(FINISHED));
    reporter.initialize();
    reporter.finished(node, result);

    verify(node).getTimeFinished();
    verify(node).getTimeFinished();
    verify(result).getStatus();
    verify(result).getThrowable();
  }

  @Test
  void finishedWhenNoHandler(@TempDir Path tempDir) {
    var file = tempDir.resolve("folder").toFile();

    Reporter reporter = new DefaultReporter(file.toPath());
    reporter.initialize();

    var files = file.listFiles();
    if (files != null) for (var child : files) deleteFile(child);
    assertThat(file.delete()).isTrue();

    reporter.finished(node, result);

    verify(result).getStatus();
    verify(result, times(0)).getThrowable();
    verifyNoInteractions(node);
  }

  @Test
  void finishedWhenNoNodeSuccessFul() {
    new DefaultReporter().finished(node, result);

    verify(result).getStatus();
    verify(result, times(0)).getThrowable();
    verifyNoInteractions(node);
  }

  @Test
  void initializeWhenFileCanNotBeCreated() throws IOException {
    when(fileSystem.provider()).thenReturn(fileSystemProvider);
    when(path.resolve(LOG)).thenReturn(path);

    try (var system = path.getFileSystem()) {
      when(system).thenReturn(fileSystem);
    }

    var ioException = new IOException(TEST);
    doThrow(ioException).when(fileSystemProvider).checkAccess(path);

    try (var byteChannel = fileSystemProvider.newByteChannel(same(path), any(), any())) {
      when(byteChannel).thenThrow(ioException);
    }

    new DefaultReporter(path).initialize();

    verify(fileSystemProvider).checkAccess(path);
    verify(fileSystemProvider).createDirectory(path);
    verify(fileSystem, times(4)).provider();
    verify(path).resolve(LOG);
  }

  @Test
  void initializeWhenFolderCanNotBeCreated() throws IOException {
    when(fileSystem.provider()).thenReturn(fileSystemProvider);
    when(path.toAbsolutePath()).thenReturn(path);

    try (var system = path.getFileSystem()) {
      when(system).thenReturn(fileSystem);
    }

    var ioException = new IOException(TEST);
    doThrow(ioException).when(fileSystemProvider).checkAccess(path);
    doThrow(ioException).when(fileSystemProvider).createDirectory(path);

    new DefaultReporter(path).initialize();

    verify(fileSystemProvider).checkAccess(path);
    verify(fileSystemProvider).createDirectory(path);
    verify(fileSystem, times(3)).provider();
    verify(path, times(0)).resolve(LOG);
    verify(path).toAbsolutePath();
  }

  @Test
  void initializeWhenFolderExists(@TempDir Path tempDir) {
    new DefaultReporter(tempDir).initialize();
    assertThat(tempDir.toFile().list()).isEmpty();
  }

  @Test
  void initialized(@TempDir Path tempDir) {
    Reporter reporter = new DefaultReporter(tempDir.resolve(INITIALIZED));
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));

    verify(node).getName();
  }

  @Test
  void initializedWhenFileCanNotBeCreated() throws IOException {
    when(fileSystem.provider()).thenReturn(fileSystemProvider);
    when(path.resolve(anyString())).thenReturn(path);

    try (var system = path.getFileSystem()) {
      when(system).thenReturn(fileSystem);
    }

    var ioException = new IOException(TEST);
    doThrow(ioException).when(fileSystemProvider).checkAccess(path);

    try (var byteChannel = fileSystemProvider.newByteChannel(same(path), any(), any())) {
      when(byteChannel).thenReturn(seekableByteChannel).thenThrow(ioException);
    }

    Reporter reporter = new DefaultReporter(path);
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));

    verify(fileSystemProvider, times(2)).checkAccess(path);
    verify(fileSystemProvider, times(2)).createDirectory(path);
    verify(fileSystem, times(8)).provider();
    verify(path, times(2)).resolve(LOG);
  }

  @Test
  void initializedWhenFolderAlreadyExisting(@TempDir Path tempDir) throws IOException {
    var folder = tempDir.resolve(INITIALIZED);

    Reporter reporter = new DefaultReporter(folder);
    reporter.initialize();

    assertThat(folder.resolve(NAME).toFile().createNewFile()).isTrue();
    reporter.initialized(Collections.singletonList(node));

    verify(node).getName();
  }

  @Test
  void initializedWhenFolderCanNotBeCreated() throws IOException {
    when(fileSystem.provider()).thenReturn(fileSystemProvider);
    when(path.toAbsolutePath()).thenReturn(path);
    when(path.resolve(anyString())).thenReturn(path);

    try (var system = path.getFileSystem()) {
      when(system).thenReturn(fileSystem);
    }

    var ioException = new IOException(TEST);
    doNothing().doThrow(ioException).when(fileSystemProvider).createDirectory(path);
    doThrow(ioException).when(fileSystemProvider).checkAccess(path);

    try (var byteChannel = fileSystemProvider.newByteChannel(same(path), any(), any())) {
      when(byteChannel).thenReturn(seekableByteChannel);
    }

    Reporter reporter = new DefaultReporter(path);
    reporter.initialize();
    reporter.initialized(Collections.singletonList(node));

    verify(fileSystemProvider, times(2)).checkAccess(path);
    verify(fileSystemProvider, times(2)).createDirectory(path);
    verify(fileSystem, times(7)).provider();
    verify(path).resolve(LOG);
    verify(path).toAbsolutePath();
  }

  @Test
  void initializedWhenNoRoot() {
    new DefaultReporter().initialized(Collections.singletonList(node));
    verifyNoInteractions(node);
  }

  @Test
  void log(@TempDir Path tempDir) {
    when(logRecord.getInstant()).thenReturn(Instant.EPOCH);
    when(logRecord.getLevel()).thenReturn(Level.INFO);

    Reporter reporter = new DefaultReporter(tempDir.resolve("log"));
    reporter.initialize();
    reporter.log(logRecord, node);

    verify(logRecord).getInstant();
    verify(logRecord, times(4)).getLevel();
    verifyNoInteractions(node);
  }

  @Test
  void logWhenNoRoot() {
    new DefaultReporter().log(logRecord, node);

    verifyNoInteractions(logRecord);
    verifyNoInteractions(node);
  }

  @Test
  void skipped(@TempDir Path tempDir) {
    Reporter reporter = new DefaultReporter(tempDir.resolve(SKIPPED));
    reporter.initialize();
    reporter.skipped(node, REASON);

    verifyNoInteractions(node);
  }

  @Test
  void skippedWhenNoRoot() {
    new DefaultReporter().skipped(node, REASON);
    verifyNoInteractions(node);
  }

  @Test
  void started(@TempDir Path tempDir) {
    Reporter reporter = new DefaultReporter(tempDir.resolve(SKIPPED));
    reporter.initialize();
    reporter.started(node);

    verifyNoInteractions(node);
  }

  @Test
  void startedWhenNoRoot() {
    new DefaultReporter().started(node);
    verifyNoInteractions(node);
  }

  @Test
  void terminated(@TempDir Path tempDir) throws IllegalAccessException, NoSuchFieldException {
    Reporter reporter = new DefaultReporter(tempDir.resolve("terminated"));
    reporter.initialize();
    reporter.terminated();

    assertThat(getMap(reporter)).isEmpty();
  }

  @Test
  void terminatedWhenNoRoot() throws IllegalAccessException, NoSuchFieldException {
    Reporter reporter = new DefaultReporter();
    reporter.terminated();

    assertThat(getMap(reporter)).isEmpty();
  }

  private static void deleteFile(File child) {
    assertThat(child.delete()).isTrue();
  }

  @SuppressWarnings("unchecked")
  private static Map<Node, Path> getMap(Reporter reporter)
      throws IllegalAccessException, NoSuchFieldException {
    var field = DefaultReporter.class.getDeclaredField("map");
    field.setAccessible(true);

    return (Map<Node, Path>) field.get(reporter);
  }
}
