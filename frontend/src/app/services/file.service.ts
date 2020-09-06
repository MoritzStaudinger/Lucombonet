import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private httpClient: HttpClient) {
  }

  createTestset(): void {
    this.httpClient.post('http://localhost:8080/createIndexTest', '');
  }

  createSmallset(): void {
    this.httpClient.post<string>('http://localhost:8080/createIndex30', null);
  }

  createLargeSet(): void {
    this.httpClient.post<string>('http://localhost:8080/createIndex2000', null);
  }
}
