
class RyvrsCollection {
  constructor() {
    this.ryvrs = [];
  }

  get length() {
    return this.keys.length;
  }

  get keys() {
    return Object.keys(this.ryvrs);
  }


  async getRyvr(title) {
    const rval = this.ryvrs[title];
    if (rval) {
      return rval;
    }

    throw new Error('Not Found');
  }

  async addRyvr(title, rvyr) {
    this.ryvrs[title] = rvyr;
  }
}
exports.RyvrsCollection = RyvrsCollection;
