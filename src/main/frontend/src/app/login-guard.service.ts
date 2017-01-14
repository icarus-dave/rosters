import { Injectable }       from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Auth }      from './auth.service';
import { Observable } from 'rxjs';
import { URLSearchParams } from '@angular/http'

@Injectable()
export class LoginGuard implements CanActivate {

  constructor(private auth: Auth, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    //ugly processing of the returned value from auth0

    //only care if auth0 returns a fragment token
    if (!this.router.parseUrl(state.url).fragment) return false

    let params = new URLSearchParams(this.router.parseUrl(state.url).fragment)
    let returnUrl = JSON.parse(decodeURIComponent(params.get("state"))).pathname
    if (!returnUrl || returnUrl == "/") returnUrl = "/operators"

    //only proceed when the authentication token is set
    return Observable.create((x) => {
      Observable.fromEvent(this.auth.lock,'authenticated').subscribe(y => { 
        x.next(false); 
        this.router.navigateByUrl( returnUrl )
        x.complete()
      })
    });
  }

}

