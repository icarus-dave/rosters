import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import {NgbModal, ModalDismissReasons} from '@ng-bootstrap/ng-bootstrap';

import { Operator } from './shared/operator.model';
import { OperatorService } from './shared/operator.service';
import { OperatorFormComponent } from './operator-form.component';

@Component({
  selector: 'operators',
  templateUrl: 'operators.component.html'
  })
export class OperatorsComponent implements OnInit {
  title = 'Operators';
  operators: Operator[];
  selectedOperator: Operator;
  errorMessage: string = '';

  constructor(private operatorService: OperatorService, private modalService: NgbModal) { }

  getOperators(): Promise<any> {
    return this.operatorService.getOperators()
      .then(operators => this.operators = operators )
      .catch((err) => this.errorMessage = err );
  }

  ngOnInit(): void {
    this.getOperators();
  }

  open(operator?: Operator) {
    const modalRef = this.modalService.open(OperatorFormComponent, { windowClass: 'fade' })
    modalRef.result.then((result) => { this.getOperators() }, (reason) => { } );
    //clone the operator to avoid two-way updates before saving
    if (operator) modalRef.componentInstance.operator = new Operator(operator); 
    return modalRef;
  }

}
