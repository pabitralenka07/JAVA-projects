package com.portfolio.isp.model;

public enum PlanTier {
    BASIC("Basic Starter", "Affordable entry-level broadband for light browsing"),
    STANDARD("Standard Fiber", "High-speed family connection for streaming & work"),
    ULTRA_FIBER("Ultra Fiber Pro", "Ultra-fast low-latency fiber for 4K streaming & gaming"),
    GIGABIT_ENTERPRISE("Gigabit Enterprise", "Dedicated symmetrical gigabit connectivity with SLA");

    private final String displayName;
    private final String description;

    PlanTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
