package com.agsl.wallpaper

object Shaders {
    const val RENDER = """
uniform float2 uResolution;
uniform float uTime;
uniform float4 uPointer1;    // xy: 屏幕坐标, z: isDown, w: 手速
uniform float4 uPointerVel1; // xy: 速度矢量 (px/s), zw: 0
uniform float4 uPointer2;
uniform float4 uPointerVel2;
uniform float4 uVortices[16];// xy: 坐标, z: 环量强度 Gamma, w: 涡核尺寸
uniform float uVortexCount;
uniform float uTurbulence;   // 剧烈搅动产生的微观湍流破碎度

float2 rotate(float2 p, float a) {
    float c = cos(a), s = sin(a);
    return float2(p.x * c - p.y * s, p.x * s + p.y * c);
}

// 纯径向势流位移（无旋场：Curl = 0）：仅在点击按压处向外平缓挤压油膜
float2 radialPotentialPush(float2 p, float2 center, float strength, float radius) {
    float2 d = p - center;
    float r = length(d);
    float env = exp(-r * r / (radius * radius * 2.0));
    return normalize(d + 1e-5) * (strength * env);
}

// 二维黏性 Lamb-Oseen 涡旋（有旋场：Curl != 0）
float2 lambOseenFlow(float2 p, float2 center, float gamma, float coreR) {
    float2 d = p - center;
    float r2 = dot(d, d);
    float factor = (1.0 - exp(-r2 / (coreR * coreR))) / (r2 + 0.005);
    return gamma * float2(-d.y, d.x) * factor * exp(-r2 * 2.6);
}

half4 main(float2 fragCoord) {
    float minRes = min(uResolution.x, uResolution.y);
    if (minRes <= 0.0) return half4(0.002, 0.004, 0.008, 1.0);

    float2 uv = (fragCoord - 0.5 * uResolution) / minRes;
    float2 p1 = (uPointer1.xy - 0.5 * uResolution) / minRes;
    float2 p2 = (uPointer2.xy - 0.5 * uResolution) / minRes;

    // 1. 无旋径向势排挤（静止点击绝不产生自旋）
    float2 potentialDisp = float2(0.0);
    if (uPointer1.z > 0.001) {
        float pushStr = 0.045 * uPointer1.z;
        potentialDisp += radialPotentialPush(uv, p1, pushStr, 0.16);
    }
    if (uPointer2.z > 0.001) {
        float pushStr = 0.045 * uPointer2.z;
        potentialDisp += radialPotentialPush(uv, p2, pushStr, 0.16);
    }

    // 2. 动量拖拽与剪切位移（直线拖动产生偶极排挤，无额外旋度输入）
    float2 dragDisp = float2(0.0);
    if (uPointer1.z > 0.001) {
        float2 vel1 = (uPointerVel1.xy / minRes);
        float d1 = length(uv - p1);
        dragDisp += vel1 * 0.07 * exp(-d1 * d1 / 0.025);
    }
    if (uPointer2.z > 0.001) {
        float2 vel2 = (uPointerVel2.xy / minRes);
        float d2 = length(uv - p2);
        dragDisp += vel2 * 0.07 * exp(-d2 * d2 / 0.025);
    }

    // 基础流形位移结合
    float2 woundUV = uv - potentialDisp - dragDisp;

    // 3. 历史自旋角动量积分场（绕圈搅动注入的有旋点涡）
    for (int i = 0; i < 16; i++) {
        float activeFlag = step(float(i), uVortexCount - 0.5);
        float4 v = uVortices[i];
        if (activeFlag > 0.5 && abs(v.z) > 0.01) {
            float2 vPos = (v.xy - 0.5 * uResolution) / minRes;
            float2 d = woundUV - vPos;
            float r2 = dot(d, d);
            float core = v.w * v.w;
            // 随角动量强度积分的局部流动旋转扭曲
            float twist = v.z * exp(-r2 / (2.2 * core));
            woundUV = vPos + rotate(d, twist);
        }
    }

    // 4. 背景本底宏观对流微场
    float t = uTime * 0.16;
    float2 ambientCenter = float2(sin(t * 0.6) * 0.12, cos(t * 0.5) * 0.09);
    float2 velocity = lambOseenFlow(woundUV, ambientCenter, 0.035, 0.25);

    // 5. 坐标平流
    float2 warpedUV = woundUV + velocity * 0.08;

    // 剧烈搅动引起的微观紊流破裂调制
    float turbWarp = 0.0;
    if (uTurbulence > 0.05) {
        turbWarp = sin(warpedUV.x * 28.0 + t * 2.0) * cos(warpedUV.y * 28.0 - t * 2.0) * uTurbulence * 0.08;
    }

    // 6. 三维极小曲面谐波相分离结构（基底自组织场）
    float wavePattern = 0.0;
    float freq = 4.8;
    float amp = 0.45;
    for (int k = 0; k < 3; k++) {
        float2 q = rotate(warpedUV, float(k) * 1.047 + t * 0.10);
        float gyroid = sin(q.x * freq + t) * cos(q.y * freq - t * 0.7)
                     + sin(q.y * freq * 0.85 + t * 0.35);
        wavePattern += abs(gyroid) * amp;
        freq *= 1.75;
        amp *= 0.55;
    }

    wavePattern += turbWarp;

    // 7. 色度阶梯映射：深邃曜石黑底色与冷光条纹（无紫光斑）
    float ridge = wavePattern - 0.52;
    float glow = 0.45 * exp(-ridge * ridge * 48.0);
    glow += length(velocity) * 0.25;

    half3 cAbyss   = half3(0.002, 0.004, 0.008);
    half3 cDeepSea = half3(0.020, 0.120, 0.280);
    half3 cTeal    = half3(0.050, 0.400, 0.520);
    half3 cCyan    = half3(0.120, 0.680, 0.800);

    float hue = sin(wavePattern * 3.1415 + t * 0.4);
    half3 cDynamic = mix(cTeal, cCyan, half(smoothstep(-0.2, 0.4, hue)));

    half3 color = cAbyss;
    color = mix(color, cDeepSea, half(smoothstep(0.08, 0.42, glow)));
    color = mix(color, cDynamic, half(smoothstep(0.42, 1.20, glow)));

    color = half3(1.0) - exp(-color * 1.35);

    return half4(clamp(color, 0.0, 1.0), 1.0);
}
"""
}
