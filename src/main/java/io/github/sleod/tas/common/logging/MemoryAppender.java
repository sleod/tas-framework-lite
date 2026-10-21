package io.github.sleod.tas.common.logging;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(
        name = "Memory",
        category = Node.CATEGORY,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true
)
public final class MemoryAppender extends AbstractAppender {
    private final StringBuilder buffer = new StringBuilder();

    private MemoryAppender(String name, Layout<?> layout) {
        super(name, null, layout, true, null);
    }

    @PluginFactory
    public static MemoryAppender create(@PluginAttribute("name") String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        return new MemoryAppender(
                name,
                PatternLayout.newBuilder().setPattern("%d{ISO8601} [%t] %-6level: %msg%n%throwable").build()
        );
    }

    @Override
    public synchronized void append(LogEvent event) {
        buffer.append(new String(getLayout().toByteArray(event), StandardCharsets.UTF_8));
    }

    public synchronized String getAndClearLogs() {
        String logs = buffer.toString();
        buffer.setLength(0);
        return logs;
    }
}