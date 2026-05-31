import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { DialogComponent } from '../dialog/dialog';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-url-form',
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatDatepickerModule, MatNativeDateModule, CommonModule],
  templateUrl: './url-form.html',
  styleUrl: './url-form.css',
})
export class UrlForm implements OnInit {
  longUrl: string = '';
  customAlias: string = '';
  expirationDate: Date | null = null;
  username: string = '';
  userUrls: any[] = [];
  isLoading: boolean = true;

  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit() {
    this.username = localStorage.getItem('username') || 'User';
    if (!this.username || this.username === 'User') {
      this.router.navigate(['/login']);
      return;
    }
    this.loadUserUrls();
  }

  loadUserUrls() {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Missing auth token. Please log in first.');
      this.router.navigate(['/login']);
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    this.http.get('/api/user-urls', { headers })
      .subscribe({
        next: (response: any) => {
          this.userUrls = response;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error fetching user URLs', error);
          this.isLoading = false;
        }
      });
  }

  onSubmit() {
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('Missing auth token. Please log in first.');
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    const body = {
      long_url: this.longUrl,
      custom_alias: this.customAlias || null,
      expiration_date: this.expirationDate ? this.expirationDate.toISOString() : null
    };

    this.http.post('/api/urls', body, { headers })
      .subscribe({
        next: (response: any) => {
          this.dialog.open(DialogComponent, {
            data: { shortUrl: response.short_url }
          });
          this.longUrl = '';
          this.customAlias = '';
          this.expirationDate = null;
          this.loadUserUrls();
        },
        error: (error) => {
          console.error('Error creating URL', error);
        }
      });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
