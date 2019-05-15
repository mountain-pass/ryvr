import { Ryvr } from '../model/Ryvr';

class MySqlRyvr extends Ryvr {
  constructor(title, pageSize, conn, queryStmt) {
    super(title, pageSize);
    this.conn = conn;
    this.queryStmt = queryStmt;
    this.cachedRows = [];
    this.fields = [];

    this.currentPos = 0;
    this.ended = false;

    /*

      So I've just learnt that the JDBC client allows seeking by loading all the rows into memory :(
      We'll need to be clever with LIMIT and OFFSET and have a set of queries we can execute
      which load the data into a reusable buffer
      When we seek past the end of the buffer, we get the next page

      At least I now understand why it was so fast gettting the next results from the resultSet
      It was just getting them from memory.

      Open question: if we stick to the C API and use mysql_stmt_fetch() or mysql_fetch_row()
      to grab the data in sequence, without having to read pages. We would only need to
      re-execute the query when moving backwards.

      BUT this isn't going to help us in the long run,
      because when we are rendering the current page, we need to requery and then seek to the
      current page, which requires iterating over each record. Much faster (I think) to use LIMIT
      and offset in the query.
    */


    this.stream = this.conn.query(this.queryStmt);
    this.queryPromise = new Promise((resolve, reject) => {
      // we need to use a streaming query, otherwise
      // it will load all the records into memory
      this.stream.on('fields', (fields) => {
        // console.log('FIELDS', fields);
        this.fields = fields;
      }).on('error', (error) => {
        reject(error);
      }).on('result', (row) => {
        // At the moment this will keep reading until we have read all the rows or
        // until all memory is exhausted.
        // Need to add limit and offset to query to handle this betters

        this.cachedRows.push(row);
        if (this.cachedRows.length >= this.pageSize) {
          resolve();
        }
      }).on('end', function () {
        this.ended = true;
        resolve();
      });
    });
  }

  seek(i) {
    const ryvr = this;
    let index = i;
    if (index < 0) {
      throw new Error('Not Found');
    }
    const iterator = {
      next: async () => {
        await ryvr.queryPromise;
        if (index >= ryvr.cachedRows.length) {
          // can't assume we are done. When we have query paging, we'll need to get the next page.
          return { done: true };
        }
        const value = ryvr.cachedRows[index];
        index += 1;
        return { value, done: false };
      },

      hasNext: async () => {
        await ryvr.queryPromise;
        console.log('INDEX', index);
        console.log('ryvr.cachedRows.length', ryvr.cachedRows.length);
        return (index - 1) < ryvr.cachedRows.length;
      },
    };

    return iterator;
  }

  [Symbol.asyncIterator]() {
    return this.seek(0);
  }

  async getFields() {
    await this.queryPromise;
    return this.fields.map(f => f.name);
  }
}


module.exports = MySqlRyvr;
