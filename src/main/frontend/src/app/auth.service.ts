import { Injectable }      from '@angular/core';
import { tokenNotExpired } from 'angular2-jwt';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';
import { WebConfigService } from './webconfig/webconfig.service';

// Avoid name not found warnings
let Auth0Lock = require('auth0-lock').default;

@Injectable()
export class Auth {

  lock

  constructor(private config:WebConfigService, private router:Router) {
    let domain = config.get("auth.domain");
    let clientId = config.get("auth.clientId");

    this.lock = new Auth0Lock(clientId, domain, { closable:false, 
                                                  auth: { redirectUrl: window.location.origin + '/login', 
                                                          responseType:'token', 
                                                          params: { scope:'openid app_metadata' }
                                                        }
                                                });
    
    // Add callback for lock `authenticated` event
    this.lock.on("authenticated", (authResult) => {
      localStorage.setItem('id_token', authResult.idToken);
    });
  }

  public login() {
    // Call the show method to display the widget.
    this.lock.show({ closable:false, redirectUrl: window.location.origin + '/login',
                              auth: { redirectUrl: window.location.origin + '/login',  
                                      responseType:'token', 
                                      params: { scope:'openid app_metadata',
                                      state: JSON.stringify({ pathname: window.location.pathname + window.location.search }) }
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