import { Component } from '@angular/core';
import { OnInit } from '@angular/core';

import { Operator } from './shared/operator.model';
import { OperatorService } from './shared/operator.service';
import { Router }     from '@angular/router';

@Component({
  selector: 'operators',
  templateUrl: 'operators.component.html',
  styleUrls: [ 'operators.component.css' ]
  })
export class OperatorsComponent implements OnInit {
  title = 'Operators';
  operators: Operator[];
  selectedOperator: Operator;
  errorMessage: string = '';

  constructor(private operatorService: OperatorService, private router: Router) { }

  getOperators(): void {
    this.operatorService.getOperators()
      .then(operators => this.operators = operators)
      .catch((err) => this.errorMessage = err );
  }

  onSelect(operator: Operator): void {
    this.selectedOperator = operator;
  }

  ngOnInit(): void {
    this.getOperators();
  }

}
