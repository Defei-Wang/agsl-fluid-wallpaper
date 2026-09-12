# AGSL Fluid Wallpaper (agsl-fluid-wallpaper)

### English

A GPU-accelerated procedural water wave live wallpaper for Android 13+ (API 33+), engineered using Android Graphics Shading Language (AGSL) and `RuntimeShader`.

Unlike conventional approaches relying on WebView wrappers or particle meshes, this engine executes fully procedural mathematics directly in fragment shading units via the system render thread (`RenderThread`), delivering native frame rates with near-zero memory bandwidth overhead.

**Key Architecture Features**
* **Zero Framebuffer Bandwidth**: Fully procedural mathematical synthesis without off-screen textures (FBO) or CPU mesh streaming.
* **Anti-Flicker Numerics**: Decouples high-frequency noise from reciprocal denominators to prevent energy oscillation and brightness spikes.
* **Causal Segment Wake Dynamics**: Models stroke motion as 1D geometric line segments with temporal causality ($t_{touch} = \text{mix}(t_A, t_B, h)$), generating continuous tapered Kelvin wakes without spatial aliasing.
* **Adaptive Spatial Step**: Adjusts segment thresholds dynamically with gesture speed (120\~260 px) to preserve long-tail wake diffusion for up to 3.5 seconds.

**Version Evolution**
* **v1.0.0 (Ambient Center Engine)**: Autonomous non-linear polar coordinate fluid dynamics originating from the display center. Demonstrates multi-layer topological folding and micro-crystalline noise perturbation.
* **v2.0.0 (Discrete Point-Source Multi-Touch)**: Touch-reactive model featuring speed-dependent core scaling and isolated multi-lobed water droplets. Includes temporal throttling to prevent premature ripple termination.
* **v3.0.0 (Causal Segment Long-Tail Wake Engine)**: Production architecture. Upgrades point sources to segment distance fields (Segment SDF). Eliminates Voronoi tearing through smooth midpoint continuous angles and `max()` projective blending, rendering tapered, organic wake waves that dynamically expand into full-screen fluid wakes.

**Project Structure**
```text
agsl-fluid-wallpaper/
├── app/
│   └── src/main/java/com/agsl/wallpaper/
│       ├── MainActivity.kt           # Wallpaper preview intent launcher
│       └── AgslWallpaperService.kt   # Core WallpaperService & AGSL Runtime
├── shadertoy/
│   ├── v1_ambient.glsl               # WebGL prototype for v1.0
│   ├── v2_discrete_point.glsl        # WebGL prototype for v2.0
│   └── v3_causal_segment.glsl        # WebGL prototype for v3.0
└── README.md

Build & InstallationPowerShellgit clone `[https://github.com/Defei-Wang/agsl-fluid-wallpaper.git](https://github.com/Defei-Wang/agsl-fluid-wallpaper.git)`
cd agsl-fluid-wallpaper
.\gradlew assembleDebug
Pre-built binaries for each milestone can be downloaded directly from GitHub Releases.中文说明基于 Android 13+ (API 33+) 平台 AGSL (RuntimeShader) 研发的高性能原生程序化水波壁纸引擎。项目跳出传统 WebView/HTML 转制或复杂粒子网格管线，完全依托片元着色器纯数学方程解算，直接由 Android 系统渲染线程（RenderThread）驱动硬件加速，在零显存带宽占用的前提下输出满帧流体物理交互。核心架构特性零纹理显存带宽：纯程序化算子解算，无需离屏帧缓存（FBO）读写或 CPU 网格数据同步。抗频闪数值解耦：将高频微扰噪波移至分子端做温和调幅，从数学层面根治反比例光核因分母扰动造成的帧间高频闪烁。时间因果律线段场（Segment SDF）：将滑动轨迹抽象为一维线段几何场，结合时间插值因果律（$t_{touch} = \text{mix}(t_A, t_B, h)$），驱动水波沿位移方向平滑展开为开尔文长尾水痕。自适应动态步长：根据手势划动速度自适应调节切分阈值（120~260 像素），并施加时间节流窗口，使尾迹得以经历长达 3.5 秒的宏观大尺度扩散周期。版本演进历程v1.0.0 (中心自主发光版)：无触控交互的中心极坐标非线性流体。展示了高阶多瓣拓扑折叠与微观晶格电离噪波。v2.0.0 (指尖光核与离散水滴版)：引入速度负相关的自适应跟手光核，滑动时克制地落下一枚枚独立展开的放射状多瓣同心水花，内建防覆盖时间锁确保老水花完整绽放。v3.0.0 (开尔文长尾流体引擎)：生产交付架构。将点源升维为一维线段场，基于几何中点连续极角与 max() 极值穿透混合消除接缝截断，重现前细后粗、尾部充分扩展的高阶有机流体水面尾迹。编译构建PowerShellgit clone `[https://github.com/Defei-Wang/agsl-fluid-wallpaper.git](https://github.com/Defei-Wang/agsl-fluid-wallpaper.git)`
cd agsl-fluid-wallpaper
.\gradlew assembleDebug
各版本构建产物可在 GitHub Releases 页面直接下载。
---

**Git 提交与推送命令**

在 Android Studio 终端中执行以下命令。代码树将以最新成熟的 v3.0 作为主体，并同时打上 `v1.0.0`、`v2.0.0`、`v3.0.0` 三个版本标签：

```powershell
# 1. 确保在项目根目录，初始化仓库并设定主分支
git init
git branch -M main

# 2. 检查暂存并提交代码
git add .
git commit -m "feat: complete agsl fluid wallpaper engine with long-tail kelvin wake"

# 3. 创建对应的发布版本标签
git tag -a v1.0.0 -m "Release v1.0.0: Ambient Center Engine"
git tag -a v2.0.0 -m "Release v2.0.0: Discrete Point-Source Multi-Touch"
git tag -a v3.0.0 -m "Release v3.0.0: Causal Segment Long-Tail Wake Engine"

# 4. 关联 GitHub 远程仓库
git remote add origin `https://github.com/Defei-Wang/agsl-fluid-wallpaper.git`

# 5. 推送分支与全部版本标签
git push -u origin main --tags