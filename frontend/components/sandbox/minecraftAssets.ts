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

export const createMaterial = (
  color: number,
  options: {
    emissive?: number
    emissiveIntensity?: number
    roughness?: number
    metalness?: number
  } = {}
) => {
  const key = `${color}-${options.emissive || 0}-${options.emissiveIntensity || 0}-${options.roughness || 0.82}-${options.metalness || 0}`
  const cached = materialCache.get(key)
  if (cached) return cached

  const mat = new THREE.MeshStandardMaterial({
    color,
    emissive: options.emissive || 0x000000,
    emissiveIntensity: options.emissiveIntensity || 0,
    roughness: options.roughness ?? 0.82,
    metalness: options.metalness ?? 0
  })
  materialCache.set(key, mat)
  return mat
}

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

const palette: Record<SandboxRole, {
  hair: number
  shirt: number
  trim: number
  pants: number
  shoes: number
  accessory: number
}> = {
  pm: {
    hair: 0x4b2a13,
    shirt: 0xf7f4eb,
    trim: 0x1e5c97,
    pants: 0x25272b,
    shoes: 0x56361d,
    accessory: 0x1e5c97
  },
  planner: {
    hair: 0x2e1b0e,
    shirt: 0x163d63,
    trim: 0xe8c37a,
    pants: 0x191b20,
    shoes: 0x101114,
    accessory: 0x2d6fa3
  },
  browser: {
    hair: 0x2c1b10,
    shirt: 0x17191d,
    trim: 0xeff3f4,
    pants: 0x2e5676,
    shoes: 0xd9dee2,
    accessory: 0x64b8c7
  },
  qa: {
    hair: 0x4d2a12,
    shirt: 0xe7bd31,
    trim: 0x1e5c97,
    pants: 0x214a68,
    shoes: 0xf1f1e9,
    accessory: 0x1e5c97
  },
  ops: {
    hair: 0x151515,
    shirt: 0x121418,
    trim: 0x24394f,
    pants: 0x242629,
    shoes: 0x0e0f11,
    accessory: 0x2b7dbd
  }
}

export const createMinecraftCharacter = (role: SandboxRole): RoleRig => {
  const colors = palette[role]
  const root = new THREE.Group()
  const head = new THREE.Group()

  addBox(root, 0.36, 0.58, 0.18, colors.shirt, [0, 0.92, 0])
  addBox(root, 0.38, 0.12, 0.2, colors.trim, [0, 1.15, -0.01])

  const skin = 0xf2c083
  const leftArm = addBox(root, 0.13, 0.52, 0.15, colors.shirt, [-0.29, 0.9, 0])
  const rightArm = addBox(root, 0.13, 0.52, 0.15, colors.shirt, [0.29, 0.9, 0])
  addBox(root, 0.14, 0.14, 0.16, skin, [-0.29, 0.58, 0])
  addBox(root, 0.14, 0.14, 0.16, skin, [0.29, 0.58, 0])

  const leftLeg = addBox(root, 0.16, 0.48, 0.16, colors.pants, [-0.1, 0.34, 0])
  const rightLeg = addBox(root, 0.16, 0.48, 0.16, colors.pants, [0.1, 0.34, 0])
  addBox(root, 0.17, 0.1, 0.2, colors.shoes, [-0.1, 0.07, 0.02])
  addBox(root, 0.17, 0.1, 0.2, colors.shoes, [0.1, 0.07, 0.02])

  addBox(head, 0.44, 0.44, 0.44, skin, [0, 1.42, 0])
  addBox(head, 0.46, 0.18, 0.46, colors.hair, [0, 1.62, -0.02])
  addBox(head, 0.48, 0.14, 0.18, colors.hair, [0, 1.5, -0.22])
  addBox(head, 0.08, 0.08, 0.03, 0x1f2933, [-0.1, 1.43, -0.235])
  addBox(head, 0.08, 0.08, 0.03, 0x1f2933, [0.1, 1.43, -0.235])

  if (role === 'planner') {
    addBox(head, 0.38, 0.08, 0.035, 0x111827, [0, 1.45, -0.245])
  }

  if (role === 'qa') {
    addBox(head, 0.18, 0.06, 0.46, 0xe7bd31, [0.18, 1.63, -0.02])
  }

  root.add(head)

  const badge = addBox(root, 0.1, 0.14, 0.025, 0xf4f7f8, [0.11, 1.02, -0.105])
  addBox(root, 0.025, 0.23, 0.02, colors.accessory, [0.06, 1.12, -0.115])
  addBox(root, 0.025, 0.23, 0.02, colors.accessory, [0.16, 1.12, -0.115])

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

export const createWorkstation = (role: SandboxRole, accent: number) => {
  const root = new THREE.Group()
  const screens: THREE.Mesh[] = []

  addBox(root, 1.55, 0.14, 0.88, 0xc88c42, [0, 0.6, 0])
  addBox(root, 0.12, 0.6, 0.12, 0x7b542c, [-0.65, 0.28, -0.32])
  addBox(root, 0.12, 0.6, 0.12, 0x7b542c, [0.65, 0.28, -0.32])
  addBox(root, 0.12, 0.6, 0.12, 0x7b542c, [-0.65, 0.28, 0.32])
  addBox(root, 0.12, 0.6, 0.12, 0x7b542c, [0.65, 0.28, 0.32])

  const screenCount = role === 'browser' ? 2 : 1
  for (let i = 0; i < screenCount; i += 1) {
    const offsetX = screenCount === 2 ? (i === 0 ? -0.24 : 0.24) : 0
    addBox(root, 0.46, 0.08, 0.08, 0x2a2f35, [offsetX, 0.73, -0.24])
    const screen = addBox(root, 0.46, 0.32, 0.04, 0x111820, [offsetX, 0.93, -0.32], {
      emissive: accent,
      emissiveIntensity: 0.25
    })
    screens.push(screen)
    addBox(root, 0.34, 0.03, 0.05, 0x2a2f35, [offsetX, 0.74, -0.31])
  }

  addBox(root, 0.58, 0.04, 0.2, 0x24282e, [0, 0.7, 0.08])
  addBox(root, 0.16, 0.05, 0.12, 0x2f3338, [0.45, 0.7, 0.08])
  addBox(root, 0.12, 0.14, 0.12, 0xf7f2e3, [-0.5, 0.73, 0.14])

  return {
    role,
    root,
    screens
  } satisfies WorkstationRig
}

export const createOfficeChair = () => {
  const root = new THREE.Group()
  addBox(root, 0.5, 0.12, 0.48, 0x202225, [0, 0.36, 0])
  addBox(root, 0.48, 0.56, 0.12, 0x202225, [0, 0.72, 0.2])
  addBox(root, 0.1, 0.42, 0.1, 0x121315, [0, 0.16, 0])
  addBox(root, 0.76, 0.08, 0.1, 0x121315, [0, 0.05, 0])
  addBox(root, 0.1, 0.08, 0.76, 0x121315, [0, 0.05, 0])
  return root
}

export const createWhiteboard = () => {
  const root = new THREE.Group()
  addBox(root, 1.55, 0.95, 0.08, 0xe8ece9, [0, 1.18, 0])
  addBox(root, 1.65, 0.08, 0.1, 0x5a6268, [0, 1.68, 0])
  addBox(root, 1.65, 0.08, 0.1, 0x5a6268, [0, 0.68, 0])
  addBox(root, 0.12, 0.18, 0.03, 0xf1c94c, [-0.45, 1.23, -0.06])
  addBox(root, 0.12, 0.18, 0.03, 0x4f9f61, [-0.2, 1.09, -0.06])
  addBox(root, 0.12, 0.18, 0.03, 0x3578c6, [0.08, 1.3, -0.06])
  addBox(root, 0.5, 0.04, 0.03, 0x22262a, [0.35, 1.0, -0.06])
  addBox(root, 0.38, 0.04, 0.03, 0x22262a, [0.3, 1.16, -0.06])
  return root
}

export const createRouterRack = () => {
  const root = new THREE.Group()
  addBox(root, 0.9, 0.24, 0.55, 0x1a1e23, [0, 0.24, 0])
  addBox(root, 0.08, 0.7, 0.08, 0x0d0e10, [-0.32, 0.74, -0.16])
  addBox(root, 0.08, 0.7, 0.08, 0x0d0e10, [0.32, 0.74, -0.16])
  addBox(root, 0.09, 0.06, 0.04, 0x68d391, [-0.26, 0.27, -0.29], { emissive: 0x22c55e, emissiveIntensity: 0.6 })
  addBox(root, 0.09, 0.06, 0.04, 0x68d391, [-0.1, 0.27, -0.29], { emissive: 0x22c55e, emissiveIntensity: 0.6 })
  addBox(root, 0.09, 0.06, 0.04, 0xeab308, [0.06, 0.27, -0.29], { emissive: 0xeab308, emissiveIntensity: 0.35 })
  return root
}

export const createPlant = () => {
  const root = new THREE.Group()
  addBox(root, 0.34, 0.34, 0.34, 0x9a6b3a, [0, 0.17, 0])
  addBox(root, 0.12, 0.54, 0.12, 0x3f7f32, [0, 0.55, 0])
  addBox(root, 0.28, 0.18, 0.16, 0x3d9635, [-0.18, 0.75, 0])
  addBox(root, 0.28, 0.18, 0.16, 0x4ca83e, [0.18, 0.88, 0.02])
  addBox(root, 0.16, 0.18, 0.28, 0x58b34b, [0.02, 0.98, -0.18])
  return root
}

export const createFileCabinet = () => {
  const root = new THREE.Group()
  addBox(root, 0.62, 0.9, 0.52, 0xb97c35, [0, 0.45, 0])
  addBox(root, 0.52, 0.24, 0.04, 0x8b5a26, [0, 0.67, -0.27])
  addBox(root, 0.52, 0.24, 0.04, 0x8b5a26, [0, 0.36, -0.27])
  addBox(root, 0.22, 0.04, 0.04, 0x2c2f33, [0, 0.69, -0.3])
  addBox(root, 0.22, 0.04, 0.04, 0x2c2f33, [0, 0.38, -0.3])
  addBox(root, 0.12, 0.42, 0.18, 0x275e9b, [-0.16, 1.14, 0])
  addBox(root, 0.12, 0.42, 0.18, 0xe3b341, [0, 1.14, 0])
  addBox(root, 0.12, 0.42, 0.18, 0x20252b, [0.16, 1.14, 0])
  return root
}

export const createFloor = () => {
  const root = new THREE.Group()
  addBox(root, 7.6, 0.14, 6.4, 0xc9c6b8, [0, -0.07, 0])

  for (let x = -3; x <= 3; x += 1) {
    addBox(root, 0.025, 0.012, 6.4, 0x9f9b8d, [x, 0.01, 0])
  }
  for (let z = -3; z <= 3; z += 1) {
    addBox(root, 7.6, 0.012, 0.025, 0x9f9b8d, [0, 0.012, z])
  }

  return root
}

export const disposeSharedMaterials = () => {
  materialCache.forEach((mat) => mat.dispose())
  materialCache.clear()
}
