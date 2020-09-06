import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {SearchResult} from '../dtos/SearchResult';
import {Version} from '../dtos/Version';

@Injectable({
  providedIn: 'root'
})
export class VersionService {

  constructor(private httpClient: HttpClient) {
  }

  getVersions(): Observable<Version[]>{
    const params = new HttpParams();
    return this.httpClient.get<Version[]>('http://localhost:8080/version', {params});
  }


}
