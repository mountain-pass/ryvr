

class RyvrApp {
  constructor() {
    this.theTitle = 'ryvr';
    this.ryvrsCollection = {};
  }

  get title() {
    return this.theTitle;
  }

  // eslint-disable-next-line no-unused-vars
  async login(username, password) {
    // no-op for js API
  }

  async getApiDocs() {
    throw new Error('Not Impleented');
  }

  async getRyvrs() {
    return this.ryvrsCollection;
  }

  async getRyvr(title) {
    return Promise.resolve(this.ryvrsCollection[title]);
  }


  async addRvyr(name, rvyr) {
    this.ryvrsCollection[name] = rvyr;
  }
}

exports.RyvrApp = RyvrApp;
