import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface WeightEntry {
  key: string;
  value: string;
  enabled?: boolean;
}

interface NotificationTemplate {
  id?: number;
  templateKey: string;
  templateBody: string;
}

@Component({
  selector: 'app-admin-console',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './console.component.html',
  styleUrls: ['./console.component.css']
})
export class ConsoleComponent implements OnInit {
  weights: WeightEntry[] = [];
  templates: NotificationTemplate[] = [];
  loading = false;
  message = '';

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.http.get<WeightEntry[]>('/api/admin/maintenance/weights').subscribe({
      next: (weights) => {
        this.weights = weights;
        this.http.get<NotificationTemplate[]>('/api/admin/maintenance/templates').subscribe({
          next: (templates) => {
            this.templates = templates;
            this.loading = false;
          },
          error: () => {
            this.loading = false;
            this.message = 'Failed to load templates.';
          }
        });
      },
      error: () => {
        this.loading = false;
        this.message = 'Failed to load weights.';
      }
    });
  }

  saveWeights(): void {
    this.http.put('/api/admin/maintenance/weights', this.weights).subscribe({
      next: () => this.message = 'Search ranking weights updated.',
      error: () => this.message = 'Could not update ranking weights.'
    });
  }

  saveTemplates(): void {
    this.http.put('/api/admin/maintenance/templates', this.templates).subscribe({
      next: () => this.message = 'Notification templates updated.',
      error: () => this.message = 'Could not update templates.'
    });
  }
}
