package com.github.sajmon.labyrythm.client.animation;

import com.github.sajmon.labyrythm.Labyrythm;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class AnimationLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, AnimationData> animations = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Load animations from a JSON resource file
     */
    public static void loadAnimations(ResourceLocation location) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
            if (resource.isEmpty()) {
                LOGGER.error("Could not find animation resource: {}", location);
                return;
            }

            try (InputStream is = resource.get().open();
                 Reader reader = new InputStreamReader(is)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                
                if (!json.has("animations") || !json.get("animations").isJsonObject()) {
                    LOGGER.error("Invalid animation format: missing 'animations' object");
                    return;
                }
                
                JsonObject animationsJson = json.getAsJsonObject("animations");
                
                for (Map.Entry<String, JsonElement> entry : animationsJson.entrySet()) {
                    String animationName = entry.getKey();
                    JsonObject animationJson = entry.getValue().getAsJsonObject();
                    
                    boolean loop = animationJson.has("loop") && animationJson.get("loop").getAsBoolean();
                    float length = animationJson.has("animation_length") ? 
                            animationJson.get("animation_length").getAsFloat() : 1.0f;
                    
                    AnimationData animation = new AnimationData(animationName, length, loop);
                    
                    if (animationJson.has("bones") && animationJson.get("bones").isJsonObject()) {
                        JsonObject bonesJson = animationJson.getAsJsonObject("bones");
                        
                        for (Map.Entry<String, JsonElement> boneEntry : bonesJson.entrySet()) {
                            String boneName = boneEntry.getKey();
                            JsonObject boneJson = boneEntry.getValue().getAsJsonObject();
                            
                            if (boneJson.has("rotation") && boneJson.get("rotation").isJsonObject()) {
                                JsonObject rotationJson = boneJson.getAsJsonObject("rotation");
                                
                                Map<Float, float[]> keyframes = new TreeMap<>();
                                
                                for (Map.Entry<String, JsonElement> keyframeEntry : rotationJson.entrySet()) {
                                    try {
                                        float time = Float.parseFloat(keyframeEntry.getKey());
                                        float normalizedTime = time / length;
                                        
                                        JsonElement valueElement = keyframeEntry.getValue();
                                        
                                        if (valueElement.isJsonArray()) {
                                            float[] values = new float[3];
                                            for (int i = 0; i < 3 && i < valueElement.getAsJsonArray().size(); i++) {
                                                values[i] = valueElement.getAsJsonArray().get(i).getAsFloat();
                                            }
                                            keyframes.put(normalizedTime, values);
                                        }
                                    } catch (NumberFormatException e) {
                                        LOGGER.warn("Invalid keyframe time: {}", keyframeEntry.getKey());
                                    }
                                }
                                
                                animation.addBoneRotation(boneName, keyframes);
                            }
                        }
                    }
                    
                    animations.put(animationName, animation);
                    LOGGER.info("Loaded animation: {} with {} bones", animationName, animation.getBones().size());
                }
                
                loaded = true;
                
            } catch (Exception e) {
                LOGGER.error("Error parsing animation JSON: {}", e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to load animations: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get interpolated rotation values for a bone at a specific animation progress
     */
    public static float[] getInterpolatedRotation(String animationName, String boneName, float progress) {
        if (!loaded) {
            loadAnimations(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "animations/minotaur.animation.json"));
        }
        
        AnimationData animation = animations.get(animationName);
        if (animation == null) {
            LOGGER.warn("Animation not found: {}", animationName);
            return new float[]{0, 0, 0};
        }
        
        return animation.getInterpolatedRotation(boneName, progress);
    }
    
    /**
     * Check if animation exists
     */
    public static boolean hasAnimation(String animationName) {
        if (!loaded) {
            loadAnimations(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "animations/minotaur.animation.json"));
        }
        return animations.containsKey(animationName);
    }
    
    /**
     * Get animation length
     */
    public static float getAnimationLength(String animationName) {
        if (!loaded) {
            loadAnimations(ResourceLocation.fromNamespaceAndPath(Labyrythm.MOD_ID, "animations/minotaur.animation.json"));
        }
        AnimationData animation = animations.get(animationName);
        return animation != null ? animation.getLength() : 1.0f;
    }
    
    /**
     * Reset loaded animations (for resource reloading)
     */
    public static void reset() {
        animations.clear();
        loaded = false;
    }
    
    /**
     * Represents a single animation with all its bones and keyframes
     */
    public static class AnimationData {
        private final String name;
        private final float length;
        private final boolean loop;
        private final Map<String, Map<Float, float[]>> boneRotations = new HashMap<>();
        
        public AnimationData(String name, float length, boolean loop) {
            this.name = name;
            this.length = length;
            this.loop = loop;
        }
        
        public void addBoneRotation(String boneName, Map<Float, float[]> keyframes) {
            boneRotations.put(boneName, keyframes);
        }
        
        public float[] getInterpolatedRotation(String boneName, float progress) {
            Map<Float, float[]> keyframes = boneRotations.get(boneName);
            if (keyframes == null || keyframes.isEmpty()) {
                return new float[]{0, 0, 0};
            }
            
            if (loop) {
                progress = progress % length;
            } else {
                progress = Mth.clamp(progress, 0, length);
            }
            
            float normalizedProgress = progress / length;
            
            Float lowerKey = null;
            Float upperKey = null;
            
            for (Float time : keyframes.keySet()) {
                if (time <= normalizedProgress && (lowerKey == null || time > lowerKey)) {
                    lowerKey = time;
                }
                if (time >= normalizedProgress && (upperKey == null || time < upperKey)) {
                    upperKey = time;
                }
            }
            
            if (lowerKey == null) {
                lowerKey = keyframes.keySet().stream().max(Float::compareTo).orElse(0f);
            }
            
            if (upperKey == null) {
                upperKey = keyframes.keySet().stream().min(Float::compareTo).orElse(0f);
            }
            
            if (lowerKey.equals(upperKey)) {
                return keyframes.get(lowerKey);
            }
            
            float[] lower = keyframes.get(lowerKey);
            float[] upper = keyframes.get(upperKey);
            float factor = (normalizedProgress - lowerKey) / (upperKey - lowerKey);
            
            return new float[]{
                Mth.lerp(factor, lower[0], upper[0]),
                Mth.lerp(factor, lower[1], upper[1]),
                Mth.lerp(factor, lower[2], upper[2])
            };
        }
        
        public String getName() {
            return name;
        }
        
        public float getLength() {
            return length;
        }
        
        public boolean isLoop() {
            return loop;
        }
        
        public Set<String> getBones() {
            return boneRotations.keySet();
        }
    }
}
