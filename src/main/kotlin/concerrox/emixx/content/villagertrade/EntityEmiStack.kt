package concerrox.emixx.content.villagertrade

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.Minecraft
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix4f
import org.joml.Quaternionf
import kotlin.math.atan


class EntityEmiStack(private val entity: Entity) : EmiStack() {

    override fun isEmpty() = false
    override fun copy() = EntityEmiStack(entity)
    override fun getComponentChanges(): DataComponentPatch = DataComponentPatch.EMPTY
    override fun getKey() = entity
    override fun getId() = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
    override fun getTooltipText() = mutableListOf(name)
    override fun getName(): Component = entity.name

    override fun render(draw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {
        if (entity is LivingEntity) {
            println("DAMN")
            renderentity(draw, x, y, 1F, 0F, 0F, entity)
        }
    }

    private fun renderentity(
        guiGraphics: GuiGraphics, x: Int, y: Int, scale: Float, mouseX: Float, mouseY: Float, entity: LivingEntity
    ) {
//        var x = x
//        var y = y
//        var scale = scale
//        guiGraphics.pose().pushPose()
//        guiGraphics.pose().translate(x.toFloat(), y.toFloat(), 50F)
//        guiGraphics.pose().scale(-scale, scale, scale)
//        guiGraphics.flush()
//        val mobPoseStack = guiGraphics.pose()
//        mobPoseStack.pushPose()
//        mobPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0f))
//        val renderInfo: IMobRenderHook.RenderInfo =
//            MobRegistryImpl.applyRenderHooks(mobPoseStack, entity, RenderInfo(x, y, scale, yaw, pitch))
//        x = renderInfo.x
//        y = renderInfo.y
//        scale = renderInfo.scale
//        var yaw = renderInfo.yaw
//        var pitch = renderInfo.pitch
//        mobPoseStack.mulPose(Axis.XN.rotationDegrees((atan((pitch / 40.0f)).toFloat()) * 20.0f))
//        entity.yo = atan(yaw / 40.0f).toFloat() * 20.0
//        val yRot = atan(yaw / 40.0f).toFloat() * 40.0f
//        val xRot = -(atan(pitch / 40.0f).toFloat()) * 20.0f
//        entity.setYRot(yRot)
//        entity.setYRot(yRot)
//        entity.setXRot(xRot)
//        entity.yHeadRot = yRot
//        entity.yHeadRotO = yRot
//        mobPoseStack.translate(0.0f, entity.y.toFloat(), 0.0f)
//        val entityRenderDispatcher = net.minecraft.client.Minecraft.getInstance().entityRenderDispatcher
//        entityRenderDispatcher.setRenderShadow(false)
//        RenderSystem.runAsFancy {
//            entityRenderDispatcher.render(
//                entity,
//                0.0,
//                0.0,
//                0.0,
//                0F,
//                1.0f,
//                mobPoseStack,
//                guiGraphics.bufferSource(),
//                15728880
//            )
//        }
//        mobPoseStack.popPose()
//        guiGraphics.flush()
//        entityRenderDispatcher.setRenderShadow(true)
//        guiGraphics.pose().popPose()

        val mouseX0 = guiGraphics.guiWidth() + 51 - mouseX
        val mouseY0 = guiGraphics.guiHeight() + 75 - 50 - mouseY
        val f = atan(mouseX0 / 40F)
        val g = atan(mouseY0 / 40F)
        val quaternion = Quaternionf().rotateZ(Mth.PI)
        val quaternion2 = Quaternionf().rotateX(g * 20F * 0.017453292F)
        quaternion.mul(quaternion2)
        val h = entity.yBodyRot
        val i = entity.yRot
        val j = entity.xRot
        val k = entity.yHeadRotO
        val l = entity.yHeadRot
        entity.yBodyRot = 180.0f + f * 20.0f
        entity.yBodyRot = 180.0f + f * 40.0f
        entity.xRot = -g * 20.0f
        entity.yHeadRot = entity.yRot
        entity.yHeadRotO = entity.yRot
        draw(guiGraphics, x, y, scale, quaternion, quaternion2, entity)
        entity.yBodyRot = h
        entity.yRot = i
        entity.xRot = j
        entity.yHeadRotO = k
        entity.yHeadRot = l
    }

    private fun draw(
        guiGraphics: GuiGraphics,
        x: Int,
        y: Int,
        size: Float,
        quaternion: Quaternionf,
        quaternion2: Quaternionf,
        entity: Entity
    ) {
        guiGraphics.pose().pushPose()
//        guiGraphics.pose().translate(x + 8.0, y + 16.0, 50.0)
//        guiGraphics.pose().mulPose(Matrix4f().scaling(size, size, -size))
//        guiGraphics.pose().mulPose(quaternion)
//        Lighting.setupForEntityInInventory()
        val dispatcher = Minecraft.entityRenderDispatcher
        quaternion2.conjugate()
        dispatcher.overrideCameraOrientation(quaternion2)
        dispatcher.setRenderShadow(false)
        val bufferSource = net.minecraft.client.Minecraft.getInstance().renderBuffers().bufferSource()
        RenderSystem.runAsFancy {
            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, guiGraphics.pose(), bufferSource, 15728880)
        }
        bufferSource.endBatch()
//        ctx.flush()
        dispatcher.setRenderShadow(true)
        guiGraphics.pose().popPose()
//        Lighting.setupFor3DItems()
    }

}