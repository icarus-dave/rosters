import { Injectable }    from '@angular/core';
import { Headers, Http } from '@angular/http';
import { AuthHttp } from 'angular2-jwt';

import 'rxjs/add/operator/toPromise';

import { Auth } from '../../auth.service';
import { Operator } from './operator.model';
import { environment } from '../../../environments/environment'

@Injectable()
export class OperatorService {

  private operatorsUrl = `${environment.backendUrl}/operator`;
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private auth: Auth, private http: AuthHttp) { }

  getOperators(): Promise<Operator[]> {
    return this.http.get(this.operatorsUrl)
               .toPromise()
               .then(response => { 
                return ((response.json().data || []) as Operator[]).sort(Operator.compare) 
               })
               .catch(this.handleError);
  }

  getOperator(id: number): Promise<Operator> {
    const url = `${this.operatorsUrl}/${id}`;
    return this.http.get(url)
      .toPromise()
      .then(response => response.json() as Operator)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    let errMsg: string;
    if (error.json() != null && error.json().error) errMsg = error.json().error
    else if (error.json() != null) errMsg = `${error.json().code} - ${error.json().message}`
    else errMsg = (error.message) ? error.message :
        error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    return Promise.reject(errMsg);
  }

  update(operator: Operator): Promise<Operator> {
    const url = `${this.operatorsUrl}/${operator.id}`;
    return this.http
      .put(url, JSON.stringify(operator), {headers: this.headers})
      .toPromise()
      .then(res => res.json() as Operator)
      .catch(this.handleError);
  }

  create(operator: Operator): Promise<Operator> {
    return this.http
      .post(this.operatorsUrl, JSON.stringify(operator), {headers: this.headers})
      .toPromise()
      .then(res => res.json() as Operator)
      .catch(this.handleError);
  }

}
