package com.step.pdf.demo.screen.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @description: 类描述
 * @author: fesine
 * @createTime:2021/8/24
 * @update:修改内容
 * @author: fesine
 * @updateTime:2021/8/24
 */
public class CommandExecUtil {
    /**
     * @param commandLine 需要执行的命令行，例如："AcroRd32.exe /p /h c://file.pdf"
     */
    public static int exec(String commandLine) throws Exception {
        CommandLine cmdLine = CommandLine.parse(commandLine);
        DefaultExecutor executor = new DefaultExecutor();
        int exitValue = executor.execute(cmdLine);
        return exitValue;
    }

    /**
     * @param command 命令，例如：AcroRd32.exe
     * @param args    参数，例如：/C , /p, /h
     */
    public static int exec(String command, String[] args, Map<String, ?> substitutionMap) throws Exception {
        CommandLine cmdLine = new CommandLine(command);
        if (args != null && args.length > 0) {
            for (String arg : args) {
                cmdLine.addArgument(arg);
            }
        }
        if (substitutionMap != null) {
            cmdLine.setSubstitutionMap(substitutionMap);
        }
        DefaultExecutor executor = new DefaultExecutor();
        // executor.setExitValue(1);
        // ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        // executor.setWatchdog(watchdog);
        int exitValue = executor.execute(cmdLine);

        return exitValue;
    }
    /**
     * @param command 命令，例如：AcroRd32.exe
     * @param args    参数，例如：/C , /p, /h
     */
    public static String execHasResult(String command, String[] args, Map<String, ?> substitutionMap) throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        PumpStreamHandler psh = new PumpStreamHandler(stdout);
        CommandLine cmdLine = new CommandLine(command);
        if (args != null && args.length > 0) {
            for (String arg : args) {
                cmdLine.addArgument(arg);
            }
        }
        if (substitutionMap != null) {
            cmdLine.setSubstitutionMap(substitutionMap);
        }
        DefaultExecutor executor = new DefaultExecutor();
        executor.execute(cmdLine);
        executor.setStreamHandler(psh);
        return stdout.toString();
    }
}
