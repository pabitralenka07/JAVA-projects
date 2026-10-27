package com.portfolio.examseating;

import com.portfolio.examseating.cli.ExamSeatingCli;

/**
 * Main application launcher for Exam Seating Optimization Engine.
 */
public class Main {

    public static void main(String[] args) {
        ExamSeatingCli cli = new ExamSeatingCli();
        boolean isDemo = args.length == 0 || "--demo".equalsIgnoreCase(args[0]) || "-d".equalsIgnoreCase(args[0]);

        if (isDemo) {
            cli.runDemo();
        } else {
            cli.startInteractiveLoop();
        }
    }
}
