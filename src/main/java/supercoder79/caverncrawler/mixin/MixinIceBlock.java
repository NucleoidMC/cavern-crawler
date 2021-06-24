package supercoder79.caverncrawler.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.IceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supercoder79.caverncrawler.CavernCrawler;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(IceBlock.class)
public class MixinIceBlock {
    @Inject(method = "melt", at = @At("HEAD"), cancellable = true)
    private void dontMelt(BlockState state, World world, BlockPos pos, CallbackInfo ci) {
        ManagedGameSpace game = ManagedGameSpace.forWorld(world);
        if (game != null && game.testRule(CavernCrawler.NO_ICE_MELT) == RuleResult.ALLOW) {
            ci.cancel();
        }
    }

    @Redirect(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private boolean dontSetWater(World world, BlockPos pos, BlockState state) {
        ManagedGameSpace game = ManagedGameSpace.forWorld(world);
        if (game != null && game.testRule(CavernCrawler.NO_ICE_MELT) == RuleResult.ALLOW) {
            return true;
        }

        return world.setBlockState(pos, state);
    }
}
