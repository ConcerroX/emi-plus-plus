package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import net.minecraft.resources.ResourceLocation

abstract class StackGroup(val id: ResourceLocation) {

    internal var isEnabled = true

    abstract fun match(stack: EmiIngredient): Boolean

//    enum class Order(val value: String) : StringRepresentable {
//        BEFORE("before"), AFTER("after"), REPLACE("replace");
//
//        override fun getSerializedName() = value
//
//        companion object {
//            @Deprecated("It's deprecated, but I don't know how to replace it")
//            val CODEC: StringRepresentable.EnumCodec<Order> = StringRepresentable.fromEnum(::values)
//        }
//    }

//    enum class Type(val value: String) : StringRepresentable {
//        ITEM(Registries.ITEM.location().toString());
//
//        override fun getSerializedName() = value
//
//        companion object {
//            @Deprecated("It's deprecated, but I don't know how to replace it")
//            val CODEC: StringRepresentable.EnumCodec<Type> = StringRepresentable.fromEnum(::values)
//        }
//    }

    companion object {
//        val CODEC: Codec<ItemGroup2> = RecordCodecBuilder.create {
//            it.group(
//                Codec.STRING.fieldOf("id").forGetter(ItemGroup2::id)
//            ).apply(it, ::ItemGroup2)
//        }
    }

}