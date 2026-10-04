package com.ji.afkcinematic.mixin;

import com.ji.afkcinematic.diagnostic.MixinState;
import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class JiAFKCinematicMixinPlugin
implements IMixinConfigPlugin {
    private static final String VANILLA_CULLING_MIXIN = "com.ji.afkcinematic.mixin.WorldRendererCullingMixin";

    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!VANILLA_CULLING_MIXIN.equals(mixinClassName)) return true;
        // Mixin preparation happens before the runtime ModList is constructed.
        return LoadingModList.get().getModFileById("sodium") == null
            && LoadingModList.get().getModFileById("embeddium") == null;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        MixinState.markApplied(mixinClassName);
    }

    public List<String> getMixins() {
        return null;
    }
}
