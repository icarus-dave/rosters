import { Injectable }    from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Operator } from './operator.model';
import { environment } from '../../../environments/environment'

@Injectable()
export class OperatorService {

  private operatorsUrl = `${environment.backendUrl}/operator`;
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http) { }

  getOperators(): Promise<Operator[]> {
    return this.http.get(this.operatorsUrl)
               .toPromise()
               .then(response => ((response.json().data || []) as Operator[]).sort(Operator.compare) )
               .catch(this.handleError);
  }

  getOperator(id: number): Promise<Operator> {
    const url = `${this.operatorsUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json().data as Operator)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    let errMsg = (error.message) ? error.message :
        error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    console.log(errMsg);
    return Promise.reject(errMsg);
  }

  update(operator: Operator): Promise<Operator> {
    const url = `${this.operatorsUrl}/${operator.id}`;
    return this.http
      .put(url, JSON.stringify(operator), {headers: this.headers})
      .toPromise()
      .then(() => operator)
      .catch(this.handleError);
  }

  create(operator: string): Promise<Operator> {
    return this.http
      .post(this.operatorsUrl, JSON.stringify(operator), {headers: this.headers})
      .toPromise()
      .then(res => res.json().data)
      .catch(this.handleError);
  }

}
