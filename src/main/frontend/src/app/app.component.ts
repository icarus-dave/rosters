import { Component } from '@angular/core';

import { OperatorService } from './operators/shared/operator.service';
import { Auth } from './auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  providers: [ Auth ],
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(private operatorService: OperatorService, private auth: Auth) {}
}
