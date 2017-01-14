import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { Auth } from './auth.service';

@Component({
    selector: 'login',
    template: ""
})
//token component to enable the login-guard
export class LoginComponent {
    constructor() { }
}
