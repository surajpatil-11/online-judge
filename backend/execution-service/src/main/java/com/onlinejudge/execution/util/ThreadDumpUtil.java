package com.onlinejudge.execution.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class ThreadDumpUtil {
    public static String getThreadDump() {
        StringBuilder dump = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        
        dump.append("Thread Dump:\n");
        for (ThreadInfo info : threadInfos) {
            dump.append(String.format("\nThread %s (ID: %d, State: %s)\n", 
                info.getThreadName(), 
                info.getThreadId(), 
                info.getThreadState()));
            
            StackTraceElement[] stackTrace = info.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                dump.append("\tat ").append(element).append("\n");
            }
        }
        return dump.toString();
    }
}