import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Version} from '../dtos/Version';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private httpClient: HttpClient) {
  }

  createTestset(): Observable<any> {
    const params = new HttpParams();
    return this.httpClient.post<any>('http://localhost:8080/createIndexTest', {params});
  }

  createSmallset(): Observable<any> {
    return this.httpClient.post<any>('http://localhost:8080/createIndex30', null);
  }

  createLargeSet(): Observable<any> {
    return this.httpClient.post<any>('http://localhost:8080/createIndex2000', null);
  }
}
