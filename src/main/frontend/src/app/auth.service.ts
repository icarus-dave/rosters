import { Injectable }      from '@angular/core';
import { tokenNotExpired } from 'angular2-jwt';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Router } from '@angular/router';
import { WebConfigService } from './webconfig/webconfig.service';

// Avoid name not found warnings
let Auth0Lock = require('auth0-lock').default;

@Injectable()
export class Auth {

  lock
  public errorSource:BehaviorSubject<string>;

  constructor(private config:WebConfigService, private router:Router) {
    let domain = config.get("auth.domain");
    let clientId = config.get("auth.clientId");

    this.errorSource = new BehaviorSubject<string>(null);

    this.lock = new Auth0Lock(clientId, domain, { closable:false, 
                                                  auth: { redirectUrl: window.location.origin + "/login", 
                                                          responseType:'token', 
                                                          params: { scope:'openid scope' }
                                                        }
                                                });

    this.lock.on("authenticated", (authResult) => {
      localStorage.setItem('id_token', authResult.idToken);
      this.errorSource.next(null);
    });

    this.lock.on("authorization_error", (authResult) => {
      this.errorSource.next(authResult.error_description);
    });

    this.lock.on("unrecoverable_error", (authResult) => {
      this.errorSource.next(authResult.error_description);
    });
  }

  public login() {
    // Call the show method to display the widget.
    this.lock.show({ closable:false, redirectUrl: window.location.origin + "/login",
                              auth: { redirectUrl: window.location.origin + "/login",  
                                      responseType:'token', 
                                      params: { scope:'openid scope',
                                      state: JSON.stringify({ redirect: window.location.pathname + window.location.search }) }
                                    }
                            });
  }

  public authenticated() {
    // Check if there's an unexpired JWT
    // This searches for an item in localStorage with key == 'id_token'
    return tokenNotExpired();
  }

  public logout() {
    // Remove token from localStorage
    localStorage.removeItem('id_token');
  }
}