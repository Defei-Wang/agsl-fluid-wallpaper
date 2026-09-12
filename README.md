# AGSL Fluid Wallpaper Engine | AGSL 高性能物理流体动态壁纸引擎

[English](#english) | [中文说明](#中文说明)

---

<a name="english"></a>
## English

### Overview
A procedural, hardware-accelerated Android live wallpaper engine engineered with AGSL (Android Graphics Shading Language) `RuntimeShader`. The engine renders a self-organizing thin-film fluid manifold operating on pure analytical differential geometry and discrete Lagrangian vortex mechanics, achieving zero frame-buffer allocation and 120 FPS performance on Android 13+ devices.

---

### Mathematical & Physical Foundations

#### 1. Gyroid Minimal Surface Manifold (Base Phase Field)
The baseline equilibrium structure is derived from the first-order nodal approximation of Schoen's Gyroid minimal surface:
$$\phi_0(\mathbf{x}) = \sin(k_x x)\cos(k_y y) + \sin(k_y y)\cos(k_z z) + \sin(k_z z)\cos(k_x x) = 0$$
In a 2D cross-section under uniform temporal phase modulation, this non-linear harmonic function models spontaneous mesoscale phase separation, providing self-organizing boundaries without reliance on numerical grid grids.

#### 2. Helmholtz-Hodge Touch Decomposition
To eliminate spurious unphysical vorticity induced by static touches, the velocity input is strictly decomposed:
$$\mathbf{u}_{\text{input}} = \mathbf{u}_{\text{potential}} + \mathbf{u}_{\text{solenoidal}}$$
* **Curl-Free Potential Tap**:
  Static pressing generates purely radial potential divergence without shear:
  $$\mathbf{u}_{\text{potential}} = -\nabla \Phi, \quad \nabla \times \mathbf{u}_{\text{potential}} = \mathbf{0}$$
* **Solenoidal Rotational Injection**:
  Rotational gestures inject genuine vorticity through angular momentum flux:
  $$\boldsymbol{\omega} = \nabla \times \mathbf{u}_{\text{solenoidal}} \neq \mathbf{0}$$

#### 3. Lamb-Oseen Viscous Vortex Dynamic Core
Vorticity injection satisfies the two-dimensional Navier-Stokes vorticity transport equation:
$$\frac{\partial \omega}{\partial t} + (\mathbf{u} \cdot \nabla)\omega = \nu \nabla^2 \omega$$
The velocity field around each discrete vortex center $\mathbf{x}_i$ is computed via the exact viscous core solution:
$$\mathbf{u}_i(\mathbf{x}) = \frac{\Gamma_i}{2\pi \|\mathbf{x} - \mathbf{x}_i\|} \left(1 - \exp\left(-\frac{\|\mathbf{x} - \mathbf{x}_i\|^2}{r_{\text{core}}^2}\right)\right) \frac{(-\Delta y, \Delta x)}{\|\mathbf{x} - \mathbf{x}_i\|} \exp(-\lambda \|\mathbf{x} - \mathbf{x}_i\|^2)$$
where $\Gamma_i$ is circulation and $r_{\text{core}}$ represents the core radius preventing singular field divergence.

#### 4. Lagrangian Angular Momentum Accumulation & Memory
The CPU maintains a discrete Lagrangian vortex history stack. Curvilinear gestures continuously integrate kinematic angular momentum:
$$\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \, \mathrm{d}t$$
Active vortices continuously deform the spatial coordinate mapping via localized rotational operators, retaining spiral memory over a physical dissipation window of 15\~20 seconds without recursive texture feedback dissipation.

#### 5. Smooth Shear Slicing & Boundary Self-Healing
High-velocity traverse gestures induce localized shear displacement via an algebraic sigmoid function:
$$\Delta \mathbf{x}_{\text{shear}} = \mathbf{n} \cdot \left(\frac{d_\perp}{\kappa + |d_\perp|}\right) \exp\left(-\frac{\|\mathbf{x} - \mathbf{x}_0\|^2}{\sigma^2}\right)$$
This replaces discontinuous geometric discard masks, allowing the phase boundary to tear smoothly and close naturally via simulated surface tension.

---

### Release Architecture Evolution

* **v1.0.0 - Ambient Conformal Engine**: Initial release featuring conformal single-center flow and base interference.
* **v2.0.0 - Dual-Touch Topological Interaction**: Added dual-pointer support and dipole flow fields.
* **v3.0.0 - Kelvin Wake & Velocity Attenuation**: Integrated wave vector damping and velocity sensitivity.
* **v4.0.0 - Multi-Scale Logarithmic Spiral**: Eliminated additive glare spots and introduced multi-tier scale nesting.
* **v5.0.0 - Advanced Viscous Oil Engine**: Introduced dual-touch droplet fusion and high-velocity slicing mechanics.
* **v5.1.0 - Physical Hydrodynamic Manifold**: Complete physical restructuring. Decoupled potential taps from solenoidal vorticity; introduced discrete angular momentum integration and algebraic continuous shear fields.

---

<a name="中文说明"></a>
## 中文说明

### 概述
基于 Android 13+ AGSL（Android Graphics Shading Language）`RuntimeShader` 构建的高性能物理流体动态壁纸引擎。系统摒弃了高显存带宽消耗的离屏帧缓冲迭代，采用纯解析微分流形与拉格朗日离散点涡混合运动学模型，在保障绝对零显存开销的前提下实现 120 FPS 满帧流体自组织演化。

---

### 核心数学与物理动力学建模

#### 1. Gyroid 极小曲面相界面（基态场）
空间自组织纹理由三维 Schoen's Gyroid 极小曲面的零等值面截面提供：
$$\phi_0(\mathbf{x}) = \sin(k_x x)\cos(k_y y) + \sin(k_y y)\cos(k_z z) + \sin(k_z z)\cos(k_x x) = 0$$
在二维平面随时间相位连续演化时，该非线性谐波函数自然呈现出类似高分子自组装或双连续微乳液的相分离边界，消除了传统周期正弦波矢产生的晶格闪烁感。

#### 2. 亥姆霍兹-霍奇触控场分解（无旋势流与旋度分离）
为防止静止点击产生非物理的虚假旋转，触控输入严格遵循场论分解：
$$\mathbf{u}_{\text{input}} = \mathbf{u}_{\text{potential}} + \mathbf{u}_{\text{solenoidal}}$$
* **无旋径向排挤势流**：
  静止按压仅施加径向高斯正压力，其旋度严格为零：
  $$\mathbf{u}_{\text{potential}} = -\nabla \Phi, \quad \nabla \times \mathbf{u}_{\text{potential}} = \mathbf{0}$$
* **有旋涡度输入**：
  仅当指尖运动轨迹形成围绕枢轴的闭合或弯曲回路时，通过角动量外积通量注入旋转环量：
  $$\boldsymbol{\omega} = \nabla \times \mathbf{u}_{\text{solenoidal}} \neq \mathbf{0}$$

#### 3. 兰姆-奥辛（Lamb-Oseen）黏性有限涡核
注入的涡度在二维黏性流场中遵循涡量输运控制方程：
$$\frac{\partial \omega}{\partial t} + (\mathbf{u} \cdot \nabla)\omega = \nu \nabla^2 \omega$$
其空间诱导诱导速度场采用解析高斯涡核形式：
$$\mathbf{u}_i(\mathbf{x}) = \frac{\Gamma_i}{2\pi \|\mathbf{x} - \mathbf{x}_i\|} \left(1 - \exp\left(-\frac{\|\mathbf{x} - \mathbf{x}_i\|^2}{r_{\text{core}}^2}\right)\right) \frac{(-\Delta y, \Delta x)}{\|\mathbf{x} - \mathbf{x}_i\|} \exp(-\lambda \|\mathbf{x} - \mathbf{x}_i\|^2)$$
通过因子 $\left(1 - \exp\left(-\frac{r^2}{r_{\text{core}}^2}\right)\right)$ 彻底抹除中心奇异点（$r \to 0$ 时速度平滑归零），消除局部像素发散。

#### 4. 拉格朗日角动量记忆积分
Kotlin 层维护一个离散拉格朗日点涡环形栈，对手势运动做角动量积分：
$$\mathbf{L} = \oint (\mathbf{r} \times \mathbf{v}) \, \mathrm{d}t$$
连续转圈搅动时，累积的角位移使坐标映射产生局域旋转卷裹：
$$\mathbf{x}' = \mathbf{x}_c + \mathbf{R}(\theta(\mathbf{x})) (\mathbf{x} - \mathbf{x}_c)$$
该机制形成长达 15\~20 秒的连续螺旋演化记忆，克服了离屏双线性插值采样导致的数值耗散模糊。

#### 5. 代数 Sigmoid 连续剪切破裂与表面张力自愈
高速划切时，系统沿划痕法线方向施加代数 S 型位移：
$$\Delta \mathbf{x}_{\text{shear}} = \mathbf{n} \cdot \left(\frac{d_\perp}{\kappa + |d_\perp|}\right) \exp\left(-\frac{\|\mathbf{x} - \mathbf{x}_0\|^2}{\sigma^2}\right)$$
彻底弃用硬几何切除蒙版，油膜在切开后呈现光滑物理凹陷，并在黏性与表面张力作用下在 1.5\~2.0 秒内平滑闭合。

---

### 版本迭代历史

* **v1.0.0 - 基础对流共形引擎**：确立 AGSL `RuntimeShader` 基础管线，实现单点共形流场。
* **v2.0.0 - 双指拓扑偶极交互**：引入双指反向偶极喷流模型，支持多点触控。
* **v3.0.0 - 开尔文尾迹与手速自适应**：构建基于物理手速采样的波矢阻尼衰减系统。
* **v4.0.0 - 多尺度对流与暗场消噪**：压制高曝光色相，建立深海冷色阶。
* **v5.0.0 - 高级油膜撕裂与液滴融合**：引入表面张力排挤位移与双指 Metaball 融合机制。
* **v5.1.0 - 物理流体力学流形重构**：分离无旋势流与有旋角动量场；建立拉格朗日历史点涡栈与代数剪切撕裂流场，实现无光斑、长记忆、真实物理流体反馈。

---

### 技术规范与环境要求
* **目标平台**：Android 13+（API Level 33+）
* **图形核心**：Android Graphics Shading Language (AGSL) / SkSL
* **运行管线**：`WallpaperService` + 硬件加速 `Canvas`
* **内存占用**：零外部 GPU 纹理显存分配（Zero-Allocation Pipeline）
