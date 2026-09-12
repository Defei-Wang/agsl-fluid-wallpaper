package com.agsl.wallpaper

object Shaders {
    const val RENDER = """
uniform float2 uResolution;
uniform float uTime;
uniform float4 uPointer1; // xy: 坐标, z: 按下状态 (0或1), w: 滤波手速 (快大慢小)
uniform float4 uPointer2; // xy: 坐标, z: 按下状态 (0或1), w: 滤波手速 (快大慢小)

half4 main(float2 fragCoord) {
    float minRes = min(uResolution.x, uResolution.y);
    if (minRes <= 0.0) return half4(0.002, 0.003, 0.006, 1.0);
    float2 uv = (fragCoord - 0.5 * uResolution) / minRes;

    float2 p1 = (uPointer1.xy - 0.5 * uResolution) / minRes;
    float2 p2 = (uPointer2.xy - 0.5 * uResolution) / minRes;

    // 纯物理表面张力排挤位移（绝对无任何发光、无紫光斑、无颜色加亮）
    float2 displacement = float2(0.0);

    if (uPointer1.z > 0.01) {
        float2 d = uv - p1;
        float r2 = dot(d, d);
        float radius = 0.15 + uPointer1.w * 0.25; // 速度决定油膜排开的范围大小
        float weight = smoothstep(radius, 0.0, sqrt(r2)) * uPointer1.z;
        displacement += normalize(d + 1e-5) * weight * (0.04 + uPointer1.w * 0.08);
    }

    if (uPointer2.z > 0.01) {
        float2 d = uv - p2;
        float r2 = dot(d, d);
        float radius = 0.15 + uPointer2.w * 0.25;
        float weight = smoothstep(radius, 0.0, sqrt(r2)) * uPointer2.z;
        displacement += normalize(d + 1e-5) * weight * (0.04 + uPointer2.w * 0.08);
    }

    // 带有位移变形的油膜 UV
    float2 warpedUV = uv - displacement;
    float t = uTime * 0.08;

    // 极其克制的暗色薄膜干涉纹理（纯物理折射感）
    float q = sin(warpedUV.x * 12.0 + t * 1.2) * cos(warpedUV.y * 12.0 - t * 0.9)
            + sin(length(warpedUV * 1.5) * 16.0 - t * 2.0);
    
    float sheen = q * 0.5 + 0.5;

    // 极暗高级黑底色与微弱次表面油膜衍射（低饱和青绿与蓝灰，永不发光）
    half3 abyss = half3(0.002, 0.003, 0.006);
    half3 filmColor = half3(0.02, 0.04, 0.07) + half3(0.015, 0.03, 0.04) * sin(sheen * 6.28318 + t);

    half3 col = abyss + filmColor;

    // 指数黑阶压制，柔和顺滑
    col = half3(1.0) - exp(-col * 1.2);

    return half4(clamp(col, 0.0, 1.0), 1.0);
}
"""
}
