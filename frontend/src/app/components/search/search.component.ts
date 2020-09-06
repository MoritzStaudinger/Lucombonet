import { Component, OnInit } from '@angular/core';
import {SearchService} from '../../services/search.service';
import {SearchResult} from '../../dtos/SearchResult';
import {FileService} from '../../services/file.service';
import {log} from 'util';
import {Version} from '../../dtos/Version';
import {VersionService} from '../../services/version.service';
import {Query} from '../../dtos/Query';
import {QueryService} from '../../services/query.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  constructor(private searchService: SearchService, private fileService: FileService, private versionService: VersionService, private queryService: QueryService) {
  }

  public luceneSearchResults: SearchResult[];
  public columnSearchResults: SearchResult[];
  public searchResults: SearchResult[];
  public searchVal = '';
  public pageSize = 10;
  public selectedVersion: number;
  versions: Version[];
  public selectedQuery: Query;
  queries: Query[];

  ngOnInit(): void {
    this.updateVersions();
    this.updateQueries();
  }

  updateVersions(): void {
    this.versionService.getVersions().subscribe(result => {this.versions = result; });
  }

  updateQueries(): void {
    if (this.selectedVersion === undefined && this.versions.length >= 1) {
      this.selectedVersion = this.versions[0].id;
    }
    this.queryService.getQueries(this.selectedVersion).subscribe(result => {this.queries = result; });
  }

  public searchCompare() {
    this.searchService.getLuceneSearch(this.searchVal, this.pageSize).subscribe(result => {this.luceneSearchResults = result; });
    this.searchService.getMariaDBSearch(this.searchVal, this.pageSize).subscribe(result => {this.columnSearchResults = result; });
    this.updateVersions();
  }

  public searchVersion() {
    if (this.selectedVersion === undefined && this.versions.length >= 1) {
      this.selectedVersion = this.versions[0].id;
    }
    this.searchService.getSearchVersion(this.searchVal, this.pageSize, this.selectedVersion)
      .subscribe(result => {this.searchResults = result; });
    this.updateVersions();
  }

  public searchSave() {
    this.searchService.getSearchSave(this.searchVal, this.pageSize).subscribe(result => {this.searchResults = result; });
    this.updateVersions();
  }

  reproduceQuery() {
    this.searchService.getSearchVersion(this.selectedQuery.query, this.pageSize, this.selectedQuery.version.id)
      .subscribe(result => {this.searchResults = result; });
    this.updateVersions();
  }

  public testset() {
    this.fileService.createTestset();
    this.updateVersions();
  }

  public smallset() {
    this.fileService.createSmallset();
    this.updateVersions();
  }

  public largeset() {

  }

}
