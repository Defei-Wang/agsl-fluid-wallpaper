# AGSL Fluid Wallpaper Engine (v1.0.0 ~ v8.0.0)

## 高性能物理流体力学与深海生物自组织运动学生态引擎[cite: 2]

[English Documentation](https://www.google.com/search?q=%23english-documentation) | [中文完整技术文档与全版本演进白皮书](https://www.google.com/search?q=%23%E4%B8%AD%E6%96%87%E5%AE%8C%E6%95%B4%E6%8A%80%E6%9C%AF%E6%96%87%E6%A1%A3%E4%B8%8E%E5%85%A8%E7%89%88%E6%9C%AC%E6%BC%94%E8%BF%9B%E7%99%BD%E7%9A%AE%E4%B9%A6)

---

# English Documentation

## 1. Project Overview & Architectural Philosophy

The **AGSL Fluid Wallpaper Engine** is an open-source, procedural, hardware-accelerated live wallpaper engine engineered specifically for Android 13+ (API level 33+) using the **Android Graphics Shading Language (AGSL)**, Skia's `RuntimeShader`, and hardware-accelerated Canvas pipelines[cite: 2].

The system encompasses two distinct architectural eras:

1. **Hydrodynamic Fluid Manifold Era (v1.0.0 ~ v5.1.0)**: Operates on a **Hybrid Lagrangian-Analytic Kinematic Manifold**[cite: 2]. It completely decouples physical vorticity integration on the CPU from continuous analytical coordinate advection on the GPU, achieving zero external texture allocations and sustained 120 FPS performance on flagship displays (e.g., Samsung Galaxy Tab S9)[cite: 2].
2. **Abyssal Ecological Kinematics Era (v5.2.0 ~ v8.0.0)**: Evolves into a multi-species procedural marine ecosystem. It transitions from rigid coordinate translation to intrinsic differential geometry, native topological orbit cruising, and unified ease-out fluid damping.

### Key Architectural Benchmarks

* **Zero Framebuffer Memory Overhead**: Zero external texture allocation; no render-target Ping-Pong buffers[cite: 2].
* **120 FPS Sustained Fluidity**: Native refresh rate performance on flagship displays with minimal thermal footprint[cite: 2].
* **Physical Causal Memory**: Preserves rotational winding and multi-scale vortex shear across a physical relaxation window of 15~20 seconds without numerical dissipation[cite: 2].
* **Procedural Morphological Topology**: Models marine life entirely via analytical coordinate mappings without static polygonal meshes or sprite sheets.

---

## 2. Complete Version Iteration Chronology (v1.0.0 ~ v8.0.0)

| Version | Milestone Title | Core Physical & Mathematical Innovations | Limitations & Iterative Motives |
| --- | --- | --- | --- |
| **v1.0.0** | Ambient Center Engine | 2D steady conformal flow mapping; complex potential $W(z) = \phi + i\psi$; single-center harmonic interference[cite: 2]. | Single touch only; static coordinate displacement; zero fluid viscosity or velocity sensitivity[cite: 2]. |
| **v2.0.0** | Discrete Point-Source Engine | Hydrodynamic dipole flow field; multi-touch tracking; streamline superposition[cite: 2]. | Interaction strength remained independent of gesture velocity; lack of momentum accumulation[cite: 2]. |
| **v3.0.0** | Causal Segment Wake Wave Engine | Dispersive gravity-capillary surface wave model; Kelvin wedge angle $\theta = \arcsin(1/3) \approx 19.47^\circ$; touch velocity differentiation[cite: 2]. | High-saturation specular hotspots; unnatural additive purple/magenta glare under prolonged touch[cite: 2]. |
| **v4.0.0** | Multi-Scale Organic Fluid Wake | Schoen's Gyroid minimal surface nodal approximation; logarithmic spiral coordinate mapping $\theta(r) = a \ln(r)$; non-linear tonemapping; cold-tone purification[cite: 2]. | Stateless single-pass shader; zero temporal memory (fluid instantly snapped back upon finger release)[cite: 2]. |
| **v5.0.0** | Advanced Viscous Oil Engine | Droplet proximity tension fusion; high-velocity slicing groove detection; attempted GPU off-screen Ping-Pong feedback buffer[cite: 2]. | **Severe numerical diffusion**: hardware bilinear filtering blurred Gyroid patterns into murky sludge within 10 frames[cite: 2]. |
| **v5.1.0** | Physical Hydrodynamic Manifold | Helmholtz-Hodge decomposition (curl-free potential tap vs. solenoidal vorticity); Lamb-Oseen finite viscous vortex core; 16-slot CPU Lagrangian vortex history stack; algebraic sigmoid continuous shear slicing[cite: 2]. | Shift from continuous single-phase fluid to discrete biological morphogenesis in abyssal environments. |
| **v5.2.0** | Balanced Abyssal Ecology | Initial transition to abyssal marine ecology; Poisson scattered spawn distribution; independent touch collision detection. | Early mathematical models exhibited rigid translation; multiple organisms triggered simultaneously on touch. |
| **v6.0.0** | Ethereal Abyssal Ecology (High-Performance Engine) | Zero-allocation render pipeline; continuous tentacle morphology; inlined Euclidean distance math; low-saturation near-white stippling; decoupled small jellyfish. | Species shared overlapping mathematical topologies; organism movement lacked natural fluid resistance. |
| **v7.0.0** | Refined Engine & Sharp Rendering | Restored razor-sharp 10,000-point tentacle details; time-varying phase scanning for golden worm stippling; 5-layer depth system with luminance attenuation. | Medium and ultra-long jellyfish suffered from artificial vertical velocity injection on touch ("spring jump"). |
| **v8.0.0** | Kinematics & Native Orbit Update | Restored native large-radius orbit cruising for medium jellyfish (covering 50% screen); doubled ultra-long jellyfish contraction stroke with decoupled clock; isolated true hydroid mathematical topology; unified ease-out damping. | Production baseline for deep-sea ecological live wallpaper. |

---

## 3. Mathematical Foundations by Version

### 3.1 v1.0.0 ~ v2.0.0: Potential Flow & Dipole Mechanics

Early iterations relied on planar ideal potential flow[cite: 2]. A point source or sink at $z_0 = x_0 + i y_0$ is defined by the complex potential[cite: 2]:

$$W(z) = \Phi(x, y) + i \Psi(x, y) = \frac{m}{2\pi} \ln(z - z_0)$$

For dual touches (v2.0.0), a hydrodynamic dipole pair was introduced[cite: 2]:

$$W_{\text{dipole}}(z) = \frac{\boldsymbol{\mu} \cdot (z - z_0)}{2\pi \vert{}z - z_0\vert{}^2}$$

### 3.2 v3.0.0: Dispersive Kelvin Wakes

To introduce velocity-dependent ripples, v3.0.0 incorporated the kinematic wave dispersion relation[cite: 2]:

$$\omega^2(k) = gk + \frac{\gamma}{\rho} k^3$$

A moving disturbance generates an envelope bounded by the Kelvin wedge angle[cite: 2]:

$$\alpha = \arcsin(1/3) \approx 19.47^\circ$$

The velocity vector was calculated via historical gesture differentiation[cite: 2]:

$$\mathbf{v}(t) = \frac{\mathbf{x}(t) - \mathbf{x}(t - \Delta t)}{\Delta t}$$

### 3.3 v4.0.0: Gyroid Minimal Surface & Color Metric

v4.0.0 established the continuous baseline manifold via Schoen's Gyroid minimal surface nodal approximation[cite: 2]:

$$\phi_0(\mathbf{x}) = \sin(k_x x) \cos(k_y y) + \sin(k_y y) \cos(k_z z) + \sin(k_z z) \cos(k_x x) = 0$$

The color metric was restructured to an exponential absorption spectrum[cite: 2]:

$$\mathbf{C}_{\text{final}} = \mathbf{1} - \exp(-\mathbf{C}_{\text{linear}} \cdot \gamma)$$

### 3.4 v5.0.0 vs. v5.1.0: Lagrangian Mechanics vs. Eulerian Dissipation

The attempted v5.0.0 off-screen Ping-Pong buffer sampled the previous frame via[cite: 2]:

$$I(t + \Delta t, \mathbf{x}) = I(t, \mathbf{x} - \mathbf{u} \Delta t)$$

Hardware bilinear interpolation introduced compounding numerical artificial diffusion[cite: 2]:

$$\nu_{\text{num}} \approx \frac{\Vert{}\mathbf{u}\Vert{} \Delta x}{2} - \frac{\Vert{}\mathbf{u}\Vert{}^2 \Delta t}{2}$$

This blurred all high-frequency Gyroid harmonics within 10 frames[cite: 2].

**v5.1.0 Solution**:

1. **Helmholtz-Hodge Decomposition**[cite: 2]:

$$\mathbf{u} = \mathbf{u}_{\text{potential}} + \mathbf{u}_{\text{solenoidal}}, \quad \nabla \times \mathbf{u}_{\text{potential}} = \mathbf{0}, \quad \nabla \cdot \mathbf{u}_{\text{solenoidal}} = 0$$

2. **Viscous Lamb-Oseen Vortex Core**[cite: 2]:

$$u_\theta(r, t) = \frac{\Gamma}{2\pi r} \left[ 1 - \exp\left(-\frac{r^2}{r_{\text{core}}^2(t)}\right) \right], \quad \lim_{r \to 0} u_\theta(r) = 0$$

3. **Lagrangian Vortex History**[cite: 2]:

$$\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \, \mathrm{d}t, \quad \mathbf{x}' = \mathbf{x}_k + \mathbf{R}(\Delta \theta_k) (\mathbf{x} - \mathbf{x}_k)$$

4. **Algebraic Sigmoid Continuous Shear**[cite: 2]:

$$\Delta \mathbf{x}_{\text{shear}} = \mathbf{n}_\perp \left[ \frac{d_\perp}{\kappa + \vert{}d_\perp\vert{}} \right] A \exp\left(-\frac{d_\parallel^2}{\sigma_\parallel^2} - \frac{d_\perp^2}{\sigma_\perp^2}\right)$$

### 3.5 v6.0.0 ~ v8.0.0: Abyssal Ecological Manifolds & Kinematics

Starting in v5.2.0 and perfected in v8.0.0, the codebase implements five distinct abyssal organisms governed by parametric mathematical topology:

#### 1. Plump Bug (Golden Stippling Hydrodynamic Beetle)

Preserves continuous surface shimmering by injecting traveling wave phase $t \cdot 0.2$ into polar envelope equations:

$$k = \cos(9 y_{\text{val}} + 0.2 t) \cdot \begin{cases} 28 \sin(t + y_{\text{val}}), & y_{\text{val}} < 9 \\ 11, & y_{\text{val}} \ge 9 \end{cases}$$

$$e = \frac{y_{\text{val}}}{8} - 13, \quad o = \frac{\sqrt{k^2 + e^2}}{6}$$

$$q = \frac{k y_{\text{val}}}{15} + 79 + k \sin(y_{\text{val}}) \left( 1 + \sin(4o - e - 8t) \right)$$

$$c = \frac{o}{2} - \frac{e}{4} - t, \quad r_x = q \sin(c) + 70 \sin\left(\frac{c}{3}\right), \quad r_y = \frac{q}{0.7} \cos(c)$$

#### 2. Classic Long Jellyfish (Native Large-Radius Orbit Cruising)

In v8.0.0, external linear translation is completely eliminated. The organism cruises along its native circular orbit spanning 50% of the screen:

$$k = 5 \cos\left(\frac{x_{\text{val}}}{14}\right) \cos\left(\frac{y_{\text{val}}}{30}\right), \quad e = \frac{y_{\text{val}}}{8} - 13, \quad d = \frac{k^2 + e^2}{59} + 4$$

$$q = 60 - 3 \sin(\operatorname{atan2}(k, e) \cdot e) + k \left( 3 + \frac{4}{d} \sin(d^2 - 2t) \right)$$

$$c = \frac{d}{2} + \frac{e}{99} - \frac{t}{18}, \quad r_x = q \sin(c), \quad r_y = (q + 9d) \cos(c) + 65$$

#### 3. Ultra-Long Jellyfish (Doubled Forward Thrust & Clock Decoupling)

Forward displacement is coupled directly to bell contraction strokes with doubled propulsion power ($0.162$):

$$k = 5 \cos\left(\frac{x_{\text{val}}}{19}\right) \cos\left(\frac{y_{\text{val}}}{30}\right), \quad e = \frac{y_{\text{val}}}{8} - 12, \quad d = \frac{k^2 + e^2}{59} + 2$$

$$c = \frac{d^2}{7} - t, \quad q = 4 \sin(9 \operatorname{atan2}(k, e)) + 9 \sin(d - t) - \frac{k}{d} \left( 9 + 3 \sin(9d - 16t) \right)$$

$$F_{\text{thrust}} = \left( \max(0, \sin(t - 1.2)) \right)^2 \cdot 0.162$$

#### 4. Golden Small Jellyfish (Classic Harmonic Oscillator)

Operates on high-frequency radial harmonic oscillation:

$$k = 2 \cos(342 i), \quad e = 2 \sin(271 i), \quad d = \frac{\sqrt{k^2 + e^2}}{1.6}$$

$$pp = 5 + 2 \sin(8d - 3t + m), \quad c = \frac{d^2}{9} - \frac{t}{8} + m$$

$$r_x = k \cdot pp + \frac{9}{d} \sin(2k) + 89 \sin(c), \quad r_y = 79 \sin(2c) + \frac{9}{d} \sin(2e) + e \cdot pp$$

#### 5. True Single Hydroid (Decoupled Polar Crystalline Topology)

Separated from the Small Jellyfish model in v8.0.0; governed by an independent conditional equation ($e > 0$):

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(7i) \cos(i), \quad (e > 0)$$

$$d = \frac{(k^2 + e^2)^{1.5}}{999} + 4.6 - \frac{\cos^3(t/4 + m)}{3}, \quad o = \sin(d^2 - t + m), \quad c = \frac{d}{8} - \frac{t}{32} + m$$

$$r_x = 99 \sin(c) + \frac{k}{3^o}, \quad r_y = 99 \cos\left(\frac{c}{3}\right) + 39d + e^o - 275$$

#### 6. Microscopic Tiny Jellyfish (Kinematic Tangent Velocity Alignment)

Decoupled single individual scaled 2.5x to exceed Hydroid dimensions, swimming along its instantaneous Lissajous tangent derivative:

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(3i) \cos(2i), \quad d = \frac{(k^2 + e^2)^{1.5}}{1999} + 1.5 - \frac{\sin^3(t/2 + m)}{3}$$

$$p_{\text{val}} = \exp\left( \sin(d^2 - t + m) \ln(\max(d, 0.05)) \right), \quad c = \frac{d}{16} - \frac{t}{48} + m$$

$$\mathbf{v}_{\text{instant}} = \left( -2.06 \cos(c), -8.25 \cos(4c) \right)^T$$

---

# 中文完整技术文档与全版本演进白皮书

## 一、 项目定位与系统架构哲学

在 Android 系统生态中，动态壁纸面临两极化的体验瓶颈：

1. **轻量但生硬的伪动画**：采用预渲染视频流或简单正弦位移贴图，无物理因果反馈，交互机械呆板[cite: 2]。
2. **重型数值网格解法（Eulerian Grid Solver）**：强行在移动端运行不可压缩 Navier-Stokes 偏微分求解器，依赖多级 FBO 进行速度场、压力泊松方程的 Ping-Pong 迭代，导致显存带宽过载与设备发热[cite: 2]。

**AGSL Fluid Wallpaper Engine** 历经两个发展时期：

* **v1.0.0 ~ v5.1.0**：**混合拉格朗日-解析微分流形**。CPU 以极低开销积分点涡环量与角动量，GPU 在片元着色器实时求值，达成零纹理显存分配与 120 FPS 满帧流体自组织演化[cite: 2]。
* **v5.2.0 ~ v8.0.0**：**深海微生态动力学生态体系**。通过纯数学点阵微分拓扑取代静态多边形网格，构建涵盖金粉胖虫、原生大半径环游长水母、双倍推力超长水母、金色小水母、独立拓扑水螅体及切线对齐迷你水母的完整自洽生态。

---

## 二、 全版本演进脉络与技术心路复盘 (v1.0.0 ~ v8.0.0)

### 2.1 v1.0.0 - 基础对流共形引擎 (Ambient Center Engine)

* **核心目标**：验证 Android 13 原生 AGSL `RuntimeShader` 与壁纸服务 `SurfaceHolder.lockHardwareCanvas()` 的渲染性能极限[cite: 2]。
* **力学模型**：采用二维稳态复变共形映射[cite: 2]：

$$W(z) = \Phi(x, y) + i \Psi(x, y) = \frac{m}{2\pi} \ln(z - z_0)$$

* **局限反思**：仅支持单点交互；位移场为稳态解析几何，缺乏流体惯性与手速反馈[cite: 2]。

---

### 2.2 v2.0.0 - 离散点源与双指拓扑交互 (Discrete Point-Source Engine)

* **核心目标**：拓展多点触控支持，模拟双指在流体中形成的偶极喷流与拓扑鞍点[cite: 2]。
* **力学模型**：构造反向偶极子流场[cite: 2]：

$$W_{\text{dipole}}(z) = \frac{\boldsymbol{\mu} \cdot (z - z_0)}{2\pi \vert{}z - z_0\vert{}^2}$$

* **局限反思**：交互响应强度与手势速度脱节，快划与慢拖体感完全一致[cite: 2]。

---

### 2.3 v3.0.0 - 因果线段开尔文尾迹引擎 (Causal Segment Wake Wave Engine)

* **核心目标**：引入真实手速物理采样，模拟移动扰动产生的开尔文重力波尾迹[cite: 2]。
* **力学模型**：
1. 通过 `MotionEvent.getHistoricalX/Y` 计算真实手速微分[cite: 2]：



$$\mathbf{v}(t) = \frac{\mathbf{x}(t) - \mathbf{x}(t - \Delta t)}{\Delta t}$$

2. 构造开尔文波包夹角包络[cite: 2]：

$$\theta_{\text{Kelvin}} = \arcsin(1/3) \approx 19.47^\circ$$

* **局限反思**：波形叠加过密导致局部高曝光白斑频发，按住不放时产生刺眼的洋红色杂光伪影[cite: 2]。

---

### 2.4 v4.0.0 - 多尺度空间折叠与冷色调净化 (Multi-Scale Organic Fluid Wake)

* **核心目标**：彻底消除紫光与过曝白斑，建立深海冷色调视觉规范[cite: 2]。
* **数学与光学重塑**：
1. 引入 Schoen's Gyroid 极小曲面零等值面谐波切片作为基态相场[cite: 2]：



$$\phi_0(x, y, z) = \sin(k_x x) \cos(k_y y) + \sin(k_y y) \cos(k_z z) + \sin(k_z z) \cos(k_x x) = 0$$

2. 建立纯净深海光谱映射吸收曲线[cite: 2]：

$$\mathbf{C}_{\text{abyss}} = (0.002, 0.004, 0.008)^T, \quad \mathbf{C}_{\text{deep}} = (0.020, 0.120, 0.280)^T, \quad \mathbf{C}_{\text{cyan}} = (0.120, 0.680, 0.800)^T$$

* **局限反思**：着色器为纯无状态解析函数，手松开后画面瞬间复原，缺乏旋涡因果记忆[cite: 2]。

---

### 2.5 v5.0.0 - 高级黏性油膜撕裂引擎 (Advanced Viscous Oil Engine)

* **核心目标**：实现双指靠近时的表面张力融合以及高速划切时的油层破裂[cite: 2]。
* **重大挫折与架构灾难**：
引入 GPU 离屏 Ping-Pong 双缓冲系统（`RenderNode + HardwareRenderer + ImageReader`）[cite: 2]。
* **失败机理剖析**：
1. **双线性重采样的数值黏性耗散**：硬件双线性插值在 120 FPS 下于 10~15 帧内产生剧烈数值扩散，精细 Gyroid 纹理被彻底抹平成低频死水蓝[cite: 2]。
2. **非线性空间的平均失真**：色调映射后的非线性色彩被强行反向平流，色彩退化为中灰死色[cite: 2]。
3. **硬切割遮罩留下无法抚平的僵死黑痕**[cite: 2]。



---

### 2.6 v5.1.0 - 物理流体力学流形重构 (Physical Hydrodynamic Manifold)

* **架构涅槃**：废除离屏双缓冲，建立**拉格朗日历史点涡栈 + 亥姆霍兹投影 + 连续代数剪切场**[cite: 2]。
* **核心物理突破**：
1. **亥姆霍兹-霍奇投影**：分离无旋场与有旋场，静止轻按仅产生径向高斯位移，旋度严格为零[cite: 2]。
2. **黏性 Lamb-Oseen 涡核**：解析求解二维不可压缩 Navier-Stokes 涡量输运方程，消除除零奇点[cite: 2]。
3. **拉格朗日点涡记忆栈（16 槽位）**：CPU 连续积分角动量 $\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \, \mathrm{d}t$，保留 15~20 秒连续螺旋记忆[cite: 2]。
4. **代数 Sigmoid 连续剪切破裂**：改用代数有理分式平滑推开相边界，表面张力在 1.5~2.0 秒内自然闭合自愈[cite: 2]。



---

### 2.7 v5.2.0 ~ v7.0.0 - 深海生态演化与高分辨率微扰

* **v5.2.0 (Balanced Abyssal Ecology)**：引入海洋生物生态群落概念，加入泊松分布防挤压生成采样，实现独立个体触控响应。
* **v6.0.0 (High-Performance Engine Update)**：剔除每帧临时对象分配，消除 120Hz 下的 GC 停顿；使用内联欧氏距离计算；实现微型小水母解耦。
* **v7.0.0 (Refined Engine & Sharp Rendering)**：恢复万点级高精点阵渲染；在胖虫数学方程中注入时变相位扫描行波，使金粉流光覆盖全域；建立五层深度亮度衰减系统。

---

### 2.8 v8.0.0 - 运动学拓扑重构与原生环游基线 (Kinematics & Native Orbit Update - 当前版本)

* **经典长水母大半径回环（覆盖 50% 屏幕）**：
彻底废除死板的外部垂直速度叠加，完全回归原作者的极坐标环形流动方程。通过放大回旋包络半径（`orbitScale = currentScale * 1.55f`），水母在屏幕中央 50% 区域内作优雅、大范围的流线型回旋游弋。受击时大幅加快内部生命时钟步进，解决点击无加速反馈的问题。
* **超长尾水母收缩冲程翻倍（2.0 倍）与时钟解耦**：
将脉动喷水推力系数从 $0.081$ 提升至 $0.162$，并提升基准垂直航速。静态生命时钟步进由 $\pi / 260$ 减半为 $\pi / 520$，受刺激增速增益系数从 $0.75$ 缩减至 $0.1875$（仅为原来的 1/4），前进速度保持强劲，彻底消除伞盖剧烈抽搐的问题。
* **真正水螅体数学拓扑独立**：
彻底剥离与金色小水母重叠的参数方程，完全恢复原版包含条件分支（$e > 0$）与独立指数项的晶体羽状拓扑方程，配合清透冰晶水青配色，杜绝物种混淆。
* **统一自然 Ease-Out 阻尼体系**：
全生物域废除瞬时冲量赋值（`surgeVx / surgeVy`），统一重构为基于流体黏性阻尼的指数渐出衰减：

$$v(t) = v_{\text{drift}} \cdot \mu_{\text{motion}} \cdot \left( 1 + \Delta_{\text{boost}} \cdot e^{-\lambda t} \right)$$

---

## 三、 核心物理偏微分方程与拓扑流形推导

### 3.1 亥姆霍兹-霍奇分解（Helmholtz-Hodge Decomposition）

连续介质力学中速度矢量场 $\mathbf{u}(\mathbf{x})$ 满足正交正规分解[cite: 2]：

$$\mathbf{u} = \mathbf{u}_{\text{potential}} + \mathbf{u}_{\text{solenoidal}} = -\nabla \Phi + \nabla \times \mathbf{A}$$

* **点击无旋势流场**[cite: 2]：

$$\Phi(r) = \Phi_0 \exp\left(-\frac{r^2}{2\sigma_r^2}\right)$$

$$\mathbf{u}_{\text{potential}} = -\nabla \Phi = \frac{\Phi_0}{\sigma_r^2} (\mathbf{x} - \mathbf{x}_0) \exp\left(-\frac{\Vert{}\mathbf{x} - \mathbf{x}_0\Vert{}^2}{2\sigma_r^2}\right), \quad \nabla \times \mathbf{u}_{\text{potential}} \equiv 0$$

* **手势有旋涡度场**[cite: 2]：

$$\boldsymbol{\omega} = \nabla \times \mathbf{u}_{\text{solenoidal}} = \left( \frac{\partial u_y}{\partial x} - \frac{\partial u_x}{\partial y} \right) \hat{\mathbf{z}} \neq 0$$

---

### 3.2 二维黏性 Lamb-Oseen 涡核严格解析解

不可压缩黏性流体的涡量输运方程[cite: 2]：

$$\frac{\partial \omega}{\partial t} + (\mathbf{u} \cdot \nabla)\omega = \nu \nabla^2 \omega$$

轴对称假设下方程退化为线性热传导方程[cite: 2]：

$$\frac{\partial \omega}{\partial t} = \nu \left( \frac{\partial^2 \omega}{\partial r^2} + \frac{1}{r} \frac{\partial \omega}{\partial r} \right)$$

狄拉克初始点源 $\omega(r, 0) = \Gamma \delta(\mathbf{x})$ 的基本解[cite: 2]：

$$\omega(r, t) = \frac{\Gamma}{4\pi \nu (t + t_0)} \exp\left(-\frac{r^2}{4\nu (t + t_0)}\right)$$

切向流速积分结果[cite: 2]：

$$u_\theta(r, t) = \frac{\Gamma}{2\pi r} \left[ 1 - \exp\left(-\frac{r^2}{r_{\text{core}}^2(t)}\right) \right]$$

当 $r \to 0$ 时求极限[cite: 2]：

$$\lim_{r \to 0} u_\theta(r) = \lim_{r \to 0} \frac{\Gamma}{2\pi r} \left[ 1 - \left( 1 - \frac{r^2}{r_{\text{core}}^2} + \mathcal{O}(r^4) \right) \right] = 0$$

涡核中心呈现完全刚体转动，彻底根除数值奇点[cite: 2]。

---

### 3.3 代数 Sigmoid 连续剪切破裂模型

沿划痕法线方向施加连续位移场[cite: 2]：

$$\Delta \mathbf{x}_{\text{shear}} = \mathbf{n}_\perp \cdot \left[ \frac{d_\perp}{\kappa + \vert{}d_\perp\vert{}} \right] A \exp\left(-\frac{d_\parallel^2}{\sigma_\parallel^2} - \frac{d_\perp^2}{\sigma_\perp^2}\right)$$

---

### 3.4 深海生物全物种数学拓扑模型 (v8.0.0 规范)

#### 1. 经典长水母（LongJellyfish）原生环游方程

点阵规模 $N = 10000$：

$$k = 5 \cos\left(\frac{x_{\text{val}}}{14}\right) \cos\left(\frac{y_{\text{val}}}{30}\right), \quad e = \frac{y_{\text{val}}}{8} - 13, \quad d = \frac{k^2 + e^2}{59} + 4$$

$$q = 60 - 3 \sin(\operatorname{atan2}(k, e) \cdot e) + k \left( 3 + \frac{4}{d} \sin(d^2 - 2t) \right)$$

$$c = \frac{d}{2} + \frac{e}{99} - \frac{t}{18}, \quad r_x = q \sin(c) \cdot S_{\text{orbit}}, \quad r_y = \left( (q + 9d) \cos(c) + 65 \right) \cdot S_{\text{orbit}}$$

其中 $S_{\text{orbit}} = 1.55 \cdot S_{\text{current}}$，实现占据屏幕约 50% 面积的自然环游。

#### 2. 超长尾水母（UltraLongJellyfish）双倍推进方程

点阵规模 $N = 8000$，静态时钟步进 $\omega_{\text{base}} = \pi / 520$：

$$c = \frac{d^2}{7} - t, \quad q = 4 \sin(9 \operatorname{atan2}(k, e)) + 9 \sin(d - t) - \frac{k}{d} \left( 9 + 3 \sin(9d - 16t) \right)$$

$$r_x = (q + 50 \cos(c)) \cdot S, \quad r_y = (q \sin(c) + 45d - 160) \cdot S$$

推进加速度项：

$$a_y = -\left( \max(0, \sin(t - 1.2)) \right)^2 \cdot 0.162 \cdot \mu_{\text{motion}} \cdot (1 + \Delta_{\text{boost}})$$

#### 3. 真正水螅体（SingleHydroid）晶体拓扑方程

点阵规模 $N = 3600$，采样约束 $e > 0$：

$$k = 9 \cos(5i) \sin(i), \quad e = 9 \cos(7i) \cos(i)$$

$$d = \frac{(k^2 + e^2)^{1.5}}{999} + 4.6 - \frac{\cos^3(t/4 + m)}{3}, \quad o = \sin(d^2 - t + m), \quad c = \frac{d}{8} - \frac{t}{32} + m$$

$$r_x = \left( 99 \sin(c) + \frac{k}{3^o} \right) \cdot S, \quad r_y = \left( 99 \cos\left(\frac{c}{3}\right) + 39d + e^o - 275 \right) \cdot S$$

#### 4. 迷你小水母（TinyJellyfish）切线速度对齐

点阵规模 $N = 3000$：

$$c = \frac{d}{16} - \frac{t}{48} + m, \quad r_x = (99 \sin(c) + k \cdot p_{\text{val}}) \cdot S, \quad r_y = (99 \sin(4c) + e \cdot p_{\text{val}}) \cdot S$$

其宏观空间航向速度严格跟随轨迹一阶微分导数：

$$\mathbf{v} = \frac{\mathbf{v}_{\text{instant}}}{\Vert{}\mathbf{v}_{\text{instant}}\Vert{}} \cdot v_{\text{cruise}} = \frac{\left( -2.06 \cos(c), -8.25 \cos(4c) \right)^T}{\sqrt{4.24 \cos^2(c) + 68.06 \cos^2(4c)}} \cdot v_{\text{cruise}}$$

---

## 四、 核心架构全景设计

```
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

---

## 五、 规范编译与发布指南

### 5.1 环境要求

* **Android SDK**: `minSdkVersion 33` (Android 13 Tiramisu), `targetSdkVersion 34`
* **JDK Version**: OpenJDK 17 或 21
* **Gradle Tooling**: Gradle 8.5+, Android Gradle Plugin 8.3.0+

### 5.2 终端编译流程

```bash
# 1. 克隆代码仓库
git clone `https://github.com/Defei-Wang/agsl-fluid-wallpaper.git`
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
`[https://github.com/Defei-Wang/agsl-fluid-wallpaper](https://github.com/Defei-Wang/agsl-fluid-wallpaper)`

所有发布构建产物可通过 GitHub Releases 页面直接下载：
`[https://github.com/Defei-Wang/agsl-fluid-wallpaper/releases](https://github.com/Defei-Wang/agsl-fluid-wallpaper/releases)`
