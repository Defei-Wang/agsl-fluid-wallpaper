float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void mainImage(out vec4 fragColor, in vec2 fragCoord) {
    float minRes = min(iResolution.x, iResolution.y);
    if (minRes <= 0.0) { fragColor = vec4(0.0, 0.0, 0.0, 1.0); return; }

    vec2 uv = (fragCoord - 0.5 * iResolution.xy) / minRes;
    float r = length(uv) * 2.2;
    float a = atan(uv.y, uv.x);

    float fold = sin(a * 2.0);
    float wave1 = sin(r * 4.0 - iTime * 1.5 + fold * 3.0);
    float wave2 = cos(a * 3.0 + sin(r * 8.0 - iTime * 0.8));
    float q = 0.85 + 0.35 * sin(wave1 + wave2 + sin(r * 12.0 - iTime * 0.5));

    float dist = abs(r - q) + 0.04 * abs(sin(a * 32.0 + r * 20.0 - iTime * 2.0));
    float intensity = 0.0062 / (dist + 0.0014) * smoothstep(2.6, 0.1, r);

    vec3 color = clamp(vec3(0.95, 0.82, 1.35) * intensity, 0.0, 1.0);
    float ambient = smoothstep(0.98, 1.0, hash(uv + iTime * 0.05)) * 0.04;
    color += vec3(ambient * 0.5, ambient * 0.6, ambient + 0.02);

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
