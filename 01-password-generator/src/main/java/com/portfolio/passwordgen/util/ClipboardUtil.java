package com.portfolio.passwordgen.util;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Utility for interacting with the operating system clipboard.
 */
public final class ClipboardUtil {

    private ClipboardUtil() {
        // Prevent instantiation
    }

    /**
     * Copies plain text to the system clipboard.
     *
     * @param text the content to copy
     * @return true if successfully copied, false if clipboard is unavailable (e.g. headless environment)
     */
    public static boolean copyToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            return true;
        } catch (HeadlessException | SecurityException e) {
            // In headless servers or strict sandboxes, clipboard access is gracefully rejected
            return false;
        }
    }
}
