package com.finders.api.infra.discord.dto;

import java.time.Instant;
import java.util.List;

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
