const { RyvrsCollection } = require('./RyvrsCollection');

const RELS_RYVRS_COLLECTION = 'https://mountain-pass.github.io/ryvr/rels/ryvrs-collection';


class RyvrApp {
  constructor() {
    this.reset();
  }

  reset() {
    this.theTitle = 'ryvr';
    this.ryvrsCollection = new RyvrsCollection();
  }

  get title() {
    return this.theTitle;
  }

  getApiDocs() {
    throw new Error('Not Impleented');
  }

  getRyvrs() {
    return this.ryvrsCollection;
  }
}


exports.RyvrApp = RyvrApp;
exports.RELS_RYVRS_COLLECTION = RELS_RYVRS_COLLECTION;
