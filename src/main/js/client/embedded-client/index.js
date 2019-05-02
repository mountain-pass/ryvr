
/* eslint-disable import/prefer-default-export */


export class EmbeddedRyvrRoot {
  constructor(client) {
    this.client = client;
    this.ryvrsCollection = {};
  }

  get title() {
    return 'ryvr';
  }

  // eslint-disable-next-line no-unused-vars
  async login(username, password) {
    // no-op for js API
  }

  async getApiDocs() {
    return null;
  }

  async getRyvrs() {
    return this.ryvrsCollection;
  }
}

class MysqlRyvrSource {

}
class Ryvr {

}

export class EmbeddedRyvrClient {
  constructor() {
    this.root = new EmbeddedRyvrRoot(this);
  }

  async getRoot() {
    return this.root;
  }


  createDataSourceRyvr(conn, config) {
    const src = new MysqlRyvrSource(conn, config.query);
    this.root.ryvrsCollection[config.name] = new Ryvr(config.name, config['page size'], src);
  }
}
