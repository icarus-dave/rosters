import { Component } from '@angular/core';
import { OperatorService } from './operators/shared/operator.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'op';

  constructor(private operatorService: OperatorService) {}
}
