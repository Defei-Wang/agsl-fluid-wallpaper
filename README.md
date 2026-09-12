# AGSL Fluid Wallpaper (agsl-fluid-wallpaper)

An ultra-efficient, GPU-accelerated procedural water wave live wallpaper for Android 13+ (API 33+), engineered using Android Graphics Shading Language (AGSL) and `RuntimeShader`.

Unlike conventional approaches that rely on heavy WebView wrappers, framebuffers (FBO), or mesh simulations, this engine executes pure procedural mathematics directly in fragment shading units via the system render thread (`RenderThread`), delivering near-zero memory bandwidth overhead and native frame-rate performance.

---

## Technical Highlights / 技术特性

* **Zero Memory Bandwidth / 零纹理显存带宽占用**
  100% procedural calculation without off-screen textures or framebuffers. Zero texture fetches (`Texture Fetch = 0`).
  完全基于片元着色器纯数学方程解算，不占用离屏纹理显存带宽，杜绝总线带宽读写开销。

* **Branchless Stability / 无分支抗崩溃架构**
  Replaces dynamic GPU branching (`if/else`) with continuous mathematical step functions (`step`, `clamp`, `max`) to eliminate shader compiler crashes across diverse mobile GPUs.
  全面剔除片元着色器内部动态逻辑分支，杜绝不同芯片平台上的 GPU 驱动编译异常。

* **Causal Segment Wake / 时间因果线段场**
  Models touch strokes as 1D geometric line segments rather than discrete point arrays. Wavefronts propagate along the motion axis using temporal causality ($t_{touch} = \text{mix}(t_A, t_B, h)$), generating seamless, tapered wakes without spatial aliasing.
  将手势划动建模为一维几何线段，通过沿轨迹的时间差函数驱动波前自半径 0 开始向外扩散，消除高速拖拽时的断节与抽帧。

---

## Release Architecture Evolution / 版本演进架构

### v1.0.0: Ambient Center Engine (中心自主发光版)
* **Interaction**: Autonomous procedural fluid originating from the display center without touch input.
* **Physics**: Polar coordinate non-linear fluid dynamics with multi-scale topological folding and micro-cellular noise perturbation.
* **交互与机制**：无触控交互的中心极坐标非线性流体，展示高阶极坐标折叠与微观电离晶格微扰。

### v2.0.0: Discrete Point-Source Multi-Touch (指尖光核与离散水花版)
* **Interaction**: Multi-touch responsive point-source model.
* **Physics**: Dynamic glowing cursor with speed-inverse scaling, accompanied by independent multi-lobed water blossoms governed by birth-time seeds. Introduces `max()` projective blending to prevent overexposure.
* **交互与机制**：指尖吸附速度负相关的微光核，移动时克制地落下独立的多瓣放射状水花，通过 `max()` 极值混合彻底解决交汇处的死白过曝。

### v3.0.0: Causal Segment Wake Engine (时间因果开尔文长尾水痕版 - Production)
* **Interaction**: Continuous drag-and-swipe fluid wake.
* **Physics**: Segment-based distance field with temporal causality. Employs hardware-synchronized IIR filtering and dynamic threshold scaling to eliminate capacitor jitter. The wavefront smoothly expands from zero into large-scale organic fluid ripples with a 3.5-second complete physical life cycle.
* **交互与机制**：现行生产架构。升维至一维时间因果线段场，结合低通滤波与自适应步长，波纹自零半径起步向后展开为长效开尔文尾迹，拥有从 0.0\~3.5 秒的完整物理扩散周期。

---

## Project Structure / 目录结构

```text
agsl-fluid-wallpaper/
├── app/
│   └── src/main/java/com/agsl/wallpaper/
│       ├── MainActivity.kt           # Entry activity for wallpaper preview
│       └── AgslWallpaperService.kt   # Core WallpaperService & AGSL Runtime
├── shadertoy/
│   ├── v1_ambient.glsl               # WebGL prototype for v1.0
│   ├── v2_discrete_point.glsl        # WebGL prototype for v2.0
│   └── v3_causal_segment.glsl        # WebGL prototype for v3.0
└── README.md
```

---

## Build & Installation / 编译与安装

1. Requires Android Studio Ladybug | 2024.2+ and JDK 17+.
2. Clone the repository and compile:
   ```powershell
   git clone `[https://github.com/Defei-Wang/agsl-fluid-wallpaper.git](https://github.com/Defei-Wang/agsl-fluid-wallpaper.git)`
   cd agsl-fluid-wallpaper
   .\gradlew assembleDebug
   ```
3. Pre-compiled standalone packages for all releases (`agsl-fluid-v1.0.0.apk`, `agsl-fluid-v2.0.0.apk`, `agsl-fluid-v3.0.0.apk`) are available under GitHub Releases:
   `https://github.com/Defei-Wang/agsl-fluid-wallpaper/releases`