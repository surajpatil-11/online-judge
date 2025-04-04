package com.onlinejudge.execution.service;

import org.springframework.stereotype.Service;
import com.onlinejudge.execution.model.CodeExecutionResponse;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.security.Permission;

@Service
public class JavaExecutorService {
    private static final long EXECUTION_TIMEOUT_MS = 1000;
    private static final int MAX_OUTPUT_SIZE = 1024;

    public CodeExecutionResponse executeCode(String sourceCode) {
        ThreadGroup threadGroup = new ThreadGroup("CodeExecution");
        try {
            // Create a temporary file for the Java class
            String className = "Main";  // Changed to Main
            String fullCode = sourceCode;  // Use the complete implementation

            // Create a custom class loader
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(".").toURI().toURL()});

            // Compile the code
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            
            JavaFileObject file = new SimpleJavaFileObject(
                URI.create("string:///" + className + ".java"), JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return fullCode;
                }
            };

            // Set compilation options
            List<String> options = Arrays.asList("-d", ".");
            JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, Arrays.asList(file));

            boolean success = task.call();
            if (!success) {
                StringBuilder errorMsg = new StringBuilder();
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    errorMsg.append(diagnostic.getMessage(null)).append("\n");
                }
                return new CodeExecutionResponse(null, errorMsg.toString(), false);
            }

            // Execute the compiled code with timeout
            try {
                // Create a separate process to run the code
                ProcessBuilder processBuilder = new ProcessBuilder("java", className);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // Read the output in a separate thread
                StringBuilder output = new StringBuilder();
                Thread outputThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        // Ignore interruption
                    }
                });
                outputThread.start();

                // Wait for completion or timeout
                boolean completed = process.waitFor(EXECUTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    outputThread.interrupt();
                    return new CodeExecutionResponse(null, "Time Limit Exceeded (1 second)", false);
                }

                // Get the output
                String result = output.toString();
                if (result.length() > MAX_OUTPUT_SIZE) {
                    result = result.substring(0, MAX_OUTPUT_SIZE) + "\n... Output truncated ...";
                }
                return new CodeExecutionResponse(result, null, true);
            } finally {
                new File(className + ".class").delete();
            }
        } catch (Exception e) {
            return new CodeExecutionResponse(null, e.getMessage(), false);
        }
    }
}