import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

interface ImportAuditLog {
  timestamp: string;
  traceId: string;
  result: string;
  changesMade: string;
  stopName: string;
  sourceType: string;
}

interface ConfigEntry {
  key: string;
  value: string;
  enabled?: boolean;
}

interface TemplateImportRequest {
  templateType: string;
  payload: string;
  mappings: Record<string, string>;
}

@Component({
  selector: 'app-dictionary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dictionary.component.html',
  styleUrls: ['./dictionary.component.css']
})
export class DictionaryComponent implements OnInit {
  importAuditLogs: ImportAuditLog[] = [];
  loadingAudit = false;
  error = '';

  importFeedback = '';
  templateType: 'JSON' | 'HTML' = 'JSON';
  templatePayload = `{
  "apt_type": "2BR",
  "stop_name": "Audit Stop",
  "address": "North Road",
  "res_area": "Garden Court",
  "size": 500,
  "size_unit": "sqft",
  "rent": "5600 yuan/month"
}`;

  mappings: Array<{ sourceKey: string; targetField: string }> = [
    { sourceKey: 'stop_name', targetField: 'name' },
    { sourceKey: 'address', targetField: 'address' },
    { sourceKey: 'res_area', targetField: 'residentialArea' },
    { sourceKey: 'apt_type', targetField: 'apartmentType' },
    { sourceKey: 'size', targetField: 'area' },
    { sourceKey: 'size_unit', targetField: 'unit' },
    { sourceKey: 'rent', targetField: 'price' }
  ];

  dictionaries: ConfigEntry[] = [];
  rules: ConfigEntry[] = [];
  loadingConfig = false;
  savingConfig = false;

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loadAuditLogs();
    this.loadConfig();
  }

  loadAuditLogs(): void {
    this.loadingAudit = true;
    this.error = '';
    this.http.get<ImportAuditLog[]>('/api/admin/stops/audit/imports').subscribe({
      next: (logs) => {
        this.importAuditLogs = logs;
        this.loadingAudit = false;
      },
      error: () => {
        this.importAuditLogs = [];
        this.loadingAudit = false;
        this.error = 'Unable to load cleaning audit logs.';
      }
    });
  }

  loadConfig(): void {
    this.loadingConfig = true;
    this.http.get<any[]>('/api/admin/stops/config/dictionaries').subscribe({
      next: (entries) => {
        this.dictionaries = entries.map((x) => ({ key: x.dictKey, value: x.dictValue }));
        this.http.get<any[]>('/api/admin/stops/config/rules').subscribe({
          next: (rules) => {
            this.rules = rules.map((x) => ({ key: x.ruleKey, value: x.ruleValue, enabled: x.enabled }));
            this.loadingConfig = false;
          },
          error: () => {
            this.loadingConfig = false;
            this.error = 'Unable to load cleaning rules.';
          }
        });
      },
      error: () => {
        this.loadingConfig = false;
        this.error = 'Unable to load field dictionaries.';
      }
    });
  }

  runTemplateImport(): void {
    this.importFeedback = '';

    if (!this.templatePayload.trim()) {
      this.importFeedback = 'Please provide template payload.';
      return;
    }

    const mappings: Record<string, string> = {};
    for (const entry of this.mappings) {
      if (entry.sourceKey.trim() && entry.targetField.trim()) {
        mappings[entry.sourceKey.trim()] = entry.targetField.trim();
      }
    }

    const request: TemplateImportRequest = {
      templateType: this.templateType,
      payload: this.templatePayload,
      mappings
    };

    this.http.post<any>('/api/admin/stops/import-template', request).subscribe({
      next: (response) => {
        this.importFeedback = `Import success for ${response.stopName} (version ${response.versionNumber}).`;
        this.loadAuditLogs();
      },
      error: () => {
        this.importFeedback = 'Template import failed. Check payload and mapping.';
      }
    });
  }

  saveDictionaries(): void {
    this.savingConfig = true;
    this.http.put('/api/admin/stops/config/dictionaries', this.dictionaries).subscribe({
      next: () => {
        this.savingConfig = false;
        this.importFeedback = 'Dictionary standards updated.';
      },
      error: () => {
        this.savingConfig = false;
        this.error = 'Failed to update dictionary standards.';
      }
    });
  }

  saveRules(): void {
    this.savingConfig = true;
    this.http.put('/api/admin/stops/config/rules', this.rules).subscribe({
      next: () => {
        this.savingConfig = false;
        this.importFeedback = 'Cleaning rules updated.';
      },
      error: () => {
        this.savingConfig = false;
        this.error = 'Failed to update cleaning rules.';
      }
    });
  }
}
