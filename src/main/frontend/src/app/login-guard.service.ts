import { Injectable }       from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Auth }      from './auth.service';
import { Observable } from 'rxjs';
import { URLSearchParams } from '@angular/http'
import { LoginComponent } from './login.component';

@Injectable()
export class LoginGuard implements CanActivate {

  constructor(private auth: Auth, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {

    //if we're already authenticated don't let acccess
    if (this.auth.authenticated()) {
      //if we haven't already been redirected then move them away from here
      if (!this.router.navigated) this.router.navigate(["/operators"])
      return false;
    }

    //no fragment means no correct login... do something nicer
    if (!this.router.parseUrl(state.url).fragment) {
      return true
    }

    //handle the various login cases
    return Observable.create((x) => {
      Observable.fromEvent(this.auth.lock,'authenticated').subscribe(authResult => { 
        x.next(false)
        let redirect = JSON.parse((authResult as any).state).redirect
        if (redirect == "/") redirect = "/operators"
        this.router.navigateByUrl( redirect )
        x.complete()
      })
      Observable.fromEvent(this.auth.lock,'authorization_error').subscribe(y => { 
        x.next(true)
        this.router.navigateByUrl( "/login" ) //clear out the error hash from the URL
        x.complete()
      })
      Observable.fromEvent(this.auth.lock,'unrecoverable_error').subscribe(y => { 
        x.next(true)
        this.router.navigateByUrl( "/login" ) //clear out the error hash from the URL
        x.complete()
      })
    });
    
  }

}

