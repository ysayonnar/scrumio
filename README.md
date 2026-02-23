# Scrumio

**Scrumio** is a lightweight backend service for managing projects and tasks based on the Scrum methodology.  
It provides support for backlogs, boards, sprints, and tickets, offering a full CRUD workflow with a clear project
structure.

---

## Core Features

- Project management
- Product backlog management
- Sprint planning
- Scrum board support
- Ticket lifecycle management
- Status tracking
- Soft delete for all entities
- RESTful API
- Validation and structured error handling

---

## Domain Model

The system is built around the following core entities:

- User
- Project
- Backlog
- Sprint
- Board
- Ticket
- Status
- Comment (if implemented)

All entities support **soft delete** using a logical deletion field (`deleted_at` or `is_deleted`).

---

## Ticket Lifecycle

1. Ticket is created in the Product Backlog
2. It can be assigned to a Sprint
3. It moves across Board statuses (`TODO → IN_PROGRESS → DONE`)
4. It can be updated, reassigned, or archived
5. It can be soft-deleted

---

## Architecture

Scrumio follows a layered architecture:

## DB scheme

Here you can see db scheme, all tables support soft-delete
![scheme](scrumio-final.png)