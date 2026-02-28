CREATE INDEX idx_ticket_project_id ON ticket (project_id);
CREATE INDEX idx_ticket_sprint_id ON ticket (sprint_id);

CREATE INDEX idx_sprint_project_id ON sprint (project_id);

CREATE INDEX idx_meeting_project_id ON meeting (project_id);
CREATE INDEX idx_meeting_sprint_id ON meeting (sprint_id);

CREATE INDEX idx_project_member_user_id ON project_member (user_id);
CREATE INDEX idx_project_member_project_id ON project_member (project_id);