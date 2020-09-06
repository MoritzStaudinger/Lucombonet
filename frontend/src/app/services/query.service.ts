import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Query} from '../dtos/Query';

@Injectable({
  providedIn: 'root'
})
export class QueryService {

  constructor(private httpClient: HttpClient) {
  }

  getQueries(version: number): Observable<Query[]> {
    const params = new HttpParams().set('version', (version).toString());
    return this.httpClient.get<Query[]>('http://localhost:8080/queries', {params});
  }
}
