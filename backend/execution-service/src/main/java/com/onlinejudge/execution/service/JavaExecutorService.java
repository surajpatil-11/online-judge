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

@Service
public class JavaExecutorService {

    private static final long EXECUTION_TIMEOUT_MS = 1000; // 1 second timeout

    public CodeExecutionResponse executeCode(String sourceCode) {
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
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            PrintStream oldOut = System.out;
            System.setOut(printStream);

            try {
                Future<Object> future = Executors.newSingleThreadExecutor().submit(() -> {
                    Class<?> dynamicClass = classLoader.loadClass(className);
                    // Call main method instead of execute
                    String[] args = new String[0];
                    dynamicClass.getMethod("main", String[].class).invoke(null, (Object) args);
                    return outputStream.toString();
                });

                try {
                    Object result = future.get(EXECUTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    String output = result != null ? result.toString() : "";
                    return new CodeExecutionResponse(output, null, true);
                } catch (TimeoutException e) {
                    future.cancel(true);
                    return new CodeExecutionResponse(null, "Execution timed out (exceeded 1 second)", false);
                }
            } finally {
                System.setOut(oldOut);
                classLoader.close();
                new File(className + ".class").delete();
            }
        } catch (Exception e) {
            return new CodeExecutionResponse(null, e.getMessage(), false);
        }
    }
}