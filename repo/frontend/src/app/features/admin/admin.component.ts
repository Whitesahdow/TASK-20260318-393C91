import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DictionaryComponent } from './dictionary/dictionary.component';
import { ConsoleComponent } from './console/console.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, DictionaryComponent, ConsoleComponent],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent {

}