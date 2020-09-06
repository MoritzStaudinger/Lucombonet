import {Version} from './Version';

export class Query {
  constructor(
    public id: number,
    public query: string,
    public version: Version) {}
}
