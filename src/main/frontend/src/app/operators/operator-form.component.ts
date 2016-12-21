import { Component } from '@angular/core';
import { NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';

import { Operator } from './shared/operator.model';
import { OperatorService } from './shared/operator.service';

@Component({
  selector: 'operator-form',
  templateUrl: 'operator-form.component.html'})
export class OperatorFormComponent {

  errorMessage: string;
  public form: FormGroup;
  operator: Operator;

  constructor(public activeModal: NgbActiveModal, private operatorService: OperatorService, public fb: FormBuilder) {
    this.setOperatorForm(new Operator());
  }

  setOperatorForm(operator: Operator) {
    this.operator = operator;
    this.form = this.fb.group({
      "id": operator.id,
      "firstName":[operator.firstName,Validators.required],
      "lastName":[operator.lastName,Validators.required],
      "email":[operator.email, Validators.compose([Validators.required, Validators.pattern('.+@.+\\..+')])],
      "active":operator.active
    });
  }

  onSubmit(): Promise<any> {
    let operator = new Operator(this.form.value);
    if (operator.id) {
      return this.operatorService.update(operator)
        .then(operator => { 
          this.activeModal.close(operator);
        })
        .catch(errMsg => this.errorMessage = errMsg);
    } else {
      return this.operatorService.create(operator)
        .then(operator => { 
          this.activeModal.close(operator);
        })
        .catch(errMsg => this.errorMessage = errMsg);
    }
  }

}
