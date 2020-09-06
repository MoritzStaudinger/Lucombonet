import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {SearchResult} from '../dtos/SearchResult';

@Injectable({
  providedIn: 'root'
})
export class SearchService {

  constructor(private httpClient: HttpClient) {
  }

  getLuceneSearch(searchstring: string, resultnumber: number): Observable<SearchResult[]>{
    let params = new HttpParams().set('resultnumber', (resultnumber).toString());
    params = params.set('searchstring', searchstring);
    return this.httpClient.get<SearchResult[]>('http://localhost:8080/searchLucene', {params});
  }

  getMariaDBSearch(searchstring: string, resultnumber: number) {
    let params = new HttpParams().set('resultnumber', (resultnumber).toString());
    params = params.set('searchstring', searchstring);
    return this.httpClient.get<SearchResult[]>('http://localhost:8080/searchMariaDB', {params});
  }

  getSearchSave(searchstring: string, resultnumber: number) {
    let params = new HttpParams().set('resultnumber', (resultnumber).toString());
    params = params.set('searchstring', searchstring);
    return this.httpClient.get<SearchResult[]>('http://localhost:8080/search', {params});
  }

  getSearchVersion(searchstring: string, resultnumber: number, version: number) {
    let params = new HttpParams().set('resultnumber', (resultnumber).toString());
    params = params.set('searchstring', searchstring);
    params = params.set('version', (version).toString());
    return this.httpClient.get<SearchResult[]>('http://localhost:8080/searchVersion', {params});
  }

}
