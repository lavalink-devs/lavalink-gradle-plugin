package dev.arbjerg.lavalink.example;

import dev.arbjerg.lavalink.api.ISocketContext;
import dev.arbjerg.lavalink.api.PluginEventHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Plugin extends PluginEventHandler {

    private static final Logger log = LoggerFactory.getLogger(Plugin.class);

    public Plugin() {
        log.info("Hello, world!");
    }

    @Override
    public void onWebSocketOpen(@NotNull ISocketContext context, boolean resumed) {
        log.info("Websocket opened!");
    }
}
