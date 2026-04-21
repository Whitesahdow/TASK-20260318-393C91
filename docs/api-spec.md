City Bus Platform API Specification
Base path: /api/v1
Method	Path	Auth	Description
POST	/auth/register	Public	Register local user account (Min 8 char password)
POST	/auth/login	Public	Authenticate and return user role + session
GET	/auth/me	Auth	Get current authenticated user and role
Authentication Flow
Primary auth is handled via Local Username/Password Verification.
Passwords must be at least 8 characters and are stored using Salted BCrypt Hashing.
Every API response includes an X-Trace-ID header for structured logging and observability.
Offline LAN constraint: Authentication is entirely self-contained within the Spring Boot/PostgreSQL environment.
code
Http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "passenger01",
  "password": "SecurePassword123!",
  "role": "PASSENGER"
}
Search & Stops (Passenger)
Method	Path	Auth	Description
GET	/search/autocomplete	Auth	Get suggestions (Min 2 chars, Pinyin/Initials support)
GET	/search/results	Auth	Get deduplicated stops sorted by Frequency + Popularity
GET	/stops/{stopId}/metadata	Auth	View stop details (Housing fields desensitized)
Search Ranking Logic
Results are ranked using a weighted formula: Score = (Frequency * W_f) + (Popularity * W_p).
Autocomplete Trigger: Logic is suppressed until the user enters at least 2 characters.
Pinyin Support: Search matches pinyin_initials and pinyin_full fields in the database.
Notifications & Preferences
Method	Path	Auth	Description
GET	/notifications	Auth	List unified notifications from Message Center
PATCH	/notifications/preferences	Auth	Toggle arrival reminders and set DND windows
POST	/notifications/reservations	Auth	Create a reservation and queue a reminder
Do Not Disturb (DND) Window
PATCH /notifications/preferences accepts dnd_start and dnd_end (e.g., 22:00, 07:00).
Scheduled tasks will suppress notifications if the current time falls within this window.
Workflow & Dispatching (Dispatcher)
Method	Path	Auth	Description
GET	/dispatch/tasks	Dispatcher	List approval tasks with visual progress status
PATCH	/dispatch/tasks/{taskId}/approve	Dispatcher	Approve or Return task (Conditional Branching)
POST	/dispatch/tasks/batch-approve	Dispatcher	Batch process selected routine approvals
Workflow Constraints
Conditional Branching: Tasks are automatically categorized as ROUTINE or RISKY (e.g., GPS coord changes).
Escalation: Tasks unprocessed for 24 hours trigger an escalation warning to Admin.
Data Integration & Maintenance (Admin)
Method	Path	Auth	Description
POST	/admin/import	Admin	Parse HTML/JSON templates and trigger cleaning
GET	/admin/audit-logs	Admin	View data cleaning history and source logs
PUT	/admin/dictionaries	Admin	Maintain field standard dictionaries (sqm, yuan)
PUT	/admin/ranking-weights	Admin	Adjust Frequency vs Popularity weights