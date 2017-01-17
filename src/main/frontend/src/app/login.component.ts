import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription, Observable } from 'rxjs';
import { Injectable, OnDestroy, OnInit } from '@angular/core';

import { Auth } from './auth.service';

@Component({
    selector: 'login',
    template: ` <div class="col alert alert-danger" role="alert" *ngIf="error">
                    Whoops... looks like something went wrong with logging you in: {{error}}
                </div>
                <div class="col alert alert-info" role="alert"> 
                    Click <a href=\"/\">here</a> to login again
                </div>`
               

})
export class LoginComponent implements OnDestroy, OnInit {
    error:string
    subscription

    constructor(private auth: Auth) { }

    ngOnInit() {
        this.subscription = this.auth.errorSource.subscribe(msg => this.error = msg);
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

}
