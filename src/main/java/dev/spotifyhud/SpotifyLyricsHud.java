
package dev.spotifyhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SpotifyLyricsHud implements ClientModInitializer {
    private static SpotifyState state;

    @Override
    public void onInitializeClient() {
        state = new SpotifyState(
            MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config/spotify-lyrics-hud")
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> state.tick());

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
            renderHud(drawContext)
        );
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int right = screenWidth - 10;
        int top = 10;
        int width = Math.min(250, screenWidth - 20);
        int left = right - width;

        context.fill(left, top, right, top + 48, 0xC9101018);

        context.drawTextWithShadow(
            client.textRenderer,
            Text.literal("♫  " + state.title()),
            left + 8, top + 7, 0xFFFFFFFF
        );

        context.drawTextWithShadow(
            client.textRenderer,
            Text.literal(state.artist()),
            left + 8, top + 22, 0xFFB8B8D0
        );

        context.fill(left + 8, top + 38, right - 8, top + 40, 0xFF444455);

        int barWidth = right - left - 16;
        int progressWidth = (int) (barWidth * state.progress());

        context.fill(
            left + 8, top + 38,
            left + 8 + progressWidth, top + 40,
            0xFFB58CFF
        );

        String lyric = state.currentLyric();

        if (!lyric.isBlank()) {
            int lyricWidth = client.textRenderer.getWidth(lyric);
            int x = (screenWidth - lyricWidth) / 2;
            int y = screenHeight / 2 + 38;

            context.fill(
                x - 10, y - 5,
                x + lyricWidth + 10, y + 15,
                0xB0101018
            );

            context.drawTextWithShadow(
                client.textRenderer,
                Text.literal(lyric),
                x, y, 0xFFFFFFFF
            );
        }
    }
}

