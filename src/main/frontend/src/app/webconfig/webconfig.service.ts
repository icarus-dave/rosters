import { Injectable }    from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { environment } from '../../environments/environment';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDataService }  from '../in-memory-data-service';

@Injectable()
export class WebConfigService {

  private webconfigUrl = `${environment.backendUrl}/webconfig`;
  private headers = new Headers({'Content-Type': 'application/json'});
  private config: any;

  constructor(private http: Http) { }

  get(key: string): string {
    return this.config[key];
  }

 load(): Promise<any> {
    return this.http.get(this.webconfigUrl)
               .toPromise()
               .then(response => this.config = response.json().data[0])
               .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    let errMsg = (error.message) ? error.message :
        error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    console.log(errMsg);
    return Promise.reject(errMsg);
  }

}
