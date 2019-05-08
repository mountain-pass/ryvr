import { Ryvr } from '../model/Ryvr';

class MySqlRyvr extends Ryvr {
  constructor(title, pageSize, conn, query) {
    super(title, pageSize);
    this.conn = conn;
    this.query = query;
    this.queryPromise = new Promise((resolve, reject) => {
      this.conn.query(this.query, (err, results, fields) => {
        if (this.err) {
          reject(err);
        } else {
          this.results = results;
          this.fields = fields;
          resolve();
        }
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
        if (index >= ryvr.results.length) {
          return { done: true };
        }
        const value = ryvr.results[index];
        index += 1;
        return { value, done: false };
      },
    };

    return iterator;
  }

  [Symbol.asyncIterator]() {
    return this.seek(0);
  }
}


module.exports = MySqlRyvr;
