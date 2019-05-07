class Ryvr {
  constructor(title, pageSize) {
    this.theTitle = title;
    this.thePageSize = pageSize;
  }

  get title() {
    return this.theTitle;
  }

  get pageSize() {
    return this.thePageSize;
  }
}

exports.Ryvr = Ryvr;
