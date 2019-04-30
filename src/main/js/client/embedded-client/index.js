
/* eslint-disable import/prefer-default-export */

export class EmbeddedRyvrRoot {
  constructor(client) {
    this.client = client;
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
    return [];
  }
}

export class EmbeddedRyvrClient {
  async getRoot() {
    return new EmbeddedRyvrRoot(this);
  }
}
