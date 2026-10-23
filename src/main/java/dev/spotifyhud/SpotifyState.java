
package dev.spotifyhud;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SpotifyState {
    private final Path directory;
    private final Path lyricsPath;
    private final List<LyricLine> lyrics = new ArrayList<>();

    private final long startMillis = System.currentTimeMillis();
    private long lastReload = 0;

    public SpotifyState(Path directory) {
        this.directory = directory;
        this.lyricsPath = directory.resolve("lyrics.lrc");

        try {
            Files.createDirectories(directory);
            reloadLyrics();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tick() {
        if (System.currentTimeMillis() - lastReload > 5000) {
            lastReload = System.currentTimeMillis();

            try {
                if (Files.exists(lyricsPath)) {
                    reloadLyrics();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String title() {
        return "Connect Spotify";
    }

    public String artist() {
        return "Local lyrics prototype";
    }

    public double progress() {
        return Math.min(
            1.0,
            (System.currentTimeMillis() - startMillis) / 210000.0
        );
    }

    public String currentLyric() {
        if (lyrics.isEmpty()) {
            return "Add lyrics.lrc in config/spotify-lyrics-hud";
        }

        long elapsed = System.currentTimeMillis() - startMillis;
        String current = "";

        for (LyricLine line : lyrics) {
            if (line.timeMillis <= elapsed) {
                current = line.text;
            } else {
                break;
            }
        }

        return current;
    }

    private void reloadLyrics() throws IOException {
        if (!Files.exists(lyricsPath)) {
            Files.createFile(lyricsPath);
            return;
        }

        List<LyricLine> parsed = new ArrayList<>();

        for (String line : Files.readAllLines(
                lyricsPath, StandardCharsets.UTF_8)) {
            if (!line.startsWith("[") || line.length() < 10) {
                continue;
            }

            int close = line.indexOf(']');

            if (close < 0) {
                continue;
            }

            try {
                String[] parts =
                    line.substring(1, close).split(":");

                long millis = (long) (
                    Double.parseDouble(parts[0]) * 60000
                    + Double.parseDouble(parts[1]) * 1000
                );

                parsed.add(new LyricLine(
                    millis, line.substring(close + 1).trim()
                ));
            } catch (RuntimeException ignored) {
                // Skip incorrectly formatted timestamps.
            }
        }

        parsed.sort(Comparator.comparingLong(a -> a.timeMillis));

        lyrics.clear();
        lyrics.addAll(parsed);
    }

    private record LyricLine(long timeMillis, String text) {}
}

