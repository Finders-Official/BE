package com.finders.api.infra.discord.dto;

import java.time.Instant;
import java.util.List;

/**
 * Discord Embed 메시지 구조
 */
public record DiscordMessage(
        List<Embed> embeds
) {
    public DiscordMessage(Embed embed) {
        this(List.of(embed));
    }

    public record Embed(
            String title,
            String description,
            int color,
            List<Field> fields,
            Instant timestamp
    ) {
        public Embed {
            if (color == 0) {
                color = 15158332; // Default red color (#E74C3C)
            }
        }
    }

    public record Field(
            String name,
            String value,
            boolean inline
    ) {
        public Field(String name, String value) {
            this(name, value, false);
        }
    }
}
