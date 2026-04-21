import { bootstrapApplication } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, provideHttpClient } from '@angular/common/http';

interface StopRecord {
  route: string;
  stopName: string;
  pinyinInitials: string;
  frequencyPriority: number;
  popularityScore: number;
}

interface MessageItem {
  title: string;
  detail: string;
  minutesAgo: number;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app/app.component.html',
  styleUrls: ['./app/app.component.css']
})
class AppComponent implements OnInit {
  constructor(private readonly http: HttpClient) {}

  searchTerm = '';
  searchResults: StopRecord[] = [];
  suggestions: string[] = [];

  reminderEnabled = true;
  dndStart = '22:00';
  dndEnd = '07:00';
  leadTimeMinutes = 10;

  backendStatus = 'CHECKING';
  backendService = 'Loading...';
  traceId = 'N/A';

  readonly stopData: StopRecord[] = [
    { route: 'B12', stopName: 'Central Avenue', pinyinInitials: 'CA', frequencyPriority: 95, popularityScore: 80 },
    { route: 'B12', stopName: 'Maple Garden', pinyinInitials: 'MG', frequencyPriority: 95, popularityScore: 72 },
    { route: 'M08', stopName: 'East Market', pinyinInitials: 'EM', frequencyPriority: 88, popularityScore: 70 },
    { route: 'M08', stopName: 'Green Lake Plaza', pinyinInitials: 'GLP', frequencyPriority: 88, popularityScore: 75 },
    { route: 'K66', stopName: 'North Harbor', pinyinInitials: 'NH', frequencyPriority: 70, popularityScore: 84 },
    { route: 'K66', stopName: 'Sunrise Residence', pinyinInitials: 'SR', frequencyPriority: 70, popularityScore: 92 },
    { route: 'A21', stopName: 'Riverside Hub', pinyinInitials: 'RH', frequencyPriority: 83, popularityScore: 77 }
  ];

  readonly messages: MessageItem[] = [
    { title: 'Reservation Confirmed', detail: 'B12 at Central Avenue is confirmed for 18:30.', minutesAgo: 2 },
    { title: 'Arrival Reminder', detail: 'Your stop Maple Garden is approximately 10 minutes away.', minutesAgo: 6 },
    { title: 'Missed Check-In', detail: 'You did not check in within 5 minutes after schedule start.', minutesAgo: 14 }
  ];

  readonly dispatcherTasks = [
    { name: 'Route data change approval', branch: 'Routine', state: 'In Review', progress: 60 },
    { name: 'Reminder rule configuration', branch: 'Parallel', state: 'Awaiting Co-Approval', progress: 40 },
    { name: 'Abnormal data review', branch: 'Risky', state: 'Escalated < 24h', progress: 85 }
  ];

  readonly adminControls = [
    { key: 'Frequency weight', value: '0.6' },
    { key: 'Popularity weight', value: '0.4' },
    { key: 'Dictionary: Area unit', value: '㎡' },
    { key: 'Dictionary: Price unit', value: 'yuan/month' }
  ];

  ngOnInit(): void {
    this.refreshBackendStatus();
    this.applySearch();
  }

  applySearch(): void {
    const keyword = this.searchTerm.trim().toLowerCase();

    const filtered = this.stopData.filter((row) => {
      if (!keyword) {
        return true;
      }

      const normalizedRoute = row.route.toLowerCase();
      const normalizedStop = row.stopName.toLowerCase();
      const normalizedInitials = row.pinyinInitials.toLowerCase();

      return (
        normalizedRoute.includes(keyword) ||
        normalizedStop.includes(keyword) ||
        normalizedInitials.startsWith(keyword)
      );
    });

    const deduplicated = new Map<string, StopRecord>();
    for (const item of filtered) {
      const id = `${item.route}|${item.stopName}`;
      if (!deduplicated.has(id)) {
        deduplicated.set(id, item);
      }
    }

    this.searchResults = Array.from(deduplicated.values()).sort((a, b) => {
      const scoreA = a.frequencyPriority * 0.6 + a.popularityScore * 0.4;
      const scoreB = b.frequencyPriority * 0.6 + b.popularityScore * 0.4;
      return scoreB - scoreA;
    });

    this.suggestions = this.searchResults.slice(0, 5).map((entry) => `${entry.route} · ${entry.stopName}`);
  }

  useSuggestion(label: string): void {
    const stopName = label.split('·')[1]?.trim();
    if (!stopName) {
      return;
    }
    this.searchTerm = stopName;
    this.applySearch();
  }

  refreshBackendStatus(): void {
    this.http.get<{ status: string; service: string }>('/api/health', { observe: 'response' }).subscribe({
      next: (response) => {
        this.backendStatus = response.body?.status ?? 'UNKNOWN';
        this.backendService = response.body?.service ?? 'Unknown service';
        this.traceId = response.headers.get('X-Trace-ID') ?? 'N/A';
      },
      error: () => {
        this.backendStatus = 'DOWN';
        this.backendService = 'Cannot reach backend';
        this.traceId = 'N/A';
      }
    });
  }
}

bootstrapApplication(AppComponent, {
  providers: [provideHttpClient()]
}).catch((err) => console.error(err));
