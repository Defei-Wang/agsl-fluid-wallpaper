float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 getRichRipple(vec2 uv, vec4 r, float t) {
    float age = max(0.0, t - r.z);
    if (step(0.5, r.w) * step(age, 3.5) < 0.5) return vec3(0.0);

    float seed = fract(r.z * 13.513);
    float lobes = floor(2.0 + fract(seed * 41.2) * 3.0);
    float rot = seed * 6.283;
    float speed = 0.85 + fract(seed * 17.5) * 0.35;

    vec3 colA = vec3(0.3, 0.85, 1.5);
    vec3 colB = vec3(1.1, 0.45, 1.35);
    vec3 rippleColor = mix(colA, colB, fract(seed * 93.1));

    vec2 delta = uv - r.xy;
    float dist = length(delta) * 2.2;
    float a = atan(delta.y, delta.x) + rot;

    float fold = sin(a * lobes);
    float w0 = sin(dist * 4.0 - age * 2.2 + fold * 2.5);
    float q0 = age * speed + 0.16 * sin(w0 + cos(a * 3.0 + dist * 6.0 - age * 1.2));

    float noise = 0.06 * fract(hash(uv * 160.0) + age);
    float d0 = abs(dist - q0) + noise;
    float intensity = 0.0042 / (d0 + 0.0016);

    float q1 = max(0.0, age - 0.22) * speed + 0.14 * sin(w0);
    float d1 = abs(dist - q1) + noise;
    intensity += (0.0022 / (d1 + 0.0016)) * step(0.22, age);

    float fade = smoothstep(3.5, 2.0, age) * smoothstep(0.0, 0.1, age);
    return rippleColor * intensity * fade;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    float minRes = min(iResolution.x, iResolution.y);
    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / minRes;

    vec3 finalColor = vec3(0.0);
    float t = iTime;
    vec2 cursorPos;

    if (iMouse.z > 0.5) {
        cursorPos = (iMouse.xy - 0.5 * iResolution.xy) / minRes;
        float cycle = mod(t, 2.4);
        finalColor = max(finalColor, getRichRipple(uv, vec4(cursorPos, t - cycle, 1.0), t));
    } else {
        cursorPos = vec2(0.28 * cos(t * 1.6), 0.22 * sin(t * 2.2));
        for (int i = 0; i < 4; i++) {
            float spawnT = floor(t * 1.5 - float(i)) / 1.5;
            vec2 p = vec2(0.28 * cos(spawnT * 1.6), 0.22 * sin(spawnT * 2.2));
            finalColor = max(finalColor, getRichRipple(uv, vec4(p, spawnT, 1.0), t));
        }
    }

    float cursorDist = length(uv - cursorPos);
    float dotCore = exp(-cursorDist * 75.0) * 0.95;
    float dotGlow = exp(-cursorDist * 20.0) * 0.35;
    vec3 dotColor = vec3(0.35, 0.85, 1.55) * (dotCore + dotGlow);
    finalColor = max(finalColor, dotColor);

    float ambient = smoothstep(0.98, 1.0, hash(uv + t * 0.05)) * 0.04;
    finalColor += vec3(ambient * 0.5, ambient * 0.6, ambient + 0.02);

    fragColor = vec4(clamp(finalColor, 0.0, 1.0), 1.0);
}
