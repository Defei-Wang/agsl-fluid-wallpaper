float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 getOrganicSegment(vec2 uv, vec2 a, vec2 b, float tA, float tB, float t, float seed) {
    vec2 ba = b - a;
    vec2 pa = uv - a;
    float l2 = dot(ba, ba);
    float h = (l2 < 1e-5) ? 0.0 : clamp(dot(pa, ba) / l2, 0.0, 1.0);
    float rawDist = length(pa - ba * h);

    float t_touch = mix(tA, tB, h);
    float age = t - t_touch;
    if (age > 3.5 || age <= 0.0) return vec3(0.0);

    float speed = 0.70;
    float R = age * speed;

    vec2 mid = 0.5 * (a + b);
    float ang = atan(uv.y - mid.y, uv.x - mid.x);

    float fold = sin(ang * 2.0);
    float wave1 = sin(rawDist * 8.0 - age * 3.0 + fold * 2.5);
    float wave2 = cos(ang * 3.0 + sin(rawDist * 16.0 - age * 1.5));
    float macroFold = clamp(R * 0.28, 0.0, 0.15) * sin(wave1 + wave2 + sin(rawDist * 22.0 - age * 1.2));

    float targetR = R + macroFold;
    float d0 = abs(rawDist - targetR);
    float intensity = 0.0055 / (d0 + 0.0032);

    float R1 = max(0.0, R - 0.16);
    if (R1 > 0.0) {
        intensity += (0.0020 / (abs(rawDist - (R1 + macroFold * 0.7)) + 0.0032)) * step(0.16 / speed, age);
    }

    float glitter = 0.025 * abs(sin(ang * 24.0 + rawDist * 36.0 - age * 4.0));
    float noise = 0.030 * fract(hash(uv * 180.0) + age);
    float modulation = 0.88 + 0.24 * (glitter + noise);
    intensity *= modulation;

    float frontMask = smoothstep(0.03, -0.01, rawDist - targetR);
    float spatialDecay = 1.0 / sqrt(max(0.04, R * 2.0 + 0.08));
    float temporalFade = smoothstep(3.5, 2.0, age) * smoothstep(0.0, 0.06, age);

    vec3 col = mix(vec3(0.25, 0.85, 1.55), vec3(1.15, 0.40, 1.35), fract(seed * 17.713));
    return col * intensity * frontMask * spatialDecay * temporalFade;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    float minRes = min(iResolution.x, iResolution.y);
    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / minRes;

    vec3 finalColor = vec3(0.0);
    float t = iTime;

    if (iMouse.z > 0.5) {
        vec2 mouseA = (iMouse.zw - 0.5 * iResolution.xy) / minRes;
        vec2 mouseB = (iMouse.xy - 0.5 * iResolution.xy) / minRes;
        float cycle = mod(t, 2.8);
        finalColor = getOrganicSegment(uv, mouseA, mouseB, t - cycle, t - cycle + 0.4, t, 0.42);
    } else {
        float cycle = mod(t, 3.2);
        float tBase = t - cycle;
        float seed = fract(floor(t / 3.2) * 19.173);

        vec2 p0 = vec2(-0.35, -0.22);
        vec2 p1 = vec2(-0.18,  0.15);
        vec2 p2 = vec2( 0.05, -0.10);
        vec2 p3 = vec2( 0.22,  0.20);
        vec2 p4 = vec2( 0.38, -0.05);

        vec3 w0 = getOrganicSegment(uv, p0, p1, tBase + 0.00, tBase + 0.20, t, seed);
        vec3 w1 = getOrganicSegment(uv, p1, p2, tBase + 0.20, tBase + 0.40, t, seed);
        vec3 w2 = getOrganicSegment(uv, p2, p3, tBase + 0.40, tBase + 0.60, t, seed);
        vec3 w3 = getOrganicSegment(uv, p3, p4, tBase + 0.60, tBase + 0.80, t, seed);

        finalColor = max(finalColor, w0);
        finalColor = max(finalColor, w1);
        finalColor = max(finalColor, w2);
        finalColor = max(finalColor, w3);
    }

    float ambient = smoothstep(0.98, 1.0, hash(uv + t * 0.05)) * 0.04;
    finalColor += vec3(ambient * 0.5, ambient * 0.6, ambient + 0.02);

    fragColor = vec4(clamp(finalColor, 0.0, 1.0), 1.0);
}
