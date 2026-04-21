import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DictionaryComponent } from './dictionary/dictionary.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, DictionaryComponent],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent {
  readonly controls = [
    { key: 'Access matrix', value: 'RBAC active for all routes' },
    { key: 'Default admin account', value: 'admin / admin1234' },
    { key: 'Password policy', value: 'Minimum 8 characters' },
    { key: 'Hashing', value: 'BCrypt with salted hashes' }
  ];

}