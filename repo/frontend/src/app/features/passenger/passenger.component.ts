import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of, takeUntil } from 'rxjs';

interface SearchResult {
  stopId: number;
  stopName: string;
  initials: string;
  routeNumber: string;
  score: number;
  popularity: number;
  reminderEnabled?: boolean;
  arrivalEta?: string;
}

interface NotificationPreference {
  userId: number;
  arrivalRemindersEnabled: boolean;
  quietHoursEnabled: boolean;
  dndStart: string;
  dndEnd: string;
  leadTimeMinutes: number;
}

interface MessageTask {
  typeLabel: string;
  finalContent: string;
  scheduledAt: string;
  traceId: string;
  status: string;
}

@Component({
  selector: 'app-passenger',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './passenger.component.html',
  styleUrls: ['./passenger.component.css']
})
export class PassengerComponent implements OnInit, OnDestroy {
  searchQuery = '';
  suggestions: SearchResult[] = [];
  results: SearchResult[] = [];
  prefs: NotificationPreference = {
    userId: 0,
    arrivalRemindersEnabled: true,
    quietHoursEnabled: false,
    dndStart: '22:00',
    dndEnd: '07:00',
    leadTimeMinutes: 10
  };
  messages: MessageTask[] = [];
  loading = false;
  error = '';
  noResults = false;
  preferencesSaving = false;
  messagesLoading = false;
  currentUsername = '';
  private readonly queryInput$ = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.currentUsername = this.readCurrentUsername();
    if (this.currentUsername) {
      this.loadPreferences();
      this.loadMessages();
    }
    this.queryInput$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((query) => {
          if (!query || query.trim().length < 2) {
            this.suggestions = [];
            this.results = [];
            this.noResults = false;
            return of([] as SearchResult[]);
          }
          this.loading = true;
          this.error = '';
          this.noResults = false;
          return this.http.get<SearchResult[]>(`/api/v1/search/autocomplete?query=${encodeURIComponent(query.trim())}`);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (data) => {
          this.loading = false;
          this.suggestions = data;
          this.results = data.map((x) => ({ ...x, reminderEnabled: false }));
          this.noResults = this.searchQuery.trim().length >= 2 && this.results.length === 0;
        },
        error: () => {
          this.loading = false;
          this.error = 'Unable to fetch search results.';
          this.suggestions = [];
          this.results = [];
          this.noResults = false;
        }
      });
  }

  onInputChange(): void {
    this.queryInput$.next(this.searchQuery);
  }

  selectStop(suggestion: SearchResult): void {
    this.searchQuery = suggestion.stopName;
    this.suggestions = [];
    this.results = [{ ...suggestion, reminderEnabled: suggestion.reminderEnabled ?? false }];
    this.noResults = false;
  }

  toggleReminder(result: SearchResult): void {
    if (!this.currentUsername) {
      this.error = 'You must be logged in.';
      return;
    }
    if (!result.arrivalEta) {
      this.error = 'Please provide an arrival ETA for the reminder.';
      return;
    }
    this.http.post(
      `/api/v1/notifications/reservations`,
      { 
        stopName: result.stopName,
        arrivalEta: new Date(result.arrivalEta).toISOString()
      }
    ).subscribe({
      next: () => {
        result.reminderEnabled = true;
        this.loadMessages();
      },
      error: () => {
        this.error = 'Could not schedule reminder.';
      }
    });
  }

  savePreferences(): void {
    if (!this.currentUsername) {
      this.error = 'You must be logged in.';
      return;
    }
    this.preferencesSaving = true;
    this.http.put<NotificationPreference>(
      `/api/v1/notifications/preferences`,
      this.prefs
    ).subscribe({
      next: (saved) => {
        this.prefs = {
          ...saved,
          dndStart: this.toTimeInput(saved.dndStart),
          dndEnd: this.toTimeInput(saved.dndEnd)
        };
        this.preferencesSaving = false;
      },
      error: () => {
        this.preferencesSaving = false;
        this.error = 'Unable to save preferences.';
      }
    });
  }

  loadMessages(): void {
    if (!this.currentUsername) {
      return;
    }
    this.messagesLoading = true;
    this.http.get<MessageTask[]>(
      `/api/v1/notifications`
    ).subscribe({
      next: (data) => {
        this.messages = data;
        this.messagesLoading = false;
      },
      error: () => {
        this.messagesLoading = false;
      }
    });
  }

  private loadPreferences(): void {
    this.http.get<NotificationPreference>(
      `/api/v1/notifications/preferences`
    ).subscribe({
      next: (pref) => {
        this.prefs = {
          ...pref,
          dndStart: this.toTimeInput(pref.dndStart),
          dndEnd: this.toTimeInput(pref.dndEnd)
        };
      },
      error: () => {
        this.error = 'Unable to load notification preferences.';
      }
    });
  }

  private readCurrentUsername(): string {
    const raw = localStorage.getItem('user');
    if (!raw) {
      return '';
    }
    try {
      const parsed = JSON.parse(raw) as { username?: string };
      return parsed.username ?? '';
    } catch {
      return '';
    }
  }

  private toTimeInput(value: string | null | undefined): string {
    if (!value) return '22:00';
    return value.slice(0, 5);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.queryInput$.complete();
  }
}