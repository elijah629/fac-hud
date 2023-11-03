package com.elijah629.fachud;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.world.biome.Biome;

public class FacHudClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		MinecraftClient client = MinecraftClient.getInstance();
		Language lang = Language.getInstance();

		HudRenderCallback.EVENT.register((e, f) -> {
			builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

			fillRectangle(e, builder, 5, 5, 150, 55, 0xAA1D1D1D);

			fillRectangle(e, builder, 5, 65, 50, 83, 0xAA1D1D1D);

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			BufferRenderer.drawWithGlobalProgram(builder.end());
			RenderSystem.disableBlend();

			TextRenderer renderer = client.textRenderer;

			int X = (int) client.player.getX();
			int Y = (int) client.player.getY();
			int Z = (int) client.player.getZ();

			int FPS = client.getCurrentFps();

			RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());

			String biome_name = lang.get("biome."
					+ biome.getKey().get().getValue().toTranslationKey());

			e.drawText(renderer,
					"X: " + X, 10, 10, 0xFFFFFF, true);
			e.drawText(renderer,
					"Y: " + Y, 10, 20, 0xFFFFFF, true);
			e.drawText(renderer,
					"Z: " + Z, 10, 30, 0xFFFFFF, true);

			e.drawText(renderer,
					"Biome: " + biome_name,
					10, 40, 0xFFFFFF, true);

			e.drawText(renderer, Text.of(
					FPS + " FPS"), 10, 70, 0xFFFFFF, true);
		});
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