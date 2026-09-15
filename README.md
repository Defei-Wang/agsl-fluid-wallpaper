# AGSL Fluid Wallpaper Engine (v1.0.0 ~ v8.0.0)

## 高性能流体与深海生物运动引擎

[English Documentation](#english-documentation) | [中文技术文档与版本演进剖析](#中文技术文档与版本演进剖析)

## Credits

The marine organism models and source logic used from **v5.2.0 through v8.0.0** are based on original works by **@yuruyurau (#つぶやきProcessing)** on X. The organism designs, procedural forms, and core mathematical constructions come from those works. This project adapts, restructures, and integrates them into an Android AGSL/Kotlin live-wallpaper engine.

本项目 **v5.2.0 至 v8.0.0** 使用的海洋生物模型及相关代码逻辑均基于 **X 用户 @yuruyurau（#つぶやきProcessing）** 的原创作品。海洋生物的设计、程序化形态和核心数学构造来自原作品；本项目主要负责 Android AGSL/Kotlin 侧的移植、重构和整合。


# English Documentation

## 1. Project Overview & Architectural Philosophy

The **AGSL Fluid Wallpaper Engine** is an open-source procedural live-wallpaper engine for Android 13+ (API level 33+). It uses **Android Graphics Shading Language (AGSL)**, Skia's `RuntimeShader`, and the hardware-accelerated Canvas pipeline to generate the scene directly on the device.

The project has gone through two distinct architectural phases:

1. **Hydrodynamic Fluid Manifold Era (v1.0.0 ~ v5.1.0)**: The early engine separates low-cost physical history integration on the CPU from analytical coordinate evaluation on the GPU. The goal is to keep the renderer free of external texture allocations while maintaining high refresh-rate performance.

2. **Procedural Marine Organism Phase (v5.2.0 ~ v8.0.0)**: From v5.2.0 onward, the project starts integrating procedural marine organisms. Their shapes and motion are driven by the underlying mathematical forms rather than by sprite animation.

### Key Characteristics

- **No external render textures**: The main rendering path does not use off-screen Ping-Pong render targets.

- **High-refresh-rate rendering**: The design targets native high-refresh-rate displays while keeping the per-frame workload predictable.

- **Persistent interaction memory**: The v5.1.0 fluid model retains rotational history and shear over a relaxation window of roughly 15–20 seconds.

- **Procedural morphology**: Organisms are generated from analytical coordinate mappings rather than static meshes or sprite sheets.

## 2. Version History (v1.0.0 ~ v8.0.0)

| Version | Milestone | Main technical changes | Main limitation / reason for the next iteration |
|---|---|---|---|
| **v1.0.0** | Ambient Center Engine | 2D steady conformal flow mapping; complex potential $W(z)=\phi+i\psi$; single-center harmonic interaction. | Single-touch interaction only; displacement was static and had no viscosity or velocity response. |
| **v2.0.0** | Discrete Point-Source Engine | Hydrodynamic dipole field; multi-touch tracking; streamline superposition. | Interaction strength was still independent of gesture speed; no momentum accumulation. |
| **v3.0.0** | Causal Segment Wake Wave Engine | Dispersive gravity-capillary surface-wave model; Kelvin wedge angle $\theta=\arcsin(1/3)\approx19.47^\circ$; gesture-velocity sampling. | Dense wave superposition could create bright hotspots and unwanted purple/magenta glare during prolonged interaction. |
| **v4.0.0** | Multi-Scale Organic Fluid Wake | Schoen's Gyroid minimal-surface approximation; logarithmic-spiral mapping; nonlinear tone mapping; colder color palette. | Single-pass stateless shader; the fluid had no temporal memory and snapped back after release. |
| **v5.0.0** | Advanced Viscous Oil Engine | Droplet-proximity tension fusion; high-speed slicing grooves; attempted GPU off-screen Ping-Pong feedback. | Severe numerical diffusion from hardware bilinear filtering blurred fine Gyroid detail within a few frames. |
| **v5.1.0** | Physical Hydrodynamic Manifold | Helmholtz-Hodge decomposition; Lamb-Oseen vortex core; 16-slot CPU Lagrangian history; algebraic sigmoid shear. | Marked the transition from a continuous fluid model toward discrete biological motion. |
| **v5.2.0** | Balanced Abyssal Ecology | First procedural deep-sea organisms; Poisson-scattered spawning; independent touch collision. | Early organism models still tended to translate rigidly, and several organisms could react together. |
| **v6.0.0** | Ethereal Abyssal Ecology | Zero-allocation render path; continuous tentacle morphology; inline distance calculations; separated small jellyfish. | Species still shared too much of the same mathematical structure, and motion lacked natural resistance. |
| **v7.0.0** | Refined Engine & Sharp Rendering | Restored 10,000-point detail; time-varying phase scanning for golden stippling; five depth/luminance layers. | Medium and ultra-long jellyfish still showed an artificial vertical “spring” response when touched. |
| **v8.0.0** | Kinematics & Native Orbit Update | Native large-radius orbit for the long jellyfish; doubled ultra-long jellyfish propulsion stroke; independent hydroid topology; unified ease-out damping. | Current production baseline for the deep-sea ecological wallpaper. |

## 3. Mathematical Foundations

### 3.1 v1.0.0 ~ v2.0.0: Potential Flow & Dipole Mechanics

Early iterations relied on planar ideal potential flow. A point source or sink at $z\_0 = x\_0 + i y\_0$ is defined by the complex potential:

$$W(z) = \Phi(x, y) + i \Psi(x, y) = \frac{m}{2\pi} \ln(z - z\_0)$$

For dual touches (v2.0.0), a hydrodynamic dipole pair was introduced:

$$W\_{\text{dipole}}(z) = \frac{\boldsymbol{\mu} \cdot (z - z\_0)}{2\pi \vert{}z - z\_0\vert{}^2}$$

### 3.2 v3.0.0: Dispersive Kelvin Wakes

To introduce velocity-dependent ripples, v3.0.0 incorporated the kinematic wave dispersion relation:

$$\omega^2(k) = gk + \frac{\gamma}{\rho} k^3$$

A moving disturbance generates an envelope bounded by the Kelvin wedge angle:

$$\alpha = \arcsin(1/3) \approx 19.47^\circ$$

The velocity vector was calculated via historical gesture differentiation:

$$\mathbf{v}(t) = \frac{\mathbf{x}(t) - \mathbf{x}(t - \Delta t)}{\Delta t}$$

### 3.3 v4.0.0: Gyroid Minimal Surface & Color Metric

v4.0.0 established the continuous baseline manifold via Schoen's Gyroid minimal surface nodal approximation:

$$\phi\_0(\mathbf{x}) = \sin(k\_x x) \cos(k\_y y) + \sin(k\_y y) \cos(k\_z z) + \sin(k\_z z) \cos(k\_x x) = 0$$

The color metric was restructured to an exponential absorption spectrum:

$$\mathbf{C}\_{\text{final}} = \mathbf{1} - \exp(-\mathbf{C}\_{\text{linear}} \cdot \gamma)$$

### 3.4 v5.0.0 vs. v5.1.0: Lagrangian Mechanics vs. Eulerian Dissipation

The attempted v5.0.0 off-screen Ping-Pong buffer sampled the previous frame via:

$$I(t + \Delta t, \mathbf{x}) = I(t, \mathbf{x} - \mathbf{u} \Delta t)$$

Hardware bilinear interpolation introduced compounding numerical artificial diffusion:

$$\nu\_{\text{num}} \approx \frac{\Vert{}\mathbf{u}\Vert{} \Delta x}{2} - \frac{\Vert{}\mathbf{u}\Vert{}^2 \Delta t}{2}$$

This blurred all high-frequency Gyroid harmonics within 10 frames.

**v5.1.0 Solution**:

1. **Helmholtz-Hodge Decomposition**:

$$\mathbf{u} = \mathbf{u}\_{\text{potential}} + \mathbf{u}\_{\text{solenoidal}}, \quad \nabla \times \mathbf{u}\_{\text{potential}} = \mathbf{0}, \quad \nabla \cdot \mathbf{u}\_{\text{solenoidal}} = 0$$

2. **Viscous Lamb-Oseen Vortex Core**:

$$u\_\theta(r, t) = \frac{\Gamma}{2\pi r} \left[ 1 - \exp\left(-\frac{r^2}{r\_{\text{core}}^2(t)}\right) \right], \quad \lim\_{r \to 0} u\_\theta(r) = 0$$

3. **Lagrangian Vortex History**:

$$\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \\, \mathrm{d}t, \quad \mathbf{x}' = \mathbf{x}\_k + \mathbf{R}(\Delta \theta\_k) (\mathbf{x} - \mathbf{x}\_k)$$

4. **Algebraic Sigmoid Continuous Shear**:

$$\Delta \mathbf{x}\_{\text{shear}} = \mathbf{n}\_\perp \left[ \frac{d\_\perp}{\kappa + \vert{}d\_\perp\vert{}} \right] A \exp\left(-\frac{d\_\parallel^2}{\sigma\_\parallel^2} - \frac{d\_\perp^2}{\sigma\_\perp^2}\right)$$

### 3.5 v5.2.0 ~ v8.0.0: Procedural Marine Organisms

From v5.2.0 onward, the project integrates a set of procedural marine organisms based on the original work of **@yuruyurau (#つぶやきProcessing)**. The Android version adapts and restructures those organisms for the AGSL/Kotlin renderer; the organism designs, source logic, and core mathematical forms are not original creations of this project.

#### 1. Plump Bug (Golden Stippling Hydrodynamic Beetle)

Preserves continuous surface shimmering by injecting traveling wave phase $t \cdot 0.2$ into polar envelope equations:

$$k = \cos(9 y\_{\text{val}} + 0.2 t) \cdot \begin{cases} 28 \sin(t + y\_{\text{val}}), & y\_{\text{val}} < 9 \\\ 11, & y\_{\text{val}} \ge 9 \end{cases}$$

$$e = \frac{y\_{\text{val}}}{8} - 13, \quad o = \frac{\sqrt{k^2 + e^2}}{6}$$

$$q = \frac{k y\_{\text{val}}}{15} + 79 + k \sin(y\_{\text{val}}) \left( 1 + \sin(4o - e - 8t) \right)$$

$$c = \frac{o}{2} - \frac{e}{4} - t, \quad r\_x = q \sin(c) + 70 \sin\left(\frac{c}{3}\right), \quad r\_y = \frac{q}{0.7} \cos(c)$$

#### 2. Classic Long Jellyfish (Native Large-Radius Orbit Cruising)

In v8.0.0, external linear translation is removed. The organism instead follows its native circular orbit, with the orbit expanded to cover roughly 50% of the screen:

$$k = 5 \cos\left(\frac{x\_{\text{val}}}{14}\right) \cos\left(\frac{y\_{\text{val}}}{30}\right), \quad e = \frac{y\_{\text{val}}}{8} - 13, \quad d = \frac{k^2 + e^2}{59} + 4$$

$$q = 60 - 3 \sin(\mathrm{atan2}(k, e) \cdot e) + k \left( 3 + \frac{4}{d} \sin(d^2 - 2t) \right)$$

$$c = \frac{d}{2} + \frac{e}{99} - \frac{t}{18}, \quad r\_x = q \sin(c), \quad r\_y = (q + 9d) \cos(c) + 65$$

#### 3. Ultra-Long Jellyfish (Doubled Forward Thrust & Clock Decoupling)

Forward displacement is tied to the bell's contraction cycle, with the propulsion coefficient doubled to $0.162$:

$$k = 5 \cos\left(\frac{x\_{\text{val}}}{19}\right) \cos\left(\frac{y\_{\text{val}}}{30}\right), \quad e = \frac{y\_{\text{val}}}{8} - 12, \quad d = \frac{k^2 + e^2}{59} + 2$$

$$c = \frac{d^2}{7} - t, \quad q = 4 \sin(9 \mathrm{atan2}(k, e)) + 9 \sin(d - t) - \frac{k}{d} \left( 9 + 3 \sin(9d - 16t) \right)$$

$$F\_{\text{thrust}} = \left( \max(0, \sin(t - 1.2)) \right)^2 \cdot 0.162$$

#### 4. Golden Small Jellyfish (Classic Harmonic Oscillator)

Operates on high-frequency radial harmonic oscillation:

$$k = 2 \cos(342 i), \quad e = 2 \sin(271 i), \quad d = \frac{\sqrt{k^2 + e^2}}{1.6}$$

$$pp = 5 + 2 \sin(8d - 3t + m), \quad c = \frac{d^2}{9} - \frac{t}{8} + m$$

$$r\_x = k \cdot pp + \frac{9}{d} \sin(2k) + 89 \sin(c), \quad r\_y = 79 \sin(2c) + \frac{9}{d} \sin(2e) + e \cdot pp$$

#### 5. True Single Hydroid (Decoupled Polar Crystalline Topology)

Separated from the Small Jellyfish model in v8.0.0; governed by an independent conditional equation ($e > 0$):

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(7i) \cos(i), \quad (e > 0)$$

$$d = \frac{(k^2 + e^2)^{1.5}}{999} + 4.6 - \frac{\cos^3(t/4 + m)}{3}, \quad o = \sin(d^2 - t + m), \quad c = \frac{d}{8} - \frac{t}{32} + m$$

$$r\_x = 99 \sin(c) + \frac{k}{3^o}, \quad r\_y = 99 \cos\left(\frac{c}{3}\right) + 39d + e^o - 275$$

#### 6. Microscopic Tiny Jellyfish (Kinematic Tangent Velocity Alignment)

A single decoupled individual is scaled by 2.5× and aligned with the instantaneous tangent of its Lissajous-like trajectory:

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(3i) \cos(2i), \quad d = \frac{(k^2 + e^2)^{1.5}}{1999} + 1.5 - \frac{\sin^3(t/2 + m)}{3}$$

$$p\_{\text{val}} = \exp\left( \sin(d^2 - t + m) \ln(\max(d, 0.05)) \right), \quad c = \frac{d}{16} - \frac{t}{48} + m$$

$$\mathbf{v}\_{\text{instant}} = \left( -2.06 \cos(c), -8.25 \cos(4c) \right)^T$$

# 中文技术文档与版本演进剖析

## 一、项目定位与系统架构

在 Android 动态壁纸中，常见方案大致落在两个极端：

1. **轻量但生硬的动画**：使用预渲染视频或简单的周期位移，资源消耗较低，但交互通常比较机械，也很难保留连续的运动反馈。

2. **重型数值流体求解**：如果直接在移动端运行不可压缩 Navier–Stokes 网格求解器，并使用多级 FBO 对速度场和压力场进行 Ping-Pong 迭代，带宽与显存访问成本都会迅速上升。

因此，这个项目的演进大致可以分成两个阶段：

- **v1.0.0 ~ v5.1.0**：**混合拉格朗日–解析运动模型**。CPU 负责保存有限的交互历史，GPU 负责连续解析求值，尽量避免依赖外部纹理反馈。

- **v5.2.0 ~ v8.0.0**：**程序化海洋生物阶段**。项目开始加入一组基于数学方程生成的海洋生物，并逐步把它们的形态、轨迹、推进和阻尼整合进整个渲染系统。这里使用的海洋生物作品来自 **X 用户 @yuruyurau（#つぶやきProcessing）**，本项目主要负责 Android/AGSL/Kotlin 侧的移植、重构和整合。

## 二、版本演进与实现思路 (v1.0.0 ~ v8.0.0)

### 2.1 v1.0.0 - 基础对流共形引擎 (Ambient Center Engine)

- **目标**：先把 Android 13 原生 AGSL `RuntimeShader`、壁纸服务和硬件 Canvas 这条渲染链路跑通，并确认它在实际设备上的性能。

- **力学模型**：采用二维稳态复变共形映射：

$$W(z) = \Phi(x, y) + i \Psi(x, y) = \frac{m}{2\pi} \ln(z - z\_0)$$

- **当时的问题**：只能处理单点交互，位移是静态解析结果，没有惯性，也没有手速带来的差异。

### 2.2 v2.0.0 - 离散点源与双指拓扑交互 (Discrete Point-Source Engine)

- **目标**：加入多点触控，并尝试用偶极子流场描述双指交互产生的局部流动。

- **力学模型**：构造反向偶极子流场：

$$W\_{\text{dipole}}(z) = \frac{\boldsymbol{\mu} \cdot (z - z\_0)}{2\pi \vert{}z - z\_0\vert{}^2}$$

- **当时的问题**：交互强度和手势速度没有关系，快划和慢拖出来的效果基本一样。

### 2.3 v3.0.0 - 因果线段开尔文尾迹引擎 (Causal Segment Wake Wave Engine)

- **目标**：把手指移动速度纳入计算，让移动扰动能够产生带方向和速度感的尾迹。

- **力学模型**：

  1. 通过 `MotionEvent.getHistoricalX/Y` 计算真实手速微分：

$$\mathbf{v}(t) = \frac{\mathbf{x}(t) - \mathbf{x}(t - \Delta t)}{\Delta t}$$

2. 构造开尔文波包夹角包络：

$$\theta\_{\text{Kelvin}} = \arcsin(1/3) \approx 19.47^\circ$$

- **当时的问题**：波形叠加得太密，局部容易过曝；长时间按住时还会出现明显的紫红色杂光。

### 2.4 v4.0.0 - 多尺度空间折叠与冷色调净化 (Multi-Scale Organic Fluid Wake)

- **目标**：把过曝和紫色伪影压下去，同时确定整体的深海冷色视觉方向。

- **数学与光学重塑**：

  1. 引入 Schoen's Gyroid 极小曲面零等值面谐波切片作为基态相场：

$$\phi\_0(x, y, z) = \sin(k\_x x) \cos(k\_y y) + \sin(k\_y y) \cos(k\_z z) + \sin(k\_z z) \cos(k\_x x) = 0$$

2. 建立纯净深海光谱映射吸收曲线：

$$\mathbf{C}\_{\text{abyss}} = (0.002, 0.004, 0.008)^T, \quad \mathbf{C}\_{\text{deep}} = (0.020, 0.120, 0.280)^T, \quad \mathbf{C}\_{\text{cyan}} = (0.120, 0.680, 0.800)^T$$

- **当时的问题**：着色器本身没有时间记忆，手指一松开，画面就会很快恢复，缺少持续的运动反馈。

### 2.5 v5.0.0 - 高级黏性油膜撕裂引擎 (Advanced Viscous Oil Engine)

- **目标**：尝试加入双指靠近时的融合效果，以及高速划过时的撕裂效果。

- **遇到的问题**：

  引入 GPU 离屏 Ping-Pong 双缓冲系统（`RenderNode + HardwareRenderer + ImageReader`）。

- **为什么失败**：

  1. **双线性重采样的数值黏性耗散**：硬件双线性插值在 120 FPS 下于 10~15 帧内产生剧烈数值扩散，精细 Gyroid 纹理被彻底抹平成低频死水蓝。

  2. **非线性空间的平均失真**：色调映射后的非线性色彩被强行反向平流，色彩退化为中灰死色。

  3. **硬切割遮罩留下无法抚平的僵死黑痕**。

### 2.6 v5.1.0 - 物理流体模型重构 (Physical Hydrodynamic Manifold)

- **架构调整**：放弃离屏双缓冲，改用**拉格朗日历史点涡栈 + 亥姆霍兹-霍奇分解 + 连续代数剪切场**。

- **这一版主要解决的问题**：

  1. **亥姆霍兹-霍奇投影**：分离无旋场与有旋场，静止轻按仅产生径向高斯位移，旋度严格为零。

  2. **黏性 Lamb-Oseen 涡核**：解析求解二维不可压缩 Navier-Stokes 涡量输运方程，消除除零奇点。

  3. **拉格朗日点涡记忆栈（16 槽位）**：CPU 连续积分角动量 $\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \\, \mathrm{d}t$，保留 15~20 秒连续螺旋记忆。

  4. **代数 Sigmoid 连续剪切破裂**：改用代数有理分式平滑推开相边界，表面张力在 1.5~2.0 秒内自然闭合自愈。

### 2.7 v5.2.0 ~ v7.0.0 - 海洋生物阶段的建立与细化

- **v5.2.0 (Balanced Abyssal Ecology)**：开始把海洋生物加入渲染系统，使用泊松式散布避免个体过度重叠，并让每个个体可以独立响应触控。生物模型来自 **X 用户 @yuruyurau（#つぶやきProcessing）** 的原创作品。

- **v6.0.0 (High-Performance Engine Update)**：继续清理每帧对象分配，减少 120Hz 下的 GC 抖动；同时把部分距离计算内联，并把微型小水母从群体逻辑中独立出来。

- **v7.0.0 (Refined Engine & Sharp Rendering)**：恢复万点级点阵细节，给胖虫模型加入随时间变化的相位扫描，并重新调整深度分层和亮度衰减。

### 2.8 v8.0.0 - 运动方式重构与原生环游 (Kinematics & Native Orbit Update - 当前版本)

> **Credit:** v5.2.0 之后的海洋生物模型均基于 **@yuruyurau（#つぶやきProcessing）** 的原创作品。本版本中的改动主要集中在运动逻辑、交互响应、参数调整以及 Android/AGSL 实现。

- **经典长水母大半径回环（覆盖 50% 屏幕）**：

  移除外部叠加的垂直速度，回到原有的极坐标环形运动方程。通过放大回旋包络半径（`orbitScale = currentScale * 1.55f`），水母在屏幕中央约 50% 的区域内进行大半径回旋运动。受击时大幅加快内部生命时钟步进，让受击后的运动速度能够明显响应。

- **超长尾水母收缩冲程翻倍（2.0 倍）与时钟解耦**：

  将脉动喷水推力系数从 $0.081$ 提升至 $0.162$，并提升基准垂直航速。静态生命时钟步进由 $\pi / 260$ 减半为 $\pi / 520$，受刺激增速增益系数从 $0.75$ 缩减至 $0.1875$（仅为原来的 1/4），前进速度保持强劲，同时避免伞盖因时钟速度变化而出现明显抽搐。

- **真正水螅体数学拓扑独立**：

  与金色小水母使用完全独立的参数方程，保留条件分支（$e > 0$）和独立指数项的晶体羽状拓扑，使两种物种在形态上保持清晰区分。

- **统一自然 Ease-Out 阻尼体系**：

  所有生物统一取消瞬时冲量式的速度赋值（`surgeVx / surgeVy`），改用基于黏性阻尼的指数渐出衰减：

$$v(t) = v\_{\text{drift}} \cdot \mu\_{\text{motion}} \cdot \left( 1 + \Delta\_{\text{boost}} \cdot e^{-\lambda t} \right)$$

## 三、核心物理模型与数学基础

### 3.1 亥姆霍兹-霍奇分解（Helmholtz-Hodge Decomposition）

连续介质力学中速度矢量场 $\mathbf{u}(\mathbf{x})$ 满足正交正规分解：

$$\mathbf{u} = \mathbf{u}\_{\text{potential}} + \mathbf{u}\_{\text{solenoidal}} = -\nabla \Phi + \nabla \times \mathbf{A}$$

- **点击无旋势流场**：

$$\Phi(r) = \Phi\_0 \exp\left(-\frac{r^2}{2\sigma\_r^2}\right)$$

$$\mathbf{u}\_{\text{potential}} = -\nabla \Phi = \frac{\Phi\_0}{\sigma\_r^2} (\mathbf{x} - \mathbf{x}\_0) \exp\left(-\frac{\Vert{}\mathbf{x} - \mathbf{x}\_0\Vert{}^2}{2\sigma\_r^2}\right), \quad \nabla \times \mathbf{u}\_{\text{potential}} \equiv 0$$

- **手势有旋涡度场**：

$$\boldsymbol{\omega} = \nabla \times \mathbf{u}\_{\text{solenoidal}} = \left( \frac{\partial u\_y}{\partial x} - \frac{\partial u\_x}{\partial y} \right) \hat{\mathbf{z}} \neq 0$$

### 3.2 二维黏性 Lamb-Oseen 涡核严格解析解

不可压缩黏性流体的涡量输运方程：

$$\frac{\partial \omega}{\partial t} + (\mathbf{u} \cdot \nabla)\omega = \nu \nabla^2 \omega$$

轴对称假设下方程退化为线性热传导方程：

$$\frac{\partial \omega}{\partial t} = \nu \left( \frac{\partial^2 \omega}{\partial r^2} + \frac{1}{r} \frac{\partial \omega}{\partial r} \right)$$

狄拉克初始点源 $\omega(r, 0) = \Gamma \delta(\mathbf{x})$ 的基本解：

$$\omega(r, t) = \frac{\Gamma}{4\pi \nu (t + t\_0)} \exp\left(-\frac{r^2}{4\nu (t + t\_0)}\right)$$

切向流速积分结果：

$$u\_\theta(r, t) = \frac{\Gamma}{2\pi r} \left[ 1 - \exp\left(-\frac{r^2}{r\_{\text{core}}^2(t)}\right) \right]$$

当 $r \to 0$ 时求极限：

$$\lim\_{r \to 0} u\_\theta(r) = \lim\_{r \to 0} \frac{\Gamma}{2\pi r} \left[ 1 - \left( 1 - \frac{r^2}{r\_{\text{core}}^2} + \mathcal{O}(r^4) \right) \right] = 0$$

涡核中心呈现完全刚体转动，彻底根除数值奇点。

### 3.3 代数 Sigmoid 连续剪切破裂模型

沿划痕法线方向施加连续位移场：

$$\Delta \mathbf{x}\_{\text{shear}} = \mathbf{n}\_\perp \cdot \left[ \frac{d\_\perp}{\kappa + \vert{}d\_\perp\vert{}} \right] A \exp\left(-\frac{d\_\parallel^2}{\sigma\_\parallel^2} - \frac{d\_\perp^2}{\sigma\_\perp^2}\right)$$

### 3.4 深海生物全物种数学拓扑模型 (v8.0.0 规范)

#### 1. 经典长水母（LongJellyfish）原生环游方程

点阵规模 $N = 10000$：

$$k = 5 \cos\left(\frac{x\_{\text{val}}}{14}\right) \cos\left(\frac{y\_{\text{val}}}{30}\right), \quad e = \frac{y\_{\text{val}}}{8} - 13, \quad d = \frac{k^2 + e^2}{59} + 4$$

$$q = 60 - 3 \sin(\mathrm{atan2}(k, e) \cdot e) + k \left( 3 + \frac{4}{d} \sin(d^2 - 2t) \right)$$

$$c = \frac{d}{2} + \frac{e}{99} - \frac{t}{18}, \quad r\_x = q \sin(c) \cdot S\_{\text{orbit}}, \quad r\_y = \left( (q + 9d) \cos(c) + 65 \right) \cdot S\_{\text{orbit}}$$

其中 $S\_{\text{orbit}} = 1.55 \cdot S\_{\text{current}}$，实现占据屏幕约 50% 面积的自然环游。

#### 2. 超长尾水母（UltraLongJellyfish）双倍推进方程

点阵规模 $N = 8000$，静态时钟步进 $\omega\_{\text{base}} = \pi / 520$：

$$c = \frac{d^2}{7} - t, \quad q = 4 \sin(9 \mathrm{atan2}(k, e)) + 9 \sin(d - t) - \frac{k}{d} \left( 9 + 3 \sin(9d - 16t) \right)$$

$$r\_x = (q + 50 \cos(c)) \cdot S, \quad r\_y = (q \sin(c) + 45d - 160) \cdot S$$

推进加速度项：

$$a\_y = -\left( \max(0, \sin(t - 1.2)) \right)^2 \cdot 0.162 \cdot \mu\_{\text{motion}} \cdot (1 + \Delta\_{\text{boost}})$$

#### 3. 真正水螅体（SingleHydroid）晶体拓扑方程

点阵规模 $N = 3600$，采样约束 $e > 0$：

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(7i) \cos(i)$$

$$d = \frac{(k^2 + e^2)^{1.5}}{999} + 4.6 - \frac{\cos^3(t/4 + m)}{3}, \quad o = \sin(d^2 - t + m), \quad c = \frac{d}{8} - \frac{t}{32} + m$$

$$r\_x = \left( 99 \sin(c) + \frac{k}{3^o} \right) \cdot S, \quad r\_y = \left( 99 \cos\left(\frac{c}{3}\right) + 39d + e^o - 275 \right) \cdot S$$

#### 4. 迷你小水母（TinyJellyfish）切线速度对齐

点阵规模 $N = 3000$：

$$c = \frac{d}{16} - \frac{t}{48} + m, \quad r\_x = (99 \sin(c) + k \cdot p\_{\text{val}}) \cdot S, \quad r\_y = (99 \sin(4c) + e \cdot p\_{\text{val}}) \cdot S$$

其宏观空间航向速度严格跟随轨迹一阶微分导数：

$$\mathbf{v} = \frac{\mathbf{v}\_{\text{instant}}}{\Vert{}\mathbf{v}\_{\text{instant}}\Vert{}} \cdot v\_{\text{cruise}} = \frac{\left( -2.06 \cos(c), -8.25 \cos(4c) \right)^T}{\sqrt{4.24 \cos^2(c) + 68.06 \cos^2(4c)}} \cdot v\_{\text{cruise}}$$

## 四、核心架构

```text
com.agsl.wallpaper/
├── MainActivity.kt                # 引擎入口与壁纸预览设置交互
├── JellyfishWallpaperService.kt   # LiveWallpaperService 服务端与渲染循环
├── FastMath.kt                    # 高性能查找表与快速三角/指数数学库
├── AbyssalOrganism.kt             # 生态基类：流体阻尼、Ease-Out 动力学衰减、分层绘制
├── PlumpBug.kt                    # 金粉胖虫：时变行波微扰金粉流光
├── LongJellyfish.kt               # 经典长水母：原生 50% 屏幕大半径回旋环游
├── UltraLongJellyfish.kt          # 超长尾水母：双倍推进冲程、解耦悠缓时钟
├── SmallJellyfish.kt              # 金色经典小水母：高频径向谐波振荡
├── SingleHydroid.kt               # 真正水螅体：独立条件极坐标晶体拓扑
├── TinyJellyfish.kt               # 迷你小水母：解耦单体、2.5倍尺度、李萨如切线导数对齐
└── JellyfishEcology.kt            # 生态管理器：泊松离散散布、图层排序、精准单选碰撞

```

### 4.1 生态动力学抽象基类 (`AbyssalOrganism.kt`)

```kotlin
package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

abstract class AbyssalOrganism(
    var x: Float,
    var y: Float,
    var scaleFactor: Float,
    val baseSpeed: Float,
    val speciesId: Int,
    val rgb: IntArray,
    var driftVx: Float,
    var driftVy: Float,
    var t: Float,
    var layerIndex: Int = 0
) {
    val pts = FloatArray(24000)
    var validPointCount = 0
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    var isOffscreen = false
    var escapeBoost = 0f
    var startlePulse = 0f

    val motionScale = 0.28f

    open fun getPointHitRadius(baseScale: Float): Float = (70f * baseScale).coerceIn(40f, 150f)

    open fun hitTest(tx: Float, ty: Float, baseScale: Float): Float {
        val dx = tx - x
        val dy = ty - y
        val r = getPointHitRadius(baseScale)
        val d2 = dx * dx + dy * dy
        return if (d2 <= r * r) d2 else Float.MAX_VALUE
    }

    open fun applyImpulse() {
        escapeBoost = (escapeBoost + 1.8f).coerceAtMost(3.5f)
        startlePulse = 1.0f
    }

    open fun clearReaction() {}

    open fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        t += baseSpeed * (1f + escapeBoost * 0.5f) * frameFactor

        val margin = 320f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    abstract fun evaluateShape(currentScale: Float): Int

    open fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 35f + layerIndex * 6.0f
        val alpha = (baseIdleAlpha + startlePulse * 50f).coerceIn(baseIdleAlpha, 220f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    protected inline fun addPoint(ptr: Int, px: Float, py: Float, maxR2: Float): Int {
        val dx = px - x
        val dy = py - y
        if (dx * dx + dy * dy < maxR2 && ptr + 1 < pts.size) {
            pts[ptr] = px
            pts[ptr + 1] = py
            return ptr + 2
        }
        return ptr
    }
}

```

## 五、编译与发布

### 5.1 环境要求

- **Android SDK**: `minSdkVersion 33` (Android 13 Tiramisu), `targetSdkVersion 34`

- **JDK Version**: OpenJDK 17 或 21

- **Gradle Tooling**: Gradle 8.5+, Android Gradle Plugin 8.3.0+

### 5.2 编译流程

```bash
# 1. 克隆代码仓库
git clone https://github.com/Defei-Wang/agsl-fluid-wallpaper.git
cd agsl-fluid-wallpaper

# 2. 编译调试版 APK
./gradlew assembleDebug

# 3. 安装到已连接设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. 启动壁纸主服务
adb shell am start -n com.agsl.wallpaper/.MainActivity

```

### 5.3 官方发布仓库与 Release 归档

项目代码与二进制安装包统一发布于 GitHub 官方仓库：

[https://github.com/Defei-Wang/agsl-fluid-wallpaper](https://github.com/Defei-Wang/agsl-fluid-wallpaper)

所有发布构建产物可通过 GitHub Releases 页面直接下载：

[https://github.com/Defei-Wang/agsl-fluid-wallpaper/releases](https://github.com/Defei-Wang/agsl-fluid-wallpaper/releases)
