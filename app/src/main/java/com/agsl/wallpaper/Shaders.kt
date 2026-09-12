package com.agsl.wallpaper

object Shaders {

    const val RENDER = """
uniform float2 uResolution;
uniform float uTime;

uniform float4 uPointer1; // xy: 坐标 z: 按下态 w: 手速
uniform float4 uPointer2; // xy: 坐标 z: 按下态 w: 手速

float hash21(float2 p) {
    return fract(
        sin(dot(p, float2(12.9898, 78.233))) *
        43758.5453
    );
}

half4 main(float2 fragCoord) {

    float minRes = min(uResolution.x, uResolution.y);

    if (minRes <= 0.0)
        return half4(0.001, 0.002, 0.004, 1.0);

    float2 uv =
        (fragCoord - 0.5 * uResolution) / minRes;

    float2 p1 =
        (uPointer1.xy - 0.5 * uResolution) / minRes;

    float2 p2 =
        (uPointer2.xy - 0.5 * uResolution) / minRes;

    float displacement = 0.0;
    float tearEffect = 1.0;

    // =========================================================
    // 1. 非发光型触控形变
    //
    // 保留触摸互动，但去掉原来的强圆形“鼓包亮圈”
    // =========================================================

    if (uPointer1.z > 0.01) {

        float d1 = length(uv - p1);

        float r1 =
            0.10 +
            uPointer1.w * 0.10;

        float w1 =
            smoothstep(r1, 0.0, d1);

        // 极弱形变，只负责扰动表面
        displacement +=
            w1 *
            (0.010 + uPointer1.w * 0.018);
    }

    if (uPointer2.z > 0.01) {

        float d2 = length(uv - p2);

        float r2 =
            0.10 +
            uPointer2.w * 0.10;

        float w2 =
            smoothstep(r2, 0.0, d2);

        displacement +=
            w2 *
            (0.010 + uPointer2.w * 0.018);

        // 双指 Fusion 保留
        float midDist =
            length(p1 - p2);

        if (midDist < 0.55) {

            float fusion =
                smoothstep(0.0, 0.55, midDist);

            displacement *=
                mix(1.12, 1.0, fusion);
        }
    }

    // =========================================================
    // 2. 快速滑动 Slicing
    // 保留原来的互动
    // =========================================================

    if (
        uPointer1.z > 0.01 &&
        uPointer1.w > 0.18
    ) {

        // 使用固定稳定方向，
        // 避免原来的假速度切线造成异常
        float2 velDir = normalize(
            float2(1.0, 0.0)
        );

        float lineDist =
            abs(
                dot(
                    uv - p1,
                    float2(-velDir.y, velDir.x)
                )
            );

        float d1 =
            length(uv - p1);

        tearEffect *=
            smoothstep(
                0.004,
                0.025,
                lineDist + d1 * 0.15
            );
    }

    // =========================================================
    // 3. 表面
    // =========================================================

    float2 warpedUV =
        uv - displacement;

    float t =
        uTime * 0.07;

    float q =
        sin(
            warpedUV.x * 13.0 + t
        ) *
        cos(
            warpedUV.y * 13.0 - t
        )
        +
        cos(
            length(warpedUV * 1.4) * 16.0
            - t * 1.2
        );

    float sheen =
        (q * 0.5 + 0.5) *
        tearEffect;

    // =========================================================
    // 4. 纯深色油膜
    //
    // 删除所有可能产生紫/粉色高亮的颜色通道
    // =========================================================

    half3 abyss =
        half3(
            0.001,
            0.002,
            0.004
        );

    half3 filmColor =
        half3(
            0.008,
            0.022,
            0.030
        )
        +
        half3(
            0.004,
            0.012,
            0.018
        )
        *
        sin(
            sheen * 6.28318 + t
        );

    half3 col =
        abyss +
        filmColor;

    // 非常轻的压缩
    col =
        half3(1.0) -
        exp(-col * 1.15);

    return half4(
        clamp(col, 0.0, 1.0),
        1.0
    );
}
"""
}
