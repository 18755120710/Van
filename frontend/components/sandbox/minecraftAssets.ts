import * as THREE from 'three'
import type { SandboxRole } from './types'

export interface RoleRig {
  role: SandboxRole
  root: THREE.Group
  head: THREE.Group
  leftArm: THREE.Mesh
  rightArm: THREE.Mesh
  leftLeg: THREE.Mesh
  rightLeg: THREE.Mesh
  badge?: THREE.Mesh
}

export interface WorkstationRig {
  role: SandboxRole
  root: THREE.Group
  screens: THREE.Mesh[]
}

type Mat = THREE.MeshStandardMaterial

const materialCache = new Map<string, Mat>()

// 创建并缓存高质感哑光材质，开启温和的光泽度和漫反射
export const createMaterial = (
  color: number,
  options: {
    emissive?: number
    emissiveIntensity?: number
    roughness?: number
    metalness?: number
    opacity?: number
    transparent?: boolean
  } = {}
) => {
  const key = `${color}-${options.emissive || 0}-${options.emissiveIntensity || 0}-${options.roughness || 0.45}-${options.metalness || 0.1}-${options.opacity || 1}-${options.transparent || false}`
  const cached = materialCache.get(key)
  if (cached) return cached

  const mat = new THREE.MeshStandardMaterial({
    color,
    emissive: options.emissive || 0x000000,
    emissiveIntensity: options.emissiveIntensity || 0,
    roughness: options.roughness ?? 0.45,
    metalness: options.metalness ?? 0.1,
    opacity: options.opacity ?? 1.0,
    transparent: options.transparent ?? false
  })
  materialCache.set(key, mat)
  return mat
}

// 快速创建长方体
const box = (
  width: number,
  height: number,
  depth: number,
  color: number,
  position: [number, number, number],
  options?: Parameters<typeof createMaterial>[1]
) => {
  const mesh = new THREE.Mesh(
    new THREE.BoxGeometry(width, height, depth),
    createMaterial(color, options)
  )
  mesh.position.set(...position)
  mesh.castShadow = true
  mesh.receiveShadow = true
  return mesh
}

// 快速向组中添加长方体
const addBox = (
  group: THREE.Group,
  width: number,
  height: number,
  depth: number,
  color: number,
  position: [number, number, number],
  options?: Parameters<typeof createMaterial>[1]
) => {
  const mesh = box(width, height, depth, color, position, options)
  group.add(mesh)
  return mesh
}

// 高保真精致色彩调色盘，完美契合系统主体的高级视觉风格
const palette: Record<SandboxRole, {
  hair: number
  shirt: number
  trim: number
  pants: number
  shoes: number
  accessory: number
}> = {
  pm: {
    hair: 0x3f2f25,      // 深棕发色
    shirt: 0xf8fafc,     // 纯白高档衬衫
    trim: 0x1e3a8a,      // 藏蓝色西装坎肩
    pants: 0x334155,     // 商务深灰西裤
    shoes: 0x451a03,     // 皮鞋棕
    accessory: 0x2563eb  // 吊绳宝蓝色
  },
  planner: {
    hair: 0x1e1b4b,      // 极客蓝黑发色
    shirt: 0xe2e8f0,     // 现代浅灰卫衣
    trim: 0x2563eb,      // 科技感蓝色拼线
    pants: 0x1e293b,     // 深黑蓝牛仔裤
    shoes: 0x0f172a,     // 暗色潮鞋
    accessory: 0xeab308  // 暖金色吊绳
  },
  browser: {
    hair: 0x7c2d12,      // 动感红棕发色
    shirt: 0x0f172a,     // 极简曜石黑外套
    trim: 0x38bdf8,      // 天蓝色亮边
    pants: 0x475569,     // 休闲灰长裤
    shoes: 0xe2e8f0,     // 纯白板鞋
    accessory: 0x06b6d4  // 极客青吊绳
  },
  qa: {
    hair: 0x78350f,      // 温暖琥珀褐发色
    shirt: 0xfef08a,     // 精致淡黄休闲衫
    trim: 0xeab308,      // 芒果黄饰边
    pants: 0x374151,     // 深灰色哈伦裤
    shoes: 0xf8fafc,     // 纯白运动鞋
    accessory: 0xdb2777  // 玫红色吊绳
  },
  ops: {
    hair: 0x18181b,      // 纯粹黑发色
    shirt: 0x27272a,     // 质感炭黑极客衫
    trim: 0x10b981,      // 翠绿色极客线条
    pants: 0x18181b,     // 漆黑长裤
    shoes: 0x3f3f46,     // 运动灰鞋
    accessory: 0x10b981  // 极客绿吊绳
  }
}

// ---------------------------------------------------------------------------
// 1. 精致方块人 (Voxel Characters) 引擎构建
// ---------------------------------------------------------------------------
export const createMinecraftCharacter = (role: SandboxRole): RoleRig => {
  const colors = palette[role]
  const root = new THREE.Group()
  const head = new THREE.Group()
  
  const skin = 0xfdba74 // 饱满高贵的健康肤色
  
  // 1.1 精致身体 & 领口细节
  addBox(root, 0.36, 0.58, 0.18, colors.shirt, [0, 0.92, 0])
  // 领口和西装马甲/肩饰
  addBox(root, 0.38, 0.14, 0.2, colors.trim, [0, 1.14, 0])

  // PM 特属：胸前白衬衫V字领口及小领结，立竿见影的干练感
  if (role === 'pm') {
    addBox(root, 0.1, 0.15, 0.21, 0xffffff, [0, 1.1, 0.005]) // V领衬衫
    addBox(root, 0.12, 0.05, 0.22, 0xd97706, [0, 1.15, 0.01]) // 橘红色小领结
  }

  // 1.2 左右手臂与精致手掌体素
  const leftArm = addBox(root, 0.12, 0.52, 0.14, colors.shirt, [-0.28, 0.9, 0])
  const rightArm = addBox(root, 0.12, 0.52, 0.14, colors.shirt, [0.28, 0.9, 0])
  // 裸露的精致手腕/手掌拼色块
  addBox(root, 0.13, 0.12, 0.15, skin, [-0.28, 0.58, 0])
  addBox(root, 0.13, 0.12, 0.15, skin, [0.28, 0.58, 0])

  // 1.3 左右长裤与精致拼色鞋子
  addBox(root, 0.15, 0.48, 0.15, colors.pants, [-0.09, 0.34, 0])
  addBox(root, 0.15, 0.48, 0.15, colors.pants, [0.09, 0.34, 0])
  // 精致拼接鞋底和鞋身
  const leftLeg = addBox(root, 0.16, 0.1, 0.18, colors.shoes, [-0.09, 0.07, 0.01])
  const rightLeg = addBox(root, 0.16, 0.1, 0.18, colors.shoes, [0.09, 0.07, 0.01])

  // 1.4 精致立体头部及多层立体刘海/发型
  addBox(head, 0.42, 0.42, 0.42, skin, [0, 1.42, 0]) // 头部核心
  
  // 基底发型
  addBox(head, 0.44, 0.16, 0.44, colors.hair, [0, 1.61, -0.01]) // 顶部头发
  addBox(head, 0.46, 0.14, 0.2, colors.hair, [0, 1.5, -0.19])   // 额前主刘海
  addBox(head, 0.44, 0.28, 0.2, colors.hair, [0, 1.44, 0.18])   // 脑后勺长发
  
  // 立体刘海微方块微调 (多层拼接表现精致感)
  addBox(head, 0.1, 0.08, 0.22, colors.hair, [-0.12, 1.52, -0.19])
  addBox(head, 0.08, 0.08, 0.22, colors.hair, [0.12, 1.52, -0.19])
  
  // 精致眼睛体素（白睛 + 虹膜复合构造）
  addBox(head, 0.08, 0.08, 0.02, 0xffffff, [-0.1, 1.42, -0.215]) // 左白睛
  addBox(head, 0.08, 0.08, 0.02, 0xffffff, [0.1, 1.42, -0.215])  // 右白睛
  addBox(head, 0.04, 0.08, 0.025, 0x1f2937, [-0.08, 1.42, -0.218]) // 左虹膜
  addBox(head, 0.04, 0.08, 0.025, 0x1f2937, [0.08, 1.42, -0.218])  // 右虹膜

  // 1.5 角色专属极客立体配饰
  
  // Planner 的立体圆黑框镜架 + 极客幽蓝自发光耳麦
  if (role === 'planner') {
    // 黑镜架
    addBox(head, 0.13, 0.13, 0.03, 0x0f172a, [-0.1, 1.42, -0.222]) // 左框
    addBox(head, 0.13, 0.13, 0.03, 0x0f172a, [0.1, 1.42, -0.222])  // 右框
    addBox(head, 0.08, 0.03, 0.02, 0x0f172a, [0, 1.44, -0.222])   // 鼻梁梁
    addBox(head, 0.02, 0.02, 0.24, 0x0f172a, [-0.22, 1.44, -0.11]) // 左镜腿
    addBox(head, 0.02, 0.02, 0.24, 0x0f172a, [0.22, 1.44, -0.11])  // 右镜腿
    
    // 自发光耳麦
    addBox(head, 0.05, 0.08, 0.08, 0x0ea5e9, [0.22, 1.4, 0.02], { emissive: 0x38bdf8, emissiveIntensity: 0.8 })
    addBox(head, 0.02, 0.02, 0.12, 0x0ea5e9, [0.22, 1.36, -0.06], { emissive: 0x38bdf8, emissiveIntensity: 0.5 })
  }

  // Browser 的天蓝色立体运动式挂耳式大耳机
  if (role === 'browser') {
    addBox(head, 0.05, 0.16, 0.13, 0x0284c7, [-0.22, 1.4, 0])  // 左耳罩
    addBox(head, 0.05, 0.16, 0.13, 0x0284c7, [0.22, 1.4, 0])   // 右耳罩
    addBox(head, 0.44, 0.03, 0.08, 0x0284c7, [0, 1.63, 0])    // 头戴大横梁
    // 侧边炫酷蓝光装饰带
    addBox(head, 0.052, 0.04, 0.04, 0x38bdf8, [-0.22, 1.4, 0], { emissive: 0x38bdf8, emissiveIntensity: 0.9 })
    addBox(head, 0.052, 0.04, 0.04, 0x38bdf8, [0.22, 1.4, 0], { emissive: 0x38bdf8, emissiveIntensity: 0.9 })
  }

  // QA 的精致黄橙拼色立体棒球帽
  if (role === 'qa') {
    addBox(head, 0.44, 0.14, 0.44, 0xeab308, [0, 1.63, 0]) // 帽冠
    // 斜戴的帽舌 (通过 X 轴或 Z 轴错落拼装表现斜着戴的街头感)
    addBox(head, 0.38, 0.02, 0.18, 0xeab308, [-0.08, 1.58, -0.24]) // 斜出的帽沿
    addBox(head, 0.08, 0.08, 0.02, 0x475569, [-0.08, 1.65, -0.225]) // 帽子前面的小徽章体素
  }

  // Ops 的发光翠绿赛博护目镜
  if (role === 'ops') {
    addBox(head, 0.38, 0.12, 0.04, 0x10b981, [0, 1.43, -0.215], {
      emissive: 0x10b981,
      emissiveIntensity: 0.72,
      transparent: true,
      opacity: 0.88
    })
    addBox(head, 0.02, 0.06, 0.22, 0x27272a, [-0.2, 1.43, -0.09]) // 左框架
    addBox(head, 0.02, 0.06, 0.22, 0x27272a, [0.2, 1.43, -0.09])  // 右框架
  }

  root.add(head)

  // 1.6 全员挂载的 3D 立体小工牌 (带吊绳结构)
  const badge = addBox(root, 0.11, 0.15, 0.03, 0xffffff, [0.1, 1.0, -0.105]) // 证件板
  addBox(root, 0.02, 0.24, 0.02, colors.accessory, [0.05, 1.11, -0.11])    // 左挂绳
  addBox(root, 0.02, 0.24, 0.02, colors.accessory, [0.15, 1.11, -0.11])    // 右挂绳
  // 工牌内部贴纸细节 (深色头像贴图及红印)
  addBox(root, 0.05, 0.06, 0.035, 0x475569, [0.07, 1.03, -0.11])
  addBox(root, 0.06, 0.015, 0.035, 0x10b981, [0.11, 0.94, -0.11]) // 绿点状态灯

  return {
    role,
    root,
    head,
    leftArm,
    rightArm,
    leftLeg,
    rightLeg,
    badge
  }
}

// ---------------------------------------------------------------------------
// 2. 现代极简纯白办公工作台构建 (Workstation)
// ---------------------------------------------------------------------------
export const createWorkstation = (role: SandboxRole, accent: number) => {
  const root = new THREE.Group()
  const screens: THREE.Mesh[] = []

  // 2.1 纯白色高反射桌面 (哑光高质感白漆)
  addBox(root, 1.6, 0.06, 0.9, 0xffffff, [0, 0.6, 0], { roughness: 0.15, metalness: 0.05 })
  
  // 2.2 浅灰色纤细圆润桌腿 (4个角)
  const legColor = 0xd4d4d8
  addBox(root, 0.06, 0.6, 0.06, legColor, [-0.72, 0.27, -0.37])
  addBox(root, 0.06, 0.6, 0.06, legColor, [0.72, 0.27, -0.37])
  addBox(root, 0.06, 0.6, 0.06, legColor, [-0.72, 0.27, 0.37])
  addBox(root, 0.06, 0.6, 0.06, legColor, [0.72, 0.27, 0.37])

  // 2.3 极致窄边框超薄现代显示器 (Browser 为双屏联动，其余为单屏)
  const screenCount = role === 'browser' ? 2 : 1
  for (let i = 0; i < screenCount; i += 1) {
    const offsetX = screenCount === 2 ? (i === 0 ? -0.26 : 0.26) : 0
    
    // 超薄背板支架
    addBox(root, 0.08, 0.22, 0.08, 0x18181b, [offsetX, 0.72, -0.26]) // 纤细立柱
    addBox(root, 0.2, 0.02, 0.2, 0xd4d4d8, [offsetX, 0.63, -0.26])   // 银色底座
    
    // 黑色大屏幕背板
    addBox(root, 0.54, 0.34, 0.03, 0x18181b, [offsetX, 0.88, -0.28])
    
    // 自发光超窄液晶屏幕面板 (Active状态时具有脉冲亮屏特效)
    const screen = addBox(root, 0.52, 0.32, 0.015, 0x181820, [offsetX, 0.88, -0.27], {
      emissive: accent,
      emissiveIntensity: 0.35,
      roughness: 0.1
    })
    screens.push(screen)
    
    // 键盘和触控板体素
    addBox(root, 0.36, 0.015, 0.14, 0xf4f4f5, [offsetX, 0.635, 0.08]) // 纯白极简键盘
    addBox(root, 0.06, 0.015, 0.08, 0xffffff, [offsetX + 0.24, 0.635, 0.08], { roughness: 0.1 }) // 鼠标
  }

  // 2.4 桌角点缀细节：一杯温热的奶茶 / 咖啡杯
  addBox(root, 0.08, 0.12, 0.08, 0xd7ccc8, [-0.55, 0.68, 0.2])  // 纸杯身
  addBox(root, 0.09, 0.02, 0.09, 0x3e2723, [-0.55, 0.74, 0.2])  // 深色盖子

  // 桌角白色 Mac Studio 主机
  addBox(root, 0.16, 0.09, 0.16, 0xf4f4f5, [0.55, 0.66, 0.22])

  return {
    role,
    root,
    screens
  } satisfies WorkstationRig
}

// ---------------------------------------------------------------------------
// 3. 曜石黑人体工学电脑转椅构建 (Office Chair)
// ---------------------------------------------------------------------------
export const createOfficeChair = () => {
  const root = new THREE.Group()
  
  const chairColor = 0x27272a // 深炭灰曜石黑
  
  // 3.1 极简厚实坐垫 (带前侧微斜角切面)
  addBox(root, 0.48, 0.08, 0.46, chairColor, [0, 0.38, 0])
  
  // 3.2 护脊工学悬浮式靠背
  addBox(root, 0.44, 0.48, 0.08, chairColor, [0, 0.68, 0.18])
  
  // 3.3 银色气压杆支撑杆 & 五星爪轮底座
  addBox(root, 0.06, 0.32, 0.06, 0xa1a1aa, [0, 0.18, 0]) // 银色钢质中轴
  
  // 十字/五星爪底座 (扁平金属杆拼接)
  addBox(root, 0.64, 0.03, 0.06, 0x52525b, [0, 0.04, 0])
  addBox(root, 0.06, 0.03, 0.64, 0x52525b, [0, 0.04, 0])
  
  // 底部极微小的尼龙静音轮体素
  addBox(root, 0.04, 0.04, 0.04, 0x18181b, [-0.29, 0.01, 0])
  addBox(root, 0.04, 0.04, 0.04, 0x18181b, [0.29, 0.01, 0])
  addBox(root, 0.04, 0.04, 0.04, 0x18181b, [0, 0.01, -0.29])
  addBox(root, 0.04, 0.04, 0.04, 0x18181b, [0, 0.01, 0.29])

  return root
}

// ---------------------------------------------------------------------------
// 4. 极致现代咖啡吧台区 (Coffee Station)
// ---------------------------------------------------------------------------
export const createCoffeeStation = () => {
  const root = new THREE.Group()
  
  const marbleColor = 0xf8fafc // 极浅冷大理石白
  
  // 4.1 L型精细拼接现代吧台
  // 主台面
  addBox(root, 1.8, 0.9, 0.6, marbleColor, [0, 0.45, 0])
  // 转角台面，完美拼合呈优雅L型
  addBox(root, 0.6, 0.9, 1.0, marbleColor, [-0.6, 0.45, 0.8])
  
  // 吧台底部的拉丝黑色金属踢脚线
  addBox(root, 1.81, 0.08, 0.61, 0x27272a, [0, 0.04, 0])
  addBox(root, 0.61, 0.08, 1.01, 0x27272a, [-0.6, 0.04, 0.8])

  // 4.2 意式重工业不锈钢咖啡机 (Coffee Machine)
  const metalColor = 0xe4e4e7
  const machine = new THREE.Group()
  machine.position.set(0.4, 0.9, 0.05)
  
  addBox(machine, 0.48, 0.38, 0.34, metalColor, [0, 0.19, 0], { roughness: 0.1, metalness: 0.8 }) // 机身
  addBox(machine, 0.44, 0.08, 0.28, 0x27272a, [0, 0.04, 0.02])                                  // 接水槽
  // 不锈钢双咖啡萃取头
  addBox(machine, 0.05, 0.1, 0.05, 0x18181b, [-0.1, 0.08, -0.14])
  addBox(machine, 0.05, 0.1, 0.05, 0x18181b, [0.1, 0.08, -0.14])
  // 金属小蒸汽棒
  addBox(machine, 0.02, 0.16, 0.02, 0xa1a1aa, [0.18, 0.12, -0.14], { roughness: 0.05, metalness: 0.9 })
  // 顶部自发光微小发光按钮 (细节拉满)
  addBox(machine, 0.03, 0.03, 0.03, 0x10b981, [-0.12, 0.32, -0.16], { emissive: 0x10b981, emissiveIntensity: 0.7 })
  addBox(machine, 0.03, 0.03, 0.03, 0xef4444, [-0.04, 0.32, -0.16], { emissive: 0xef4444, emissiveIntensity: 0.4 })
  
  root.add(machine)

  // 4.3 吧台上整齐陈列的纸杯两列 (Coffee Cups)
  const cupPlacements: [number, number, number][] = [
    [-0.3, 0.9, 0.05], [-0.15, 0.9, 0.05], [0, 0.9, 0.05],
    [-0.3, 0.9, -0.08], [-0.15, 0.9, -0.08], [0, 0.9, -0.08],
    [-0.6, 0.9, 0.5], [-0.6, 0.9, 0.65], [-0.6, 0.9, 0.8]
  ]
  cupPlacements.forEach((pos, idx) => {
    const cup = new THREE.Group()
    cup.position.set(...pos)
    
    // 牛皮纸色杯身
    addBox(cup, 0.08, 0.11, 0.08, 0xd7ccc8, [0, 0.055, 0])
    // 巧克力色杯盖
    addBox(cup, 0.09, 0.02, 0.09, 0x3e2723, [0, 0.11, 0])
    
    // 纸杯外面环绕套环细节
    addBox(cup, 0.084, 0.04, 0.084, 0x8d6e63, [0, 0.055, 0])
    
    root.add(cup)
  })

  return root
}

// ---------------------------------------------------------------------------
// 5. 极致极简家用跑步机构建 (Treadmill)
// ---------------------------------------------------------------------------
export const createTreadmill = () => {
  const root = new THREE.Group()
  
  const whiteFrame = 0xf8fafc // 高级哑光白色
  
  // 5.1 跑带底盘边框
  addBox(root, 0.82, 0.08, 1.55, whiteFrame, [0, 0.04, 0])
  // 灰黑塑胶大跑带面
  addBox(root, 0.64, 0.015, 1.36, 0x3f3f46, [0, 0.08, 0], { roughness: 0.8 })
  
  // 5.2 扶手与前倾控制面板
  addBox(root, 0.05, 0.85, 0.05, whiteFrame, [-0.36, 0.46, -0.6]) // 左侧立柱
  addBox(root, 0.05, 0.85, 0.05, whiteFrame, [0.36, 0.46, -0.6])  // 右侧立柱
  
  // 黑色横板控制台
  addBox(root, 0.78, 0.06, 0.16, 0x18181b, [0, 0.88, -0.6])
  // 自发光绿色/红色微型按键 (启动/停止)
  addBox(root, 0.04, 0.01, 0.04, 0x10b981, [-0.15, 0.915, -0.6], { emissive: 0x10b981, emissiveIntensity: 0.8 })
  addBox(root, 0.04, 0.01, 0.04, 0xef4444, [0.15, 0.915, -0.6], { emissive: 0xef4444, emissiveIntensity: 0.8 })
  
  // 左右黑色软胶小扶手
  addBox(root, 0.04, 0.04, 0.38, 0x18181b, [-0.36, 0.86, -0.42])
  addBox(root, 0.04, 0.04, 0.38, 0x18181b, [0.36, 0.86, -0.42])

  return root
}

// ---------------------------------------------------------------------------
// 6. 极简洗手间角落与圆润智能马桶构建 (Restroom Corner)
// ---------------------------------------------------------------------------
export const createRestroom = () => {
  const root = new THREE.Group()
  
  const whiteCeramic = 0xffffff // 陶瓷纯亮白
  
  // 6.1 智能马桶组 (Toilet)
  const toilet = new THREE.Group()
  toilet.position.set(0, 0, 0.05)
  
  // 陶瓷主身 (圆润底座)
  addBox(toilet, 0.38, 0.36, 0.52, whiteCeramic, [0, 0.18, -0.05], { roughness: 0.1 })
  
  // 智能马桶盖垫圈圈 (分层立体化)
  addBox(toilet, 0.36, 0.03, 0.48, whiteCeramic, [0, 0.38, -0.05], { roughness: 0.1 })
  
  // 微微半掀开的扁平马桶盖板 (微倾的体素组合)
  addBox(toilet, 0.36, 0.38, 0.03, whiteCeramic, [0, 0.56, 0.16], { roughness: 0.1 })
  
  // 后侧方形储水箱 (智能控制背板)
  addBox(toilet, 0.38, 0.52, 0.16, whiteCeramic, [0, 0.62, 0.22], { roughness: 0.1 })
  // 蓝色发光智能侧面板 (细节彩蛋)
  addBox(toilet, 0.02, 0.06, 0.1, 0x3b82f6, [0.192, 0.74, 0.22], { emissive: 0x3b82f6, emissiveIntensity: 0.7 })

  root.add(toilet)

  // 6.2 墙壁悬挂式迷你卷纸架 (Toilet Paper Roll)
  const roll = new THREE.Group()
  roll.position.set(0.32, 0.72, 0.26)
  
  // 银色小置物板/小纸架架子
  addBox(roll, 0.03, 0.06, 0.14, 0xd4d4d8, [0.08, 0, 0], { roughness: 0.05, metalness: 0.8 })
  // 缠卷在纸芯上的纯白蓬松卫生卷纸
  addBox(roll, 0.12, 0.12, 0.11, 0xfafafa, [0, 0, 0], { roughness: 0.85 })
  // 垂挂下来、在风中自然耷拉下的微薄一小段纸张细节 (太有灵魂了)
  addBox(roll, 0.005, 0.09, 0.11, 0xfafafa, [-0.06, -0.09, 0], { roughness: 0.85 })

  root.add(roll)

  return root
}

// ---------------------------------------------------------------------------
// 7. 其余设备及背景装点 (白板、路由器机架、盆栽、文件柜、现代地板)
// ---------------------------------------------------------------------------
export const createWhiteboard = () => {
  const root = new THREE.Group()
  // 现代浅灰白板面
  addBox(root, 1.5, 0.95, 0.04, 0xf8fafc, [0, 1.18, 0], { roughness: 0.1 })
  // 不锈钢银色包边框
  addBox(root, 1.54, 0.06, 0.06, 0x71717a, [0, 1.68, 0])
  addBox(root, 1.54, 0.06, 0.06, 0x71717a, [0, 0.68, 0])
  
  // 彩色磁吸立体小便签纸 (拼贴细节)
  addBox(root, 0.1, 0.14, 0.01, 0xef4444, [-0.4, 1.25, -0.025])
  addBox(root, 0.1, 0.14, 0.01, 0x10b981, [-0.18, 1.15, -0.025])
  addBox(root, 0.1, 0.14, 0.01, 0x3b82f6, [0.08, 1.34, -0.025])
  
  // 手绘黑色线条小草稿框
  addBox(root, 0.44, 0.02, 0.01, 0x18181b, [0.38, 1.16, -0.025])
  addBox(root, 0.32, 0.02, 0.01, 0x18181b, [0.32, 1.05, -0.025])
  
  return root
}

export const createRouterRack = () => {
  const root = new THREE.Group()
  // 现代曜石黑刀片服务器机箱
  addBox(root, 0.85, 0.22, 0.5, 0x27272a, [0, 0.22, 0])
  // 细长不锈钢金属四角托架
  addBox(root, 0.06, 0.72, 0.06, 0x52525b, [-0.3, 0.72, -0.14])
  addBox(root, 0.06, 0.72, 0.06, 0x52525b, [0.3, 0.72, -0.14])
  
  // 极其精致的自发光指示灯模块 (绿/红/黄发光点，表现网络吞吐和Ops特征)
  addBox(root, 0.08, 0.05, 0.03, 0x10b981, [-0.24, 0.25, -0.26], { emissive: 0x10b981, emissiveIntensity: 0.9 })
  addBox(root, 0.08, 0.05, 0.03, 0x10b981, [-0.1, 0.25, -0.26], { emissive: 0x10b981, emissiveIntensity: 0.9 })
  addBox(root, 0.08, 0.05, 0.03, 0xeab308, [0.04, 0.25, -0.26], { emissive: 0xeab308, emissiveIntensity: 0.8 })
  addBox(root, 0.08, 0.05, 0.03, 0xef4444, [0.18, 0.25, -0.26], { emissive: 0xef4444, emissiveIntensity: 0.4 })
  
  return root
}

export const createPlant = () => {
  const root = new THREE.Group()
  // 极简现代水泥灰几何体花盆
  addBox(root, 0.32, 0.32, 0.32, 0xe4e4e7, [0, 0.16, 0], { roughness: 0.5 })
  // 翠绿盆栽植物枝干及体素大片绿叶
  addBox(root, 0.1, 0.48, 0.1, 0x065f46, [0, 0.5, 0])
  addBox(root, 0.26, 0.16, 0.24, 0x059669, [-0.14, 0.7, 0.02])
  addBox(root, 0.24, 0.16, 0.26, 0x10b981, [0.14, 0.82, -0.02])
  addBox(root, 0.14, 0.14, 0.24, 0x34d399, [0, 0.92, -0.14])
  return root
}

export const createFileCabinet = () => {
  const root = new THREE.Group()
  // 暖棕橡木质感文件柜
  addBox(root, 0.58, 0.88, 0.48, 0xd97706, [0, 0.44, 0])
  // 抽屉滑道接缝
  addBox(root, 0.48, 0.22, 0.03, 0xb45309, [0, 0.65, -0.25])
  addBox(root, 0.48, 0.22, 0.03, 0xb45309, [0, 0.34, -0.25])
  // 曜石黑抽屉拉手
  addBox(root, 0.2, 0.03, 0.03, 0x27272a, [0, 0.67, -0.27])
  addBox(root, 0.2, 0.03, 0.03, 0x27272a, [0, 0.36, -0.27])
  // 柜顶整齐立放的三本书 (红、黄、黑拼色，表现极高的人文气息)
  addBox(root, 0.1, 0.4, 0.16, 0x3b82f6, [-0.14, 1.08, 0.02])
  addBox(root, 0.1, 0.4, 0.16, 0xeab308, [0, 1.08, 0.02])
  addBox(root, 0.1, 0.4, 0.16, 0x18181b, [0.14, 1.08, 0.02])
  
  return root
}

// ---------------------------------------------------------------------------
// 8. 亮白灰大理石无缝地板构建 (Floor)
// ---------------------------------------------------------------------------
export const createFloor = () => {
  const root = new THREE.Group()
  
  // 使用超大平面铺满整个镜头视野 (120 * 120)，配合 100% 漫反射纯白以呈现高级的柔和阴影 (Soft Shadow)
  const floorMat = createMaterial(0xffffff, {
    roughness: 0.95,
    metalness: 0.0
  })
  
  const floorMesh = new THREE.Mesh(new THREE.PlaneGeometry(120, 120), floorMat)
  floorMesh.rotation.x = -Math.PI / 2
  floorMesh.position.y = 0.002 // 极微抬高避开渲染底平面冲突
  floorMesh.receiveShadow = true
  root.add(floorMesh)

  // 1. 周围纯白矮墙设计 (Half-wall, 提拔至高度 1.75 米)，带灰色顶盖收边条，完美暴露出墙体厚度与合围空间
  const wallColor = 0xffffff
  const capColor = 0xd4d4d8 // 优雅的冷灰色收边顶盖板

  // 1.1 后侧矮背景墙 (Z = -3.8, 高 1.75, 厚 0.06)
  const backWall = addBox(root, 9.2, 1.75, 0.06, wallColor, [0, 0.875, -3.8], { roughness: 0.95 })
  backWall.castShadow = false // 禁用阴影，保持大平地洁净
  backWall.receiveShadow = true
  // 后墙顶部冷灰色盖板 (突出墙面 0.02 宽表现精致厚度)
  const backCap = addBox(root, 9.22, 0.02, 0.08, capColor, [0, 1.76, -3.8])
  backCap.castShadow = false

  // 1.2 左侧矮背景墙 (内缩至 X = -3.7 处，完美贴合生活区边缘防道具遮挡，高 1.75, 厚 0.06)
  const leftWall = addBox(root, 0.06, 1.75, 7.6, wallColor, [-3.7, 0.875, 0], { roughness: 0.95 })
  leftWall.castShadow = false // 禁用阴影
  leftWall.receiveShadow = true
  // 左墙顶部冷灰色盖板
  const leftCap = addBox(root, 0.08, 0.02, 7.62, capColor, [-3.7, 1.76, 0])
  leftCap.castShadow = false

  // ---------------------------------------------------------------------------
  // 2. 在后侧矮墙上集成极致精美的【半透明幽蓝色科技玻璃大门】(X = 1.8, 高度 1.5 米隐于墙下)
  // ---------------------------------------------------------------------------
  const frameColor = 0xa1a1aa  // 银灰色高级极细钢架
  const glassColor = 0xbae6fd  // 半透明清亮幽蓝色玻璃
  const handleColor = 0xf4f4f5 // 亮银色拉丝不锈钢把手

  // 2.1 极细钢制门框 (高度 1.5 米，顶部留有 0.25 米墙梁，极其符合建筑工程美学)
  const frameL = addBox(root, 0.03, 1.5, 0.04, frameColor, [1.8 - 0.42, 0.75, -3.76])
  const frameR = addBox(root, 0.03, 1.5, 0.04, frameColor, [1.8 + 0.42, 0.75, -3.76])
  const frameT = addBox(root, 0.87, 0.03, 0.04, frameColor, [1.8, 1.5, -3.76])
  frameL.castShadow = false
  frameR.castShadow = false
  frameT.castShadow = false

  // 2.2 极具科技感的半透明玻璃门板
  const doorPanel = addBox(root, 0.8, 1.46, 0.015, glassColor, [1.8, 0.73, -3.77], {
    transparent: true,
    opacity: 0.52,
    roughness: 0.1,
    metalness: 0.8
  })
  doorPanel.castShadow = false // 门板不投射阴影，极其干净
  doorPanel.receiveShadow = true

  // 2.3 极致精致的亮银色圆润大门执手
  const handle = addBox(root, 0.02, 0.45, 0.02, handleColor, [1.8 + 0.32, 0.75, -3.74], { roughness: 0.1, metalness: 0.9 })
  handle.castShadow = false

  // 极柔和、朦胧的极淡灰色瓷砖板块分割线，限定在核心办公室网格 (9.2 x 7.6) 内，丰富工位区域的现代纹理
  const gridColor = 0xf3f4f6
  for (let x = -4; x <= 4; x += 2) {
    addBox(root, 0.015, 0.005, 7.6, gridColor, [x, 0.005, 0], { roughness: 0.5 })
  }
  for (let z = -3; z <= 3; z += 2) {
    addBox(root, 9.2, 0.005, 0.015, gridColor, [0, 0.005, z], { roughness: 0.5 })
  }

  return root
}

// 清理缓存材质，防内存泄露
export const disposeSharedMaterials = () => {
  materialCache.forEach((mat) => mat.dispose())
  materialCache.clear()
}
