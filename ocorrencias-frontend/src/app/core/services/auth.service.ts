import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../models';

const TOKEN_KEY = 'occ_jwt';
const USER_KEY  = 'occ_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private _email = signal<string | null>(localStorage.getItem(USER_KEY));

  readonly token   = this._token.asReadonly();
  readonly email   = this._email.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());

  constructor(private http: HttpClient, private router: Router) {}

  login(body: LoginRequest) {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/api/v1/auth/login`, body).pipe(
      tap(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(USER_KEY, body.email);
        this._token.set(res.token);
        this._email.set(body.email);
      })
    );
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._token.set(null);
    this._email.set(null);
    this.router.navigate(['/login']);
  }
}
