interface BadgeProps {
  label: string
  variant?: 'default' | 'green' | 'yellow' | 'red' | 'blue' | 'purple' | 'gray'
}

const variantClasses: Record<string, string> = {
  default: 'bg-indigo-100 text-indigo-700',
  green: 'bg-green-100 text-green-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  red: 'bg-red-100 text-red-700',
  blue: 'bg-blue-100 text-blue-700',
  purple: 'bg-purple-100 text-purple-700',
  gray: 'bg-gray-100 text-gray-700',
}

export function Badge({ label, variant = 'default' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${variantClasses[variant]}`}>
      {label}
    </span>
  )
}

export function ticketStatusVariant(status: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    BACKLOG: 'gray',
    TODO: 'blue',
    IN_PROGRESS: 'yellow',
    ON_HOLD: 'red',
    ON_REVIEW: 'purple',
    DONE: 'green',
  }
  return map[status] ?? 'default'
}

export function ticketPriorityVariant(priority: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    LOW: 'green',
    MEDIUM: 'yellow',
    HIGH: 'red',
  }
  return map[priority] ?? 'default'
}

export function sprintStatusVariant(status: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    PLANNED: 'blue',
    ACTIVE: 'green',
    COMPLETED: 'gray',
  }
  return map[status] ?? 'default'
}

export function memberRoleVariant(role: string): BadgeProps['variant'] {
  const map: Record<string, BadgeProps['variant']> = {
    OWNER: 'red',
    MANAGER: 'purple',
    DEVELOPER: 'blue',
    STAKEHOLDER: 'yellow',
  }
  return map[role] ?? 'default'
}
