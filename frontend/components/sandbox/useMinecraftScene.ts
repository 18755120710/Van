import * as THREE from 'three'
import {
  createFileCabinet,
  createFloor,
  createMinecraftCharacter,
  createOfficeChair,
  createPlant,
  createRouterRack,
  createWhiteboard,
  createWorkstation,
  createCoffeeStation,
  createTreadmill,
  createRestroom,
  disposeSharedMaterials
} from './minecraftAssets'
import type { MinecraftSandboxState, RoleAnchorMap, SandboxRole, ScreenPoint } from './types'

interface SceneRole {
  rig: ReturnType<typeof createMinecraftCharacter>
  target: THREE.Vector3
  home: THREE.Vector3
  lookAt: THREE.Vector3
}

const hiddenPoint = (): ScreenPoint => ({ x: -1000, y: -1000, visible: false })

// 各个角色的科技发光屏专属主题色
const roleAccents: Record<SandboxRole, number> = {
  pm: 0x1e3a8a,      // 藏蓝色
  planner: 0x0ea5e9, // 极客蓝
  browser: 0x06b6d4, // 极客青
  qa: 0xeab308,      // 金黄色
  ops: 0x10b981      // 翠绿色
}

export class MinecraftSandboxScene {
  private scene: THREE.Scene
  private camera: THREE.PerspectiveCamera
  private renderer: THREE.WebGLRenderer
  private container: HTMLElement
  private animationId: number | null = null
  private state: MinecraftSandboxState
  private roles = new Map<SandboxRole, SceneRole>()
  private screens = new Map<SandboxRole, THREE.Mesh[]>()
  private anchors: RoleAnchorMap = {
    pm: hiddenPoint(),
    planner: hiddenPoint(),
    browser: hiddenPoint(),
    qa: hiddenPoint(),
    ops: hiddenPoint()
  }
  
  // 视角锁定为类似图二的高精度等角俯视 Isometric 视角
  private theta = 5.0
  private phi = 0.72
  private radius = 12.8
  
  private scratchVector = new THREE.Vector3()
  private onAnchorsChanged: (anchors: RoleAnchorMap) => void
  private resizeObserver: ResizeObserver | null = null

  constructor(
    container: HTMLElement,
    state: MinecraftSandboxState,
    onAnchorsChanged: (anchors: RoleAnchorMap) => void
  ) {
    this.container = container
    this.state = state
    this.onAnchorsChanged = onAnchorsChanged
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0xffffff)

    this.camera = new THREE.PerspectiveCamera(40, 1, 0.1, 100)
    
    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: false })
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.shadowMap.enabled = true
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap

    this.container.appendChild(this.renderer.domElement)
    this.renderer.domElement.className = 'mc-sandbox-canvas'

    this.buildScene()
    this.bindEvents()
    this.resize()
    this.syncTargets()
    this.animate()
  }

  setState(state: MinecraftSandboxState) {
    this.state = state
    this.syncTargets()
  }

  dispose() {
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }

    this.unbindEvents()
    this.scene.traverse((object) => {
      const mesh = object as THREE.Mesh
      if (mesh.geometry) {
        mesh.geometry.dispose()
      }
    })
    disposeSharedMaterials()
    this.renderer.dispose()
    this.renderer.domElement.remove()
    this.roles.clear()
    this.screens.clear()
  }

  private buildScene() {
    // 调高全局透亮的环境光，渲染明净办公室的感官
    const ambient = new THREE.AmbientLight(0xffffff, 0.95)
    this.scene.add(ambient)

    // 精细布置的斜向主光源，渲染柔和立体的软阴影 (SoftShadow)
    const keyLight = new THREE.DirectionalLight(0xffffff, 1.15)
    keyLight.position.set(5.5, 7.5, 3.8)
    keyLight.castShadow = true
    keyLight.shadow.mapSize.set(1024, 1024)
    keyLight.shadow.bias = -0.001
    this.scene.add(keyLight)

    // 辅助补光源 (带微弱的科技冷蓝，表现现代极客感)
    const fillLight = new THREE.PointLight(0xe0f2fe, 0.65, 12)
    fillLight.position.set(-3.2, 2.8, -2.2)
    this.scene.add(fillLight)

    // 挂载地面
    this.scene.add(createFloor())
    
    // 构建办公室道具及人物
    this.buildOfficeProps()
    this.buildRoles()
  }

  // ---------------------------------------------------------------------------
  // 3. 办公室道具场景定位排布 (Office Props Placement)
  // ---------------------------------------------------------------------------
  private buildOfficeProps() {
    // 3.1 右侧工作区工位错落排布 (PM, Planner, Browser)
    this.addStation('pm', [1.2, 0, -1.8], [0, 0, 0])
    this.addStation('planner', [0.6, 0, 0.4], [0, 0, 0])
    this.addStation('browser', [2.6, 0, -0.2], [0, 0, 0])

    // 3.2 场景其余经典现代办公道具定位
    // 白板 (中左上方)
    const whiteboard = createWhiteboard()
    whiteboard.position.set(0.2, 0, 2.2)
    whiteboard.rotation.y = Math.PI
    this.scene.add(whiteboard)

    // 路由器架 (中右下方)
    const router = createRouterRack()
    router.position.set(2.5, 0, 2.2)
    this.scene.add(router)

    // 文件柜 (中上方)
    const cabinet = createFileCabinet()
    cabinet.position.set(2.6, 0, -2.4)
    cabinet.rotation.y = Math.PI / 12
    this.scene.add(cabinet)

    // 3.3 左侧趣味生活区道具排布 (还原图二经典设定)
    // 咖啡吧台区 (左上方)
    const coffeeStation = createCoffeeStation()
    coffeeStation.position.set(-2.6, 0, -2.2)
    coffeeStation.rotation.y = Math.PI / 2 // 旋转 90 度呈优美的观赏切面
    this.scene.add(coffeeStation)

    // 极简跑步机 (左中部)
    const treadmill = createTreadmill()
    treadmill.position.set(-2.8, 0, 0.2)
    treadmill.rotation.y = Math.PI / 2 // 横向放置符合 Isometric 美学
    this.scene.add(treadmill)

    // 洗手间智能马桶与卷纸架 (左下方)
    const restroom = createRestroom()
    restroom.position.set(-2.8, 0, 2.4)
    restroom.rotation.y = Math.PI / 2 // 旋转使马桶和精细挂卷纸面向屏幕
    this.scene.add(restroom)

    // 3.4 绿植点缀
    const plantLeft = createPlant()
    plantLeft.position.set(-0.6, 0, -2.5)
    this.scene.add(plantLeft)

    const plantRight = createPlant()
    plantRight.position.set(3.3, 0, 0.8)
    this.scene.add(plantRight)
  }

  // 部署工作台与转椅
  private addStation(role: SandboxRole, position: [number, number, number], rotation: [number, number, number]) {
    const station = createWorkstation(role, roleAccents[role])
    station.root.position.set(...position)
    station.root.rotation.set(...rotation)
    this.scene.add(station.root)
    this.screens.set(role, station.screens)

    // 转椅在办公台的前下方 (z 轴偏移 0.72)
    const chair = createOfficeChair()
    chair.position.set(position[0], 0, position[2] + 0.72)
    this.scene.add(chair)
  }

  // ---------------------------------------------------------------------------
  // 4. 精致 MC 人物初始化 (Refined Roles Initialization)
  // ---------------------------------------------------------------------------
  private buildRoles() {
    // 对应工位及椅子的精确 Home 点
    const home: Record<SandboxRole, THREE.Vector3> = {
      pm: new THREE.Vector3(1.2, 0, -1.08),       // 坐在 PM 椅中心
      planner: new THREE.Vector3(0.6, 0, 1.12),   // 坐在 Planner 椅中心
      browser: new THREE.Vector3(2.6, 0, 0.52),   // 坐在 Browser 椅中心
      qa: new THREE.Vector3(-0.5, 0, 2.2),        // 站在白板侧方
      ops: new THREE.Vector3(2.2, 0, 1.6)         // 站在服务器机柜前方
    }

    // 初始 LookAt 面朝的目标点
    const lookAt: Record<SandboxRole, THREE.Vector3> = {
      pm: new THREE.Vector3(1.2, 0, -1.8),        // 盯向 PM 显示屏
      planner: new THREE.Vector3(0.6, 0, 0.4),    // 盯向 Planner 显示屏
      browser: new THREE.Vector3(2.6, 0, -0.2),   // 盯向 Browser 显示屏
      qa: new THREE.Vector3(0.2, 0, 2.2),         // 盯向白板便签
      ops: new THREE.Vector3(2.5, 0, 2.2)         // 盯向服务器网络灯
    }

    ;(['pm', 'planner', 'browser', 'qa', 'ops'] as SandboxRole[]).forEach((role) => {
      const rig = createMinecraftCharacter(role)
      rig.root.position.copy(home[role])
      
      // 统一人物的比例，精致化后人物更加协调
      rig.root.scale.setScalar(0.85)
      
      this.scene.add(rig.root)
      this.roles.set(role, {
        rig,
        target: home[role].clone(),
        home: home[role],
        lookAt: lookAt[role]
      })
    })
  }

  // ---------------------------------------------------------------------------
  // 5. 不同执行状态下的人物行进网格逻辑 (State Matrix Sync)
  // ---------------------------------------------------------------------------
  private syncTargets() {
    // 初始化所有角色回到其专属的工位 Home 点
    this.roles.forEach((role) => {
      role.target.copy(role.home)
      role.lookAt.copy(role.home.clone().add(new THREE.Vector3(0, 0, -1)))
    })

    const planner = this.roles.get('planner')
    const browser = this.roles.get('browser')
    const qa = this.roles.get('qa')
    const ops = this.roles.get('ops')

    // 5.1 规划阶段交接：Planner 走向 Browser 的桌旁进行工作交底
    if ((this.state.phase === 'handoff' || this.state.phase === 'working') && planner) {
      planner.target.set(1.9, 0, 0.52)
      planner.lookAt.set(2.6, 0, 0.52) // 盯向正在操作多屏的 Browser
    }

    // 5.2 汇报阶段：Browser 走向 PM 的办公桌前进行任务汇报
    if (this.state.phase === 'reporting' && browser) {
      browser.target.set(1.9, 0, -1.08)
      browser.lookAt.set(1.2, 0, -1.08) // 盯向主导项目的 PM
    }

    // 5.3 最终提答阶段：Planner 走向 PM 桌前提交成果
    if (this.state.phase === 'answer' && planner) {
      planner.target.set(0.5, 0, -1.08)
      planner.lookAt.set(1.2, 0, -1.08)
    }

    // 5.4 测试拦截错误：QA 踱步至场景中心思考出错链路
    if (this.state.phase === 'error' && qa) {
      qa.target.set(0.5, 0, 0.2)
      qa.lookAt.set(0, 0, 0.5)
    }

    // 5.5 部署运维阶段：Ops 贴身靠近服务器路由器机箱检修
    if (this.state.phase === 'usage' && ops) {
      ops.target.set(2.5, 0, 1.8)
      ops.lookAt.set(2.5, 0, 2.2)
    }
  }

  // ---------------------------------------------------------------------------
  // 6. 3D 物理渲染循环与交互绑定 (Render Loop)
  // ---------------------------------------------------------------------------
  private animate = () => {
    this.animationId = requestAnimationFrame(this.animate)
    const time = performance.now() * 0.001

    this.roles.forEach((role, roleName) => this.animateRole(roleName, role, time))
    this.animateScreens(time)
    this.updateCamera()
    this.renderer.render(this.scene, this.camera)
    this.projectAnchors()
  }

  // 角色精细动画引擎
  private animateRole(roleName: SandboxRole, role: SceneRole, time: number) {
    const active = roleName === this.state.activeRole
    const dir = role.target.clone().sub(role.rig.root.position)
    const distance = dir.length()

    // 步行中状态：高频的跳跃与自然的双臂、双腿 MC 交替摆动
    if (distance > 0.035) {
      dir.normalize()
      role.rig.root.position.add(dir.multiplyScalar(0.045))
      // 步行跳动波幅
      role.rig.root.position.y = Math.abs(Math.sin(time * 9.5)) * 0.06
      role.rig.root.rotation.z = Math.sin(time * 9.5) * 0.035
      role.rig.root.rotation.y = Math.atan2(dir.x, dir.z) + Math.PI
      
      // MC 经典双臂与双腿交替摆动
      role.rig.leftArm.rotation.x = Math.sin(time * 9.5) * 0.45
      role.rig.rightArm.rotation.x = -Math.sin(time * 9.5) * 0.45
      role.rig.leftLeg.rotation.x = -Math.sin(time * 9.5) * 0.38
      role.rig.rightLeg.rotation.x = Math.sin(time * 9.5) * 0.38
      return
    }

    // 站立/静坐阶段：
    role.rig.root.position.copy(role.target)
    role.rig.root.rotation.z = 0
    role.rig.leftLeg.rotation.x = 0
    role.rig.rightLeg.rotation.x = 0
    this.face(role.rig.root, role.lookAt)

    // 活跃状态下，手腕产生打字的微幅敲击律动，或工牌的立体跃动
    const armWave = active ? Math.sin(time * 6.5) * 0.14 : 0
    role.rig.rightArm.rotation.x = -0.1 + armWave
    role.rig.leftArm.rotation.x = 0.06 - armWave

    if (role.rig.badge) {
      const scale = active ? 1 + Math.sin(time * 5) * 0.08 : 1
      role.rig.badge.scale.setScalar(scale)
    }
  }

  // 自发光屏的微光渐变脉动
  private animateScreens(time: number) {
    this.screens.forEach((screens, role) => {
      const intense =
        role === this.state.activeRole ||
        (role === 'browser' && this.state.phase === 'working') ||
        (role === 'ops' && this.state.phase === 'usage') ||
        (role === 'qa' && this.state.phase === 'error')
      
      // 脉冲发光
      const pulse = Math.sin(time * 6.8) * 0.16 + 0.84

      screens.forEach((screen) => {
        const mat = screen.material as THREE.MeshStandardMaterial
        mat.emissiveIntensity = intense ? 0.85 * pulse : 0.12
      })
    })
  }

  private face(group: THREE.Group, target: THREE.Vector3) {
    const dx = target.x - group.position.x
    const dz = target.z - group.position.z
    group.rotation.y = Math.atan2(dx, dz) + Math.PI
  }

  // 将 3D 角色的坐标投影到 2D HUD 上悬浮对话气泡 (Speech Bubble)
  private projectAnchors() {
    const width = this.container.clientWidth
    const height = this.container.clientHeight
    const nextAnchors = { ...this.anchors }

    this.roles.forEach((role, key) => {
      this.scratchVector.copy(role.rig.root.position)
      // 气泡飘在精致方块人的头顶 (y 轴 1.85 米处)
      this.scratchVector.y += 1.85
      this.scratchVector.project(this.camera)
      const visible = this.scratchVector.z <= 1
      nextAnchors[key] = visible
        ? {
            x: (this.scratchVector.x * 0.5 + 0.5) * width,
            y: (-this.scratchVector.y * 0.5 + 0.5) * height,
            visible: true
          }
        : hiddenPoint()
    })

    this.anchors = nextAnchors
    this.onAnchorsChanged(nextAnchors)
  }

  // 绝对固定的俯视 Isometric 视角镜头
  private updateCamera() {
    this.camera.position.x = this.radius * Math.sin(this.theta) * Math.cos(this.phi)
    this.camera.position.y = this.radius * Math.sin(this.phi) + 1.25
    this.camera.position.z = this.radius * Math.cos(this.theta) * Math.cos(this.phi)
    
    // 聚焦于办公区的对称中心，使得画面构图极度匀称优雅
    this.camera.lookAt(0.3, 0.42, 0)
  }

  private resize = () => {
    const width = Math.max(this.container.clientWidth, 1)
    const height = Math.max(this.container.clientHeight, 1)
    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height)
  }

  // 彻底剔除了 onMouseDown / onMouseMove / onMouseUp 事件，杜绝任何拖拽视角的交互
  private bindEvents() {
    window.addEventListener('resize', this.resize)
    this.resizeObserver?.disconnect()
    this.resizeObserver = new ResizeObserver(this.resize)
    this.resizeObserver.observe(this.container)
  }

  private unbindEvents() {
    window.removeEventListener('resize', this.resize)
    this.resizeObserver?.disconnect()
    this.resizeObserver = null
  }
}
