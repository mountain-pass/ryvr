import { Ryvr } from '../model/Ryvr';


class MySqlRyvr extends Ryvr {
  constructor(title, pageSize, conn, query) {
    super(title, pageSize);
    this.conn = conn;
    this.query = query;
  }
}

module.exports = MySqlRyvr;
