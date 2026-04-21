import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

interface AuditRow {
  stopName: string;
  versionNumber: number;
  areaSqm: number | null;
  importedAt: string;
}

@Component({
  selector: 'app-dispatcher',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dispatcher.component.html',
  styleUrls: ['./dispatcher.component.css']
})
export class DispatcherComponent implements OnInit {
  readonly tasks = [
    { task: 'Approve route B12 schedule update', owner: 'Shift A', status: 'In Review', progress: 64 },
    { task: 'Resolve stop conflicts in East Market', owner: 'Shift C', status: 'Escalated', progress: 82 },
    { task: 'Validate evening reminder rules', owner: 'Shift B', status: 'Pending Approval', progress: 48 }
  ];

  reviewQueue: AuditRow[] = [];
  loading = false;
  error = '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadReviewQueue();
  }

  loadReviewQueue(): void {
    this.loading = true;
    this.error = '';
    this.http.get<AuditRow[]>('/api/admin/stops/audit').subscribe({
      next: (logs) => {
        this.reviewQueue = logs.slice(0, 5);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Unable to load abnormal data review queue.';
      }
    });
  }
}