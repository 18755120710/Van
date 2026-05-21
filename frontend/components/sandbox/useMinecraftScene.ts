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

const roleAccents: Record<SandboxRole, number> = {
  pm: 0x1e5c97,
  planner: 0x2d6fa3,
  browser: 0x38bdf8,
  qa: 0xeab308,
  ops: 0x22c55e
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
  private theta = Math.PI / 4
  private phi = Math.PI / 5
  private radius = 10.6
  private dragging = false
  private previousMouse = { x: 0, y: 0 }
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
    this.scene.background = null

    this.camera = new THREE.PerspectiveCamera(44, 1, 0.1, 100)
    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
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
    const ambient = new THREE.AmbientLight(0xffffff, 0.72)
    this.scene.add(ambient)

    const keyLight = new THREE.DirectionalLight(0xffffff, 1.25)
    keyLight.position.set(3.6, 6, 4.2)
    keyLight.castShadow = true
    keyLight.shadow.mapSize.set(1024, 1024)
    this.scene.add(keyLight)

    const fillLight = new THREE.PointLight(0x9ad5ff, 0.75, 9)
    fillLight.position.set(-2.6, 2.4, -1.8)
    this.scene.add(fillLight)

    this.scene.add(createFloor())
    this.buildOfficeProps()
    this.buildRoles()
  }

  private buildOfficeProps() {
    this.addStation('pm', [0, 0, -2.78], [0, 0, 0])
    this.addStation('planner', [-2.35, 0, 0.25], [0, 0, 0])
    this.addStation('browser', [2.35, 0, 0.25], [0, 0, 0])

    const whiteboard = createWhiteboard()
    whiteboard.position.set(-2.35, 0, 2.55)
    whiteboard.rotation.y = Math.PI
    this.scene.add(whiteboard)

    const router = createRouterRack()
    router.position.set(3.35, 0, 2.18)
    this.scene.add(router)

    const cabinet = createFileCabinet()
    cabinet.position.set(-3.35, 0, -2.45)
    cabinet.rotation.y = Math.PI / 8
    this.scene.add(cabinet)

    const leftPlant = createPlant()
    leftPlant.position.set(-3.35, 0, 1.72)
    this.scene.add(leftPlant)

    const rightPlant = createPlant()
    rightPlant.position.set(3.25, 0, -2.38)
    this.scene.add(rightPlant)
  }

  private addStation(role: SandboxRole, position: [number, number, number], rotation: [number, number, number]) {
    const station = createWorkstation(role, roleAccents[role])
    station.root.position.set(...position)
    station.root.rotation.set(...rotation)
    this.scene.add(station.root)
    this.screens.set(role, station.screens)

    const chair = createOfficeChair()
    chair.position.set(position[0], 0, position[2] + 0.72)
    this.scene.add(chair)
  }

  private buildRoles() {
    const home: Record<SandboxRole, THREE.Vector3> = {
      pm: new THREE.Vector3(0, 0, -1.9),
      planner: new THREE.Vector3(-2.35, 0, 1.02),
      browser: new THREE.Vector3(2.35, 0, 1.02),
      qa: new THREE.Vector3(-3.08, 0, 0.72),
      ops: new THREE.Vector3(3.08, 0, 1.55)
    }

    const lookAt: Record<SandboxRole, THREE.Vector3> = {
      pm: new THREE.Vector3(0, 0, -3),
      planner: new THREE.Vector3(-2.35, 0, 0.18),
      browser: new THREE.Vector3(2.35, 0, 0.18),
      qa: new THREE.Vector3(-2.35, 0, 2.55),
      ops: new THREE.Vector3(3.35, 0, 2.18)
    }

    ;(['pm', 'planner', 'browser', 'qa', 'ops'] as SandboxRole[]).forEach((role) => {
      const rig = createMinecraftCharacter(role)
      rig.root.position.copy(home[role])
      rig.root.scale.setScalar(role === 'pm' ? 0.86 : 0.82)
      this.scene.add(rig.root)
      this.roles.set(role, {
        rig,
        target: home[role].clone(),
        home: home[role],
        lookAt: lookAt[role]
      })
    })
  }

  private syncTargets() {
    this.roles.forEach((role) => {
      role.target.copy(role.home)
      role.lookAt.copy(role.home.clone().add(new THREE.Vector3(0, 0, -1)))
    })

    const planner = this.roles.get('planner')
    const browser = this.roles.get('browser')
    const qa = this.roles.get('qa')
    const ops = this.roles.get('ops')

    if (this.state.phase === 'handoff' && planner) {
      planner.target.set(1.35, 0, 0.9)
      planner.lookAt.set(2.35, 0, 1.02)
    }

    if (this.state.phase === 'working' && planner) {
      planner.target.set(1.35, 0, 0.9)
      planner.lookAt.set(2.35, 0, 1.02)
    }

    if (this.state.phase === 'reporting' && browser) {
      browser.target.set(-1.35, 0, 1.05)
      browser.lookAt.set(-2.35, 0, 1.02)
    }

    if (this.state.phase === 'answer' && planner) {
      planner.target.set(0.7, 0, -1.65)
      planner.lookAt.set(0, 0, -1.9)
    }

    if (this.state.phase === 'error' && qa) {
      qa.target.set(-1.25, 0, 0.88)
      qa.lookAt.set(0, 0, 0.5)
    }

    if (this.state.phase === 'usage' && ops) {
      ops.target.set(2.55, 0, 1.85)
      ops.lookAt.set(3.35, 0, 2.18)
    }
  }

  private animate = () => {
    this.animationId = requestAnimationFrame(this.animate)
    const time = performance.now() * 0.001

    this.roles.forEach((role, roleName) => this.animateRole(roleName, role, time))
    this.animateScreens(time)
    this.updateCamera()
    this.renderer.render(this.scene, this.camera)
    this.projectAnchors()
  }

  private animateRole(roleName: SandboxRole, role: SceneRole, time: number) {
    const active = roleName === this.state.activeRole
    const dir = role.target.clone().sub(role.rig.root.position)
    const distance = dir.length()

    if (distance > 0.035) {
      dir.normalize()
      role.rig.root.position.add(dir.multiplyScalar(0.045))
      role.rig.root.position.y = Math.abs(Math.sin(time * 9)) * 0.055
      role.rig.root.rotation.z = Math.sin(time * 9) * 0.035
      role.rig.root.rotation.y = Math.atan2(dir.x, dir.z) + Math.PI
      role.rig.leftArm.rotation.x = Math.sin(time * 9) * 0.45
      role.rig.rightArm.rotation.x = -Math.sin(time * 9) * 0.45
      role.rig.leftLeg.rotation.x = -Math.sin(time * 9) * 0.38
      role.rig.rightLeg.rotation.x = Math.sin(time * 9) * 0.38
      return
    }

    role.rig.root.position.copy(role.target)
    role.rig.root.rotation.z = 0
    role.rig.leftLeg.rotation.x = 0
    role.rig.rightLeg.rotation.x = 0
    this.face(role.rig.root, role.lookAt)

    const armWave = active ? Math.sin(time * 6) * 0.16 : 0
    role.rig.rightArm.rotation.x = -0.14 + armWave
    role.rig.leftArm.rotation.x = 0.08 - armWave

    if (role.rig.badge) {
      const scale = active ? 1 + Math.sin(time * 5) * 0.08 : 1
      role.rig.badge.scale.setScalar(scale)
    }
  }

  private animateScreens(time: number) {
    this.screens.forEach((screens, role) => {
      const intense =
        role === this.state.activeRole ||
        (role === 'browser' && this.state.phase === 'working') ||
        (role === 'ops' && this.state.phase === 'usage') ||
        (role === 'qa' && this.state.phase === 'error')
      const pulse = Math.sin(time * 7) * 0.18 + 0.82

      screens.forEach((screen) => {
        const mat = screen.material as THREE.MeshStandardMaterial
        mat.emissiveIntensity = intense ? 0.95 * pulse : 0.22
      })
    })
  }

  private face(group: THREE.Group, target: THREE.Vector3) {
    const dx = target.x - group.position.x
    const dz = target.z - group.position.z
    group.rotation.y = Math.atan2(dx, dz) + Math.PI
  }

  private projectAnchors() {
    const width = this.container.clientWidth
    const height = this.container.clientHeight
    const nextAnchors = { ...this.anchors }

    this.roles.forEach((role, key) => {
      this.scratchVector.copy(role.rig.root.position)
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

  private updateCamera() {
    this.camera.position.x = this.radius * Math.sin(this.theta) * Math.cos(this.phi)
    this.camera.position.y = this.radius * Math.sin(this.phi) + 1.35
    this.camera.position.z = this.radius * Math.cos(this.theta) * Math.cos(this.phi)
    this.camera.lookAt(0, 0.62, 0)
  }

  private resize = () => {
    const width = Math.max(this.container.clientWidth, 1)
    const height = Math.max(this.container.clientHeight, 1)
    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height)
  }

  private onMouseDown = (event: MouseEvent) => {
    this.dragging = true
    this.previousMouse = { x: event.clientX, y: event.clientY }
  }

  private onMouseMove = (event: MouseEvent) => {
    if (!this.dragging) return
    const deltaX = event.clientX - this.previousMouse.x
    const deltaY = event.clientY - this.previousMouse.y
    this.theta -= deltaX * 0.005
    this.phi = Math.max(0.16, Math.min(Math.PI / 2 - 0.16, this.phi - deltaY * 0.005))
    this.previousMouse = { x: event.clientX, y: event.clientY }
  }

  private onMouseUp = () => {
    this.dragging = false
  }

  private bindEvents() {
    this.renderer.domElement.addEventListener('mousedown', this.onMouseDown)
    window.addEventListener('mousemove', this.onMouseMove)
    window.addEventListener('mouseup', this.onMouseUp)
    window.addEventListener('resize', this.resize)
    this.resizeObserver?.disconnect()
    this.resizeObserver = new ResizeObserver(this.resize)
    this.resizeObserver.observe(this.container)
  }

  private unbindEvents() {
    this.renderer.domElement.removeEventListener('mousedown', this.onMouseDown)
    window.removeEventListener('mousemove', this.onMouseMove)
    window.removeEventListener('mouseup', this.onMouseUp)
    window.removeEventListener('resize', this.resize)
    this.resizeObserver?.disconnect()
    this.resizeObserver = null
  }
}
