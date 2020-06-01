import { Component, OnInit } from '@angular/core';
import {SearchService} from '../../services/search.service';
import {SearchResult} from '../../dtos/SearchResult';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  constructor(private searchService: SearchService) {
  }

  public searchResults: SearchResult[];
  public searchVal = '';
  public pageSize = 10;

  ngOnInit(): void {
  }

  public search() {
    console.log(this.searchVal);
    this.searchService.getLuceneSearch(this.searchVal, this.pageSize).subscribe(result => {this.searchResults = result; });
    console.log(this.searchResults);
  }

}
