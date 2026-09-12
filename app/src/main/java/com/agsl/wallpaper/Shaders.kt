package com.agsl.wallpaper

object Shaders {
    const val RENDER = """
uniform float2 uResolution;
uniform float uTime;
uniform float4 uPointer1; // xy: 坐标, z: 按下态, w: 滤波手速
uniform float4 uPointer2; // xy: 坐标, z: 按下态, w: 滤波手速

half4 main(float2 fragCoord) {
    float minRes = min(uResolution.x, uResolution.y);
    if (minRes <= 0.0) return half4(0.001, 0.002, 0.004, 1.0);
    float2 uv = (fragCoord - 0.5 * uResolution) / minRes;

    float2 p1 = (uPointer1.xy - 0.5 * uResolution) / minRes;
    float2 p2 = (uPointer2.xy - 0.5 * uResolution) / minRes;

    float displacement = 0.0;
    float tearEffect = 1.0;

    // 1. 触控点表面张力排挤与双指融合 (Fusion)
    if (uPointer1.z > 0.01) {
        float d1 = length(uv - p1);
        float r1 = 0.14 + uPointer1.w * 0.22;
        float w1 = smoothstep(r1, 0.0, d1) * uPointer1.z;
        displacement += w1 * (0.045 + uPointer1.w * 0.08);
    }

    if (uPointer2.z > 0.01) {
        float d2 = length(uv - p2);
        float r2 = 0.14 + uPointer2.w * 0.22;
        float w2 = smoothstep(r2, 0.0, d2) * uPointer2.z;
        displacement += w2 * (0.045 + uPointer2.w * 0.08);

        // 双指靠近时的张力平滑融合
        float midDist = length(p1 - p2);
        if (midDist < 0.55) {
            float fusion = smoothstep(0.0, 0.55, midDist);
            displacement *= mix(1.25, 1.0, fusion);
        }
    }

    // 2. 快速划动的油膜切割撕裂 (Slicing Groove)
    if (uPointer1.z > 0.01 && uPointer1.w > 0.18) {
        float2 velDir = normalize(p1 - (p1 - float2(0.01, 0.01))); // 简化速度切线
        float lineDist = abs(dot(uv - p1, float2(-velDir.y, velDir.x)));
        float d1 = length(uv - p1);
        tearEffect *= smoothstep(0.004, 0.025, lineDist + d1 * 0.15);
    }

    // 变形后 UV
    float2 warpedUV = uv - displacement;
    float t = uTime * 0.07;

    // 纳米级暗调油膜干涉纹理（纯冷色调：钛灰与深青，绝对无紫光）
    float q = sin(warpedUV.x * 13.0 + t) * cos(warpedUV.y * 13.0 - t)
            + cos(length(warpedUV * 1.4) * 16.0 - t * 1.2);
    
    float sheen = (q * 0.5 + 0.5) * tearEffect;

    half3 abyss = half3(0.001, 0.002, 0.004);
    half3 filmColor = half3(0.012, 0.035, 0.050) + half3(0.008, 0.020, 0.030) * sin(sheen * 6.28318 + t);

    half3 col = abyss + filmColor;

    // 极致柔和的黑阶压制
    col = half3(1.0) - exp(-col * 1.3);

    return half4(clamp(col, 0.0, 1.0), 1.0);
}
"""
}
