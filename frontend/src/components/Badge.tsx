interface BadgeProps {
  label: string
  variant?: 'default' | 'green' | 'yellow' | 'red' | 'blue' | 'purple' | 'gray'
}

export function Badge({ label, variant = 'default' }: BadgeProps) {
  return <span className={`badge badge-${variant}`}>{label}</span>
}

export function ticketStatusVariant(status: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    BACKLOG: 'gray', TODO: 'blue', IN_PROGRESS: 'yellow',
    ON_HOLD: 'red', ON_REVIEW: 'purple', DONE: 'green',
  }
  return map[status] ?? 'default'
}

export function ticketPriorityVariant(priority: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    LOW: 'green', MEDIUM: 'yellow', HIGH: 'red',
  }
  return map[priority] ?? 'default'
}

export function sprintStatusVariant(status: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    PLANNED: 'blue', ACTIVE: 'green', COMPLETED: 'gray',
  }
  return map[status] ?? 'default'
}

export function memberRoleVariant(role: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    OWNER: 'red', MANAGER: 'purple', DEVELOPER: 'blue', STAKEHOLDER: 'yellow',
  }
  return map[role] ?? 'default'
}
