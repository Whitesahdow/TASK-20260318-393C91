import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

interface WorkflowTask {
  id: number;
  title: string;
  status: string;
  branch: string;
  progress: number;
}

@Component({
  selector: 'app-dispatcher',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dispatcher.component.html',
  styleUrls: ['./dispatcher.component.css']
})
export class DispatcherComponent implements OnInit {
  tasks: WorkflowTask[] = [];
  selectedTaskIds: number[] = [];
  loading = false;
  error = '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    this.error = '';
    this.http.get<WorkflowTask[]>('/api/dispatcher/workflow/tasks').subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to load dispatcher tasks.';
      }
    });
  }

  toggleSelection(taskId: number): void {
    if (this.selectedTaskIds.includes(taskId)) {
      this.selectedTaskIds = this.selectedTaskIds.filter((id) => id !== taskId);
    } else {
      this.selectedTaskIds = [...this.selectedTaskIds, taskId];
    }
  }

  returnTask(taskId: number): void {
    this.http.post(`/api/dispatcher/workflow/tasks/${taskId}/return`, {}).subscribe({
      next: () => this.loadTasks(),
      error: () => this.error = 'Failed to return task.'
    });
  }

  batchApprove(): void {
    if (this.selectedTaskIds.length === 0) {
      return;
    }
    this.http.post('/api/dispatcher/workflow/tasks/batch-approve', { taskIds: this.selectedTaskIds }).subscribe({
      next: () => {
        this.selectedTaskIds = [];
        this.loadTasks();
      },
      error: () => this.error = 'Batch approval failed.'
    });
  }
}