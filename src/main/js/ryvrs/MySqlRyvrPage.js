import mysql from 'mysql';
import { RyvrPage } from '../model/Ryvr';

class MySqlRyvrPage extends RyvrPage {
  constructor(conn, queryStmt, underlyingPageSize, pageNo) {
    super();
    this.conn = conn;
    this.queryStmt = queryStmt;
    this.underlyingPageSize = underlyingPageSize;
    this.pageNo = pageNo;

    const stmt = mysql.format(`${this.queryStmt} LIMIT ?, ?`,
      [this.underlyingPageSize * (pageNo - 1), this.underlyingPageSize]);

    this.stream = this.conn.query(stmt);

    /* finishing init */


    this.errorPromise = new Promise((resolve, reject) => {
      this.stream.on('error', (error) => {
        reject(error);
      });
    });
    this.queryPromise = new Promise((resolve, reject) => {
      this.stream.on('result', async (row) => {
        this.cachedRows.push(row);
        if (this.cachedRows.length % 1024 === 1023) {
          // careful not to remove this next line, otherwise it will be optimised away
          // and we will not yeild to give the ryvr a change to load.
          process.stdout.write('.');
          // need to yeild here to simulate the route returning
          await new Promise((xresolve) => {
            setTimeout(() => xresolve(), 1);
          });
        }


        if (this.cachedRows.length % 10000 === 0) {
          console.log('row loaded', this.constructor.name, this.cachedRows.length + this.underlyingPageSize * (this.pageNo - 1));
        }
        resolve();
      }).on('end', () => {
        resolve();
      }).on('error', (error) => {
        reject(error);
      });
    });
    this.fieldsPromise = new Promise((resolve, reject) => {
      this.stream.on('fields', (fields) => {
      // console.log('FIELDS', fields);
        this.fields = fields.map(f => f.name);
        resolve(this.fields);
      }).on('error', (error) => {
        reject(error);
      });
    });

    this.endPromise = new Promise((resolve, reject) => {
      this.stream.on('error', (error) => {
        reject(error);
      }).on('end', () => {
        // console.log('last row of page loaded', this.cachedRows.length);
        this.ended = true;
        resolve(this);
      });
    });
  }


  async loadNextPage() {
    return new MySqlRyvrPage(this.conn, this.queryStmt, this.underlyingPageSize, this.pageNo + 1);
  }

  async hasNextPage() {
    return true;
  }

  async getUnderlyingPageSize() {
    return this.underlyingPageSize;
  }

  async getSizeInBytes() {
    await this.endPromise;
    return Buffer.byteLength(JSON.stringify(this.cachedRows), 'utf8');
  }
}


module.exports = MySqlRyvrPage;
