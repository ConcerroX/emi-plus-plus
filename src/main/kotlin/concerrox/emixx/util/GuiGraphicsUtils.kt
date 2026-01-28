package concerrox.emixx.util

import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import concerrox.emixx.Minecraft
import concerrox.emixx.id
import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.Util
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Deprecated("")
internal inline fun GuiGraphics.with(action: PoseStack.() -> Unit) {
    pose().pushPose()
//    action(pose)
    pose().popPose()
}

@Deprecated("")
internal fun GuiGraphics.renderScrollingString(
    font: Font,
    text: Component,
    minX: Int,
    minY: Int,
    maxX: Int,
    maxY: Int,
    color: Int,
    hasShadow: Boolean
) {
    renderScrollingString(this, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color, hasShadow)
}

@Deprecated("")
private fun renderScrollingString(
    guiGraphics: GuiGraphics,
    font: Font,
    text: Component,
    centerX: Int,
    minX: Int,
    minY: Int,
    maxX: Int,
    maxY: Int,
    color: Int,
    hasShadow: Boolean
) {
    val i = font.width(text)
    val var10000 = minY + maxY
    val j = (var10000 - 9) / 2 + 1
    val k = maxX - minX
    if (i > k) {
        val l = i - k
        val d = Util.getMillis().toDouble() / 1000.0
        val e = max(l.toDouble() * 0.5, 3.0)
        val f = sin((Math.PI / 2.0) * cos((Math.PI * 2.0) * d / e)) / 2.0 + 0.5
        val g = Mth.lerp(f, 0.0, l.toDouble())
        guiGraphics.enableScissor(minX, minY, maxX, maxY)
        guiGraphics.drawString(font, text, minX - g.toInt(), j, color, hasShadow)
        guiGraphics.disableScissor()
    } else {
        val l = Mth.clamp(centerX, minX + i / 2, maxX - i / 2)
        guiGraphics.drawCenteredString(font, text, l, j, color, hasShadow)
    }
}

@Deprecated("")
internal fun GuiGraphics.drawCenteredString(
    font: Font, text: Component, x: Int, y: Int, color: Int, hasShadow: Boolean
) {
    val visualText = text.visualOrderText
    drawString(font, visualText, x - font.width(visualText) / 2, y, color, hasShadow)
}

@Deprecated("")
internal fun GuiGraphics.blitOreUi(x: Int, y: Int, width: Int, height: Int, u: Int, v: Int, uWidth: Int, vHeight: Int) {
    blit(id("textures/gui/oreui.png"), x, y, width, height, u.toFloat(), v.toFloat(), uWidth, vHeight, 256, 256)
}

//internal fun GuiGraphics.blitOreUi(x: Int, y: Int, width: Int, height: Int, u: Int, v: Int, uWidth: Int, vHeight: Int) {
//    blitSprite(res("textures/gui/oreui.png"), x, y, width, height, u.toFloat(), v.toFloat(), uWidth, vHeight, 256, 256)
//}

@Deprecated("")
internal fun GuiGraphics.fastBlitNineSlicedSprite(
    spriteLocation: ResourceLocation, x: Int, y: Int, width: Int, height: Int
) {
//    val sprite = sprites.getSprite(spriteLocation)
//    val nineSlice = sprites.getSpriteScaling(sprite) as GuiSpriteScaling.NineSlice
//    val border = nineSlice.border()
//    val left = min(border.left(), width / 2)
//    val right = min(border.right(), width / 2)
//    val top = min(border.top(), height / 2)
//    val bottom = min(border.bottom(), height / 2)
//    val blitOffset = 0
////    if (width == nineSlice.width() && height == nineSlice.height()) {
////        blitSprite(
////            sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, height
////        )
////    } else if (height == nineSlice.height()) {
////        blitSprite(
////            sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, i, height
////        )
////        blitTiledSprite(
////            sprite,
////            x + i,
////            y,
////            blitOffset,
////            width - j - i,
////            height,
////            i,
////            0,
////            nineSlice.width() - j - i,
////            nineSlice.height(),
////            nineSlice.width(),
////            nineSlice.height()
////        )
////        blitSprite(
////            sprite,
////            nineSlice.width(),
////            nineSlice.height(),
////            nineSlice.width() - j,
////            0,
////            x + width - j,
////            y,
////            blitOffset,
////            j,
////            height
////        )
////    } else if (width == nineSlice.width()) {
////        blitSprite(
////            sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, width, k
////        )
////        blitTiledSprite(
////            sprite,
////            x,
////            y + k,
////            blitOffset,
////            width,
////            height - l - k,
////            0,
////            k,
////            nineSlice.width(),
////            nineSlice.height() - l - k,
////            nineSlice.width(),
////            nineSlice.height()
////        )
////        blitSprite(
////            sprite,
////            nineSlice.width(),
////            nineSlice.height(),
////            0,
////            nineSlice.height() - l,
////            x,
////            y + height - l,
////            blitOffset,
////            width,
////            l
////        )
////    } else {
//    blitSprite(
//        sprite, nineSlice.width(), nineSlice.height(), 0, 0, x, y, blitOffset, left, top
//    )
//    blitSpriteStretched(
//        sprite,
//        x + left,
//        y,
//        blitOffset,
//        width - right - left,
//        top,
//        left,
//        0,
//        nineSlice.width() - right - left,
//        top,
//        nineSlice.width(),
//        nineSlice.height()
//    )
//    blitSprite(
//        sprite,
//        nineSlice.width(),
//        nineSlice.height(),
//        nineSlice.width() - right,
//        0,
//        x + width - right,
//        y,
//        blitOffset,
//        right,
//        top
//    )
//    blitSprite(
//        sprite,
//        nineSlice.width(),
//        nineSlice.height(),
//        0,
//        nineSlice.height() - bottom,
//        x,
//        y + height - bottom,
//        blitOffset,
//        left,
//        bottom
//    )
//    blitSpriteStretched(
//        sprite,
//        x + left,
//        y + height - bottom,
//        blitOffset,
//        width - right - left,
//        bottom,
//        left,
//        nineSlice.height() - bottom,
//        nineSlice.width() - right - left,
//        bottom,
//        nineSlice.width(),
//        nineSlice.height()
//    )
//    blitSprite(
//        sprite,
//        nineSlice.width(),
//        nineSlice.height(),
//        nineSlice.width() - right,
//        nineSlice.height() - bottom,
//        x + width - right,
//        y + height - bottom,
//        blitOffset,
//        right,
//        bottom
//    )
//    blitSpriteStretched(
//        sprite,
//        x,
//        y + top,
//        blitOffset,
//        left,
//        height - bottom - top,
//        0,
//        top,
//        left,
//        nineSlice.height() - bottom - top,
//        nineSlice.width(),
//        nineSlice.height()
//    )
//    blitSpriteStretched(
//        sprite,
//        x + left,
//        y + top,
//        blitOffset,
//        width - right - left,
//        height - bottom - top,
//        left,
//        top,
//        nineSlice.width() - right - left,
//        nineSlice.height() - bottom - top,
//        nineSlice.width(),
//        nineSlice.height()
//    )
//    blitSpriteStretched(
//        sprite,
//        x + width - right,
//        y + top,
//        blitOffset,
//        right,
//        height - bottom - top,
//        nineSlice.width() - right,
//        top,
//        right,
//        nineSlice.height() - bottom - top,
//        nineSlice.width(),
//        nineSlice.height()
//    )
//    }
}

@Deprecated("")
private fun GuiGraphics.blitSpriteStretched(
    sprite: TextureAtlasSprite,
    x: Int,
    y: Int,
    blitOffset: Int,
    width: Int,
    height: Int,
    uPosition: Int,
    vPosition: Int,
    uWidth: Int,
    vHeight: Int,
    textureWidth: Int,
    textureHeight: Int
) {
//    innerBlit(
//        sprite.atlasLocation(),
//        x,
//        x + width,
//        y,
//        y + height,
//        blitOffset,
//        sprite.getU(uPosition.toFloat() / textureWidth.toFloat()),
//        sprite.getU((uPosition + uWidth).toFloat() / textureWidth.toFloat()),
//        sprite.getV(vPosition.toFloat() / textureHeight.toFloat()),
//        sprite.getV((vPosition + vHeight).toFloat() / textureHeight.toFloat())
//    )
}

@Deprecated("")
internal fun GuiGraphics.renderItem(
    stack: ItemStack?, x: Float, y: Float, size: Float = 16F
) {
    if (stack == null || stack.isEmpty) return
    val bakedModel = Minecraft.itemRenderer.getModel(stack, Minecraft.level, null, 0)
    pose.pushPose()
    pose.translate(x + size / 2F, y + size / 2F, 150F)
    try {
        pose.scale(size, -size, size)
        val useBlockLight = !bakedModel.usesBlockLight()
        if (useBlockLight) Lighting.setupForFlatItems()
        Minecraft.itemRenderer.render(
            stack, ItemDisplayContext.GUI, false, pose, bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel
        )
        flush()
        if (useBlockLight) Lighting.setupFor3DItems()
    } catch (throwable: Throwable) {
        val report = CrashReport.forThrowable(throwable, "Rendering item")
        report.addCategory("Item being rendered").apply {
            setDetail("Item Type") { stack.item.toString() }
            setDetail("Item Components") { stack.components.toString() }
            setDetail("Item Foil") { stack.hasFoil().toString() }
        }
        throw ReportedException(report)
    }
    pose.popPose()
}

@Deprecated("")
object GuiGraphicsUtils {

    @Deprecated("")
    fun renderItem(
        guiGraphics: GuiGraphics, stack: ItemStack?, x: Float, y: Float, size: Float = 16F
    ) {
        if (stack == null || stack.isEmpty) return
        val minecraft = Minecraft
        val bakedModel = minecraft.itemRenderer.getModel(stack, minecraft.level, null, 0)
        guiGraphics.pose.pushPose()
        guiGraphics.pose.translate(x + size / 2F, y + size / 2F, 150F)
        try {
            guiGraphics.pose.scale(size, -size, size)
            val bl = !bakedModel.usesBlockLight()
            if (bl) {
                Lighting.setupForFlatItems()
            }
            minecraft.itemRenderer.render(
                stack,
                ItemDisplayContext.GUI,
                false,
                guiGraphics.pose,
                guiGraphics.bufferSource(),
                15728880,
                OverlayTexture.NO_OVERLAY,
                bakedModel
            )
            guiGraphics.flush()
            if (bl) {
                Lighting.setupFor3DItems()
            }
        } catch (throwable: Throwable) {
            val crashReport = CrashReport.forThrowable(throwable, "Rendering item")
            crashReport.addCategory("Item being rendered").apply {
                setDetail("Item Type") { stack.item.toString() }
                setDetail("Item Components") { stack.components.toString() }
                setDetail("Item Foil") { stack.hasFoil().toString() }
            }
            throw ReportedException(crashReport)
        }
        guiGraphics.pose.popPose()

    }

}