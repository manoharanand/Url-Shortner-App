import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [NgIf, FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  email: string = '';
  password: string = '';
  errorMessage: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  onLogin() {
    this.http.post('/api/login', { email: this.email, password: this.password })
      .subscribe({
        next: (response: any) => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('username', response.username);
          this.router.navigate(['/url-form']);
        },
        error: (error) => {
          this.errorMessage = 'Invalid credentials';
        }
      });
  }

  onRegisterRedirect() {
    this.router.navigate(['/register']);
  }
}

