
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


  getRyvr(title) {
    const rval = this.ryvrs[title];
    if (rval) {
      return rval;
    }
    throw new Error('Not Found');
  }

  addRyvr(title, rvyr) {
    this.ryvrs[title] = rvyr;
  }

  deleteRyvr(title) {
    delete this.ryvrs[title];
  }
}
exports.RyvrsCollection = RyvrsCollection;
