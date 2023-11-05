package com.elijah629.fachud;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.elijah629.fachud.FacHudConfig.HudLocation;
import com.mojang.blaze3d.systems.RenderSystem;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.Biome;

public class FacHudClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AutoConfig.register(FacHudConfig.class, GsonConfigSerializer::new);
		
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		MinecraftClient client = MinecraftClient.getInstance();
		Language lang = Language.getInstance();
		
		final int COORD_Y = 5;
		final int COORD_P = 5;
		final int COORD_W = 145;
		final int COORD_H = 50;
		
		final int FPS_Y = 60;
		final int FPS_P = 5;
		final int FPS_W = 45;
		final int FPS_H = 18;
		
		final int BACKGROUND = 0xAA1D1D1D;
		final int TEXT = 0xFFFFFF;
		
		HudRenderCallback.EVENT.register((e, f) -> {
			if (client.getDebugHud().shouldShowDebugHud()) {
				return;
			}
			
			final FacHudConfig config = AutoConfig.getConfigHolder(FacHudConfig.class).getConfig();
			final TextRenderer renderer = client.textRenderer;
			final Window window = client.getWindow();

			final int COORD_X = config.HUDLocation == HudLocation.Left ? 5 : window.getScaledWidth() - COORD_W - 5;
			final int FPS_X = config.HUDLocation == HudLocation.Left ? 5 : window.getScaledWidth() - FPS_W - 5;
			
			final int COORD_SX = COORD_X + COORD_P;
			final int COORD_SY = COORD_Y + COORD_P;
			final int FPS_SX = FPS_X + FPS_P;
			final int FPS_SY = FPS_Y + FPS_P;
			
			builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
			
			fillRectangle(e, builder, COORD_X, COORD_Y, COORD_W + COORD_X, COORD_H + COORD_Y, BACKGROUND);
			fillRectangle(e, builder, FPS_X, FPS_Y, FPS_W + FPS_X, FPS_H + FPS_Y, BACKGROUND);
			
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			BufferRenderer.drawWithGlobalProgram(builder.end());
			RenderSystem.disableBlend();

			final int X = (int) client.player.getX();
			final int Y = (int) client.player.getY();
			final int Z = (int) client.player.getZ();
			final int FPS = client.getCurrentFps();

			final int Yaw = Math.abs((int) client.player.getYaw()) % 360;
			final int Pitch = (int) client.player.getPitch();

			final Direction direction = client.player.getHorizontalFacing();

			drawTextLines(e, renderer,
					directionToText(direction) + "\n"
							+ rotationToHalfPrecisionCardinalString(Yaw) + (config.ShowAngles ? "\n" + Pitch + "°\n" + Yaw + "°" : ""),
					COORD_X + COORD_W - COORD_P - 5 * 4, COORD_SY, TEXT, client.textRenderer.fontHeight + 1, true);


			final RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
			final String biome_name = lang.get("biome."	+ biome.getKey().get().getValue().toTranslationKey());

			drawTextLines(e, renderer,
					"X: " + X + "\n" +
							"Y: " + Y + "\n" +
							"Z: " + Z + "\n" +
							"Biome: " + biome_name,
					COORD_SX, COORD_SY, TEXT, client.textRenderer.fontHeight + 1, true);

			e.drawText(renderer, Text.of(
					FPS + " FPS"), FPS_SX, FPS_SY, TEXT, true);
		});
	}

	private static String rotationToHalfPrecisionCardinalString(int rotYaw) {
		int facing = rotYaw / 45;

		final String[] directions = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };

		return directions[facing];

	}

	private static String directionToText(Direction direction) {
		switch (direction) {
			case NORTH:
				return "-Z";
			case SOUTH:
				return "Z";
			case WEST:
				return "-X";
			case EAST:
				return "X";
			default:
				return "Invalid";
		}
	}

	private static void drawTextLines(DrawContext drawContext, TextRenderer textRenderer, @Nullable String text, int x,
			int y, int color, int line_height,
			boolean shadow) {
		String[] lines = text.split("\n");

		for (int line = 0; line < lines.length; line++) {
			drawContext.drawText(textRenderer, lines[line], x, y + (line * line_height), color, shadow);
		}
	}

	private static void fillRectangle(DrawContext context, BufferBuilder builder, int x1, int y1, int x2, int y2,
			int color) {
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		float a = (float) (color >> 24 & 255) / 255F;
		float r = (float) (color >> 16 & 255) / 255F;
		float g = (float) (color >> 8 & 255) / 255F;
		float b = (float) (color & 255) / 255F;

		builder.vertex(matrix, (float) x1, (float) y2, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix, (float) x2, (float) y2, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix, (float) x2, (float) y1, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix, (float) x1, (float) y1, 0.0F).color(r, g, b, a).next();
	}

}